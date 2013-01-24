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
package org.deephacks.westty.example;

import java.io.File;

import org.deephacks.tools4j.config.admin.JaxrsConfigClient;
import org.deephacks.westty.Westty;
import org.deephacks.westty.config.WebConfig;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class ExampleJaxrs {
    public static final String prop = "conf/jpa.properties";

    public static void main(String[] args) throws Throwable {

        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        DdlExec.executeResource("META-INF/uninstall_derby.ddl", prop, true);
        DdlExec.executeResource("META-INF/install_derby.ddl", prop, true);
        DdlExec.executeResource("META-INF/uninstall.ddl", prop, true);
        DdlExec.executeResource("META-INF/install.ddl", prop, true);

        File file = new File("./src/main/resources");
        Westty westty = new Westty();
        westty.setRootDir(file);
        westty.startup();

        JaxrsConfigClient client = new JaxrsConfigClient("localhost", 8080, "/jaxrs");
        WebConfig config = new WebConfig();
        config.setStaticRoot(file.getAbsolutePath());
        client.merge(config);

    }
}
