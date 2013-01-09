/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package launcher;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Launcher is responsible for setting the classpath and 
 * launching the main program.
 */
public class Launcher {
    /** files to put on current thread classloader */
    private final List<File> classpath = new ArrayList<File>();
    /** system property to determine launcher run script directory  */
    private static final String LAUNCHER_HOME_PROP = "launcher.home";
    /** system property of main class to launch  */
    private static final String MAIN_CLASS_PROP = "main.class";
    /** system property of directory to load classpath from */
    private static final String LIB_DIR_PROP = "lib.dir";
    /** Are we in debug mode, --debug */
    private boolean debug = false;

    public static void main(String[] args) {
        Launcher launcher = new Launcher();
        for (int i = 0; i < args.length; i++) {
            if ("--debug".equals(args[i].trim())) {
                launcher.debug = true;
            }
        }
        launcher.run(args);
    }

    public void run(String[] args) {
        String launcherHome = System.getProperty(LAUNCHER_HOME_PROP);
        if (launcherHome == null || "".equals(launcherHome)) {
            throw new IllegalArgumentException("set launcher.home system property");
        }
        String mainClass = System.getProperty(MAIN_CLASS_PROP);
        if (mainClass == null || "".equals(launcherHome)) {
            throw new IllegalArgumentException("set main.class system property");
        }
        String libDir = System.getProperty(LIB_DIR_PROP);
        if (libDir == null || "".equals(libDir)) {
            throw new IllegalArgumentException("set lib.dir system property");
        }
        add(new File(launcherHome, libDir));
        setContextClassLoader();
        initLogback(new File(launcherHome + "/conf", "logback.xml"));
        try {
            Class<?> main = Thread.currentThread().getContextClassLoader().loadClass(mainClass);
            main.getDeclaredMethod("main", String[].class).invoke(null, new Object[] { args });
        } catch (InvocationTargetException e) {
            Throwable ex = e.getTargetException();
            if (ex instanceof RuntimeException) {
                System.out.println(ex.getMessage());
                if (debug) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setContextClassLoader() {
        URL[] urls = new URL[classpath.size()];
        for (int i = 0; i < urls.length; i++) {
            urls[i] = toURL(classpath.get(i));
        }
        URLClassLoader loader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
        Thread.currentThread().setContextClassLoader(loader);
    }

    public void add(Collection<File> files) {
        for (File file : files) {
            add(file);
        }
    }

    public void add(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException(file.getAbsolutePath() + " does not exist.");
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                if (!f.isDirectory()) {
                    classpath.add(f);
                }
            }
        } else {
            classpath.add(file);
        }
    }

    private static URL toURL(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    void initLogback(File file) {
        if (!file.exists()) {
            if (debug) {
                System.out.println(file.getAbsolutePath() + " does not exist.");
            }
            return;
        }
        Class<?> loggerFactoryCls = null;
        Class<?> loggerContextCls = null;
        Class<?> contextCls = null;
        Class<?> joranConfiguratorCls = null;
        try {
            loggerFactoryCls = Thread.currentThread().getContextClassLoader()
                    .loadClass("org.slf4j.LoggerFactory");
            loggerContextCls = Thread.currentThread().getContextClassLoader()
                    .loadClass("ch.qos.logback.classic.LoggerContext");
            contextCls = Thread.currentThread().getContextClassLoader()
                    .loadClass("ch.qos.logback.core.Context");
            joranConfiguratorCls = Thread.currentThread().getContextClassLoader()
                    .loadClass("ch.qos.logback.classic.joran.JoranConfigurator");
        } catch (ClassNotFoundException e) {
            // missing logging classes, ignore
            if (debug) {
                e.printStackTrace();
            }
            return;
        }
        try {
            Object LoggerContext = loggerFactoryCls.getMethod("getILoggerFactory").invoke(
                    (Object) null);
            Object JoranConfigurator = joranConfiguratorCls.newInstance();
            joranConfiguratorCls.getMethod("setContext", contextCls).invoke(JoranConfigurator,
                    LoggerContext);
            loggerContextCls.getMethod("reset", (Class<?>[]) null).invoke(LoggerContext);
            joranConfiguratorCls.getMethod("doConfigure", File.class).invoke(JoranConfigurator,
                    file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
