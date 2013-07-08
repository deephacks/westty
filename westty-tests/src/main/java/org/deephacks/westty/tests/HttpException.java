package org.deephacks.westty.tests;

import javax.ws.rs.core.Response.Status;

public class HttpException extends RuntimeException {
    private Status status;

    public HttpException(Status status, String msg){
        super(msg);
        this.status = status;
    }

    public Status getCode(){
        return status;
    }
}