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

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.deephacks.westty.example.CreateMessages.CreateRequest;
import org.deephacks.westty.example.CreateMessages.CreateResponse;
import org.deephacks.westty.jpa.Transactional;
import org.deephacks.westty.protobuf.Protobuf;
import org.deephacks.westty.protobuf.ProtobufMethod;

@Protobuf("create")
public class ProtobufEndpoint {
    @Inject
    private EntityManager em;

    @ProtobufMethod
    @Transactional
    public CreateResponse execute(CreateRequest request) {
        System.out.println(request);
        ExampleEntity entity = new ExampleEntity(request.getName(), "config prop, fixme");
        em.persist(entity);
        return CreateResponse.newBuilder().setMsg("success").build();
    }
}