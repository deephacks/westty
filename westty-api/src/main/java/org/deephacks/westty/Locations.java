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

public class Locations {
    public static final File ROOT_DIR = new File(System.getProperty("launcher.home"));
    public static final String CONF = "conf";
    public static final File CONF_DIR = new File(ROOT_DIR, CONF);
    public static final String LIB = "lib";
    public static final File LIB_DIR = new File(ROOT_DIR, LIB);
    public static final String BIN = "bin";
    public static final File BIN_DIR = new File(ROOT_DIR, BIN);

}
