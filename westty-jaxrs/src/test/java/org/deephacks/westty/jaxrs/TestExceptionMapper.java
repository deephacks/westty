package org.deephacks.westty.jaxrs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class TestExceptionMapper implements ExceptionMapper<Exception> {

    private Logger logger = LoggerFactory.getLogger(TestExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        final Response response;
        if (exception instanceof IllegalArgumentException) {
            response = Response.status(Status.BAD_REQUEST).entity(exception.getMessage()).build();
        } else {
            logger.warn("Unexpected {} with message {}", exception.getClass().getName(), exception.getMessage());
            response = Response.serverError().entity(exception.getMessage()).build();
        }
        logger.debug("Uncaught exception. ", exception);
        return response;
    }
}
