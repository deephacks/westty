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
import org.deephacks.westty.config.JpaConfig;
import org.deephacks.westty.config.WebConfig;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class ExampleConfig {
    static {
        // make sure loggin is initalized before anything else
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        new JpaConfig().dropInstall();
    }

    private static final Westty westty = new Westty();

    private File root = new File("./src/main/resource");
    private JaxrsConfigClient client = new JaxrsConfigClient("localhost", 8080, "/jaxrs");

    @BeforeClass
    public static void beforeClass() {
        westty.startup();
    }

    @Before
    public void before() {

    }

    @AfterClass
    public static void afterClass() {
        westty.stop();
    }

    @Test
    public void test_set_config() {
        WebConfig config = new WebConfig();
        config.setStaticRoot(root.getAbsolutePath());
        client.merge(config);
    }
}
