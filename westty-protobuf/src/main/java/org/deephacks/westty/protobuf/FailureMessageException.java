package org.deephacks.westty.protobuf;

import org.deephacks.westty.protobuf.FailureMessages.Failure;

import com.google.common.base.Preconditions;

public class FailureMessageException extends RuntimeException {

    private static final long serialVersionUID = -7994691832123397253L;
    private Failure failure;

    public FailureMessageException(Failure failure) {
        this.failure = Preconditions.checkNotNull(failure);
    }

    public Failure getFailure() {
        return failure;
    }

}
