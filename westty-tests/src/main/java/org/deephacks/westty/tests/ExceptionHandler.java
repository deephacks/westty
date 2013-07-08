package org.deephacks.westty.tests;

import org.deephacks.westty.protobuf.ProtobufException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

public class ExceptionHandler implements ExceptionMapper<Exception> {
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

    public Response toResponse(Exception ex) {
        log.warn("{}", ex.getMessage());
        log.debug("Exception occured", ex);
        Status status = null;
        String message = "";
        if (ex instanceof HttpException) {
            HttpException e = ((HttpException) ex);
            status = e.getCode();
            message = e.getMessage();
        } else if (ex instanceof ProtobufException) {
            ProtobufException e = ((ProtobufException) ex);
            throw e;
        }
        if (status == null) {
            status = Status.INTERNAL_SERVER_ERROR;
        }
        return Response.serverError().status(status).entity(message).build();
    }

}
