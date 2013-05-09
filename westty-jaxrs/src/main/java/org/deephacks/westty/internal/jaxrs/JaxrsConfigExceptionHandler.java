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
package org.deephacks.westty.internal.jaxrs;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.deephacks.tools4j.config.model.AbortRuntimeException;
import org.deephacks.tools4j.config.model.Event;
import org.deephacks.tools4j.config.model.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
class JaxrsConfigExceptionHandler implements ExceptionMapper<AbortRuntimeException> {
    private Logger log = LoggerFactory.getLogger(JaxrsConfigExceptionHandler.class);

    public Response toResponse(AbortRuntimeException ex) {
        log.warn("{}", ex.getMessage());
        log.debug("Exception occured.", ex);
        Event e = ex.getEvent();
        Status status = null;
        // 300 codes are reserved for user input errors
        switch (e.getCode()) {
        case Events.CFG101:
            status = Status.NOT_FOUND;
            break;
        case Events.CFG102:
            status = Status.BAD_REQUEST;
            break;
        case Events.CFG103:
            status = Status.BAD_REQUEST;
            break;
        case Events.CFG104:
            status = Status.BAD_REQUEST;
            break;
        case Events.CFG105:
            status = Status.BAD_REQUEST;
            break;
        case Events.CFG106:
            status = Status.BAD_REQUEST;
            break;
        case Events.CFG107:
            status = Status.BAD_REQUEST;
            break;
        case Events.CFG108:
            status = Status.BAD_REQUEST;
            break;
        case Events.CFG109:
            status = Status.BAD_REQUEST;
            break;
        case Events.CFG110:
            status = Status.BAD_REQUEST;
            break;
        case Events.CFG111:
            status = Status.BAD_REQUEST;
            break;
        case Events.CFG301:
            status = Status.NOT_FOUND;
            break;
        case Events.CFG302:
            status = Status.FORBIDDEN;
            break;
        case Events.CFG303:
            status = Status.CONFLICT;
            break;
        case Events.CFG304:
            status = Status.NOT_FOUND;
            break;
        case Events.CFG305:
            status = Status.BAD_REQUEST;
            break;
        case Events.CFG306:
            status = Status.BAD_REQUEST;
            break;
        case Events.CFG307:
            status = Status.FORBIDDEN;
            break;
        case Events.CFG309:
            status = Status.BAD_REQUEST;
            break;
        case Events.CFG310:
            status = Status.CONFLICT;
            break;
        case Events.CFG311:
            status = Status.FORBIDDEN;
            break;
        default:
            status = Status.INTERNAL_SERVER_ERROR;
            break;
        }
        if (status == null) {
            status = Status.INTERNAL_SERVER_ERROR;
        }
        return Response.serverError().status(status).entity(e.getMessage()).build();

    }
}
