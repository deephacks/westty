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
package org.deephacks.westty.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Schedule("0/2 * * * * ?")
public class Job1 implements Job {
    private Logger logger = LoggerFactory.getLogger(Job1.class);
    private String key = "test";

    @Override
    public void execute(JobData map) {
        String str = map.get(key);
        int i = 0;
        if (str != null) {
            i = Integer.parseInt(str);
        }
        map.put(key, "" + ++i);
        logger.debug("" + i);
    }
}