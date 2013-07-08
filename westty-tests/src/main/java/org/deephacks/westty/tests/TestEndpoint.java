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
package org.deephacks.westty.tests;

import org.deephacks.westty.jpa.Transactional;
import org.deephacks.westty.sockjs.SockJsEndpoint;
import org.deephacks.westty.sockjs.SockJsMessage;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Endpoint that use all the features available in westty in
 * order to test how they integrate. This is only a test. This
 * kind of endpoints is not advised.
 */
@Singleton
@SockJsEndpoint
public class TestEndpoint  {
    public static final String SERVER_EVENTBUS_ADDRESS = "server";
    @Inject
    private EventBus bus;

    @SockJsMessage(SERVER_EVENTBUS_ADDRESS)
    @Transactional
    public void event(Message<JsonObject> msg){
        System.out.println(msg);
    }
}
