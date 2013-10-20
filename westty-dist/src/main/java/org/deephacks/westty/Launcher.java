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
package org.deephacks.westty;

import java.io.File;
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
    private final List<File> classpath = new ArrayList<>();
    /** system property of main class to launch  */
    private static final String MAIN_CLASS_PROP = "main.class";
    /** system property to determine launcher run script directory  */
    private static final String LAUNCHER_HOME_PROP = "WESTTY_LAUNCHER_HOME";
    /** system property of directory to load westty classpath from */
    private static final String LIB_DIR_PROP = "WESTTY_LIB_DIR";
    /** system property of directory to load application classpath from */
    private static final String APP_DIR_PROP = "WESTTY_APP_DIR";
    /** system property of directory to load application classpath from */
    private static final String CONF_DIR_PROP = "WESTTY_CONF_DIR";

    /** Are we in debug mode, --debug */
    private boolean debug = false;

    public static void main(String[] args) throws Exception {
        Launcher launcher = new Launcher();
        for (int i = 0; i < args.length; i++) {
            if ("--debug".equals(args[i].trim())) {
                launcher.debug = true;
            }
        }
        launcher.run(args);
    }

    public void run(String[] args) throws Exception {
        String launcherHome = System.getProperty(LAUNCHER_HOME_PROP);
        if (launcherHome == null || "".equals(launcherHome)) {
            throw new IllegalArgumentException("set " + LAUNCHER_HOME_PROP + " system property");
        }
        String mainClass = System.getProperty(MAIN_CLASS_PROP);
        if (mainClass == null || "".equals(launcherHome)) {
            throw new IllegalArgumentException("set main.class system property");
        }
        String libDir = System.getProperty(LIB_DIR_PROP);
        if (libDir == null || "".equals(libDir)) {
            throw new IllegalArgumentException("set " + LIB_DIR_PROP + " system property");
        }
        String appDir = System.getProperty(APP_DIR_PROP);
        if (appDir == null || "".equals(appDir)) {
            throw new IllegalArgumentException("set " + APP_DIR_PROP + " system property");
        }
        String confDir = System.getProperty(CONF_DIR_PROP);
        if (confDir == null || "".equals(confDir)) {
            throw new IllegalArgumentException("set " + CONF_DIR_PROP + " system property");
        }
        add(new File(libDir));
        add(new File(appDir));
        setContextClassLoader();
        initLogback(new File(confDir, "westty-logback.xml"));

        Class<?> main = Thread.currentThread().getContextClassLoader().loadClass(mainClass);
        main.getDeclaredMethod("main", String[].class).invoke(null, new Object[] { args });
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
