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
import org.deephacks.westty.jaxrs.JaxrsConfigError;
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
        if (e.getCode() >= 300 && e.getCode() < 400) {
            status = Status.BAD_REQUEST;
        }
        if (status == null) {
            status = Status.INTERNAL_SERVER_ERROR;
        }
        JaxrsConfigError err = new JaxrsConfigError(e.getModule(), e.getCode(), e.getMessage());
        return Response.serverError().status(status).entity(err).build();

    }
}
