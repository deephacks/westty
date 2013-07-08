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
package org.deephacks.westty.protobuf;

import org.deephacks.westty.protobuf.CreateMessages.AsyncCreateRequest;
import org.deephacks.westty.protobuf.CreateMessages.CreateExceptionRequest;
import org.deephacks.westty.protobuf.CreateMessages.CreateRequest;
import org.deephacks.westty.protobuf.CreateMessages.CreateResponse;
import org.deephacks.westty.protobuf.CreateMessages.GetRequest;
import org.deephacks.westty.protobuf.CreateMessages.NullRequest;
import org.deephacks.westty.protobuf.DeleteMessages.DeleteRequest;
import org.deephacks.westty.protobuf.DeleteMessages.DeleteResponse;
import org.deephacks.westty.config.ProtobufConfig;

import javax.inject.Inject;
import java.util.LinkedList;

@Protobuf({ "create", "delete" })
public class ProtobufEndpoint {

    @Inject
    private ProtobufConfig config;

    private static final LinkedList<Object> requests = new LinkedList<>();

    @ProtobufMethod
    public CreateResponse create(CreateRequest request) throws ProtobufException {
        if(config == null){
            throw new ProtobufException("Config could not be injected", Integer.MAX_VALUE);
        }
        requests.addFirst(request);
        return CreateResponse.newBuilder().setMsg(request.getMsg()).build();
    }

    @ProtobufMethod
    public CreateResponse nullRequest(NullRequest request) throws ProtobufException {
        if(config == null){
            throw new ProtobufException("Config could not be injected", Integer.MAX_VALUE);
        }
        return null;
    }

    @ProtobufMethod
    public DeleteResponse delete(DeleteRequest request) throws ProtobufException {
        if(config == null){
            throw new ProtobufException("Config could not be injected", Integer.MAX_VALUE);
        }
        return DeleteResponse.newBuilder().setMsg(request.getMsg()).build();
    }

    @ProtobufMethod
    public void asyncCreate(AsyncCreateRequest request) throws ProtobufException {
        if(config == null){
            throw new ProtobufException("Config could not be injected", Integer.MAX_VALUE);
        }
        requests.addFirst(request);
    }

    @ProtobufMethod
    public Object getRequest(GetRequest request) throws ProtobufException {
        if(config == null){
            throw new ProtobufException("Config could not be injected", Integer.MAX_VALUE);
        }
        return requests.pollFirst();
    }

    @ProtobufMethod
    public void exception(CreateExceptionRequest request) throws ProtobufException {
        if(config == null){
            throw new ProtobufException("Config could not be injected", Integer.MAX_VALUE);
        }
        throw new ProtobufException("Always fails", Integer.MIN_VALUE);
    }
}
