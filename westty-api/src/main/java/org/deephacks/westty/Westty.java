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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Westty {
    private static final Logger log = LoggerFactory.getLogger(Westty.class);
    private static final String WESTTY_CORE = "org.deephacks.westty.internal.core.WesttyCore";
    private static Object WESTTY;

    public static void main(String[] args) {
        Westty westty = new Westty();
        westty.startup();
    }

    public synchronized void startup() {
        log.info("Westty startup.");
        if (WESTTY == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            try {
                WESTTY = cl.loadClass(WESTTY_CORE).newInstance();
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }

        try {
            WESTTY.getClass().getMethod("startup").invoke(WESTTY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("Westty ready.");
    }

    public synchronized void stop() {
        log.info("Westty shutdown.");
        try {
            WESTTY.getClass().getMethod("shutdown").invoke(WESTTY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            WESTTY = null;
        }
    }

}
