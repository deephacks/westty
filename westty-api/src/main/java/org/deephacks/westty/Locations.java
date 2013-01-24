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

import com.google.common.base.Strings;

public class Locations {
    private static final String LAUNCHER_HOME_PROP = "launcher.home";
    private static final String LAUNCHER_HOME = System.getProperty(LAUNCHER_HOME_PROP);

    private static File ROOT_DIR;
    private static String CONF = "conf";
    private static String LIB = "lib";
    private static String BIN = "bin";

    public static void setRootDir(File dir) {
        if (dir == null || !dir.exists()) {
            throw new IllegalArgumentException("Directory is not valid " + dir.getAbsolutePath());
        }
        ROOT_DIR = dir;
    }

    public static File getRootDir() {
        if (ROOT_DIR == null || !ROOT_DIR.exists()) {
            if (Strings.isNullOrEmpty(LAUNCHER_HOME)) {
                throw new IllegalStateException(
                        "Root dir not set. Use Westty.setRootDir or set system property "
                                + LAUNCHER_HOME_PROP + ".");
            } else {
                File file = new File(LAUNCHER_HOME);
                if (!file.exists()) {
                    throw new IllegalStateException("Directory " + LAUNCHER_HOME
                            + " does not exist, defined by " + LAUNCHER_HOME_PROP
                            + " system property");
                }
            }

        }
        return ROOT_DIR;
    }

    public static File getConfDir() {
        File root = getRootDir();
        return new File(root, CONF);
    }

    public static File getLibDir() {
        File root = getRootDir();
        return new File(root, LIB);
    }

    public static File getBinDir() {
        File root = getRootDir();
        return new File(root, BIN);
    }

}
