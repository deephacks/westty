package org.deephacks.westty.protobuf;

import org.deephacks.westty.protobuf.FailureMessages.Failure;

import com.google.common.base.Preconditions;

public class FailureMessageException extends RuntimeException {

    private static final long serialVersionUID = 1409792663033749236L;
    private Integer code;
    private String message;

    public FailureMessageException(Failure failure) {
        this.code = failure.getCode();
        this.message = Preconditions.checkNotNull(failure.getMsg());
    }

    public FailureMessageException(String message, Integer code) {
        this.code = Preconditions.checkNotNull(code);
        this.message = Preconditions.checkNotNull(message);
    }

    public FailureMessageException(String message, Integer code, Exception e) {
        super(e);
        this.code = Preconditions.checkNotNull(code);
    }

    public static void throwBadRequest(String message) {
        throw new FailureMessageException(message, FailureCode.BAD_REQUEST.code);
    }

    public static void throwUnauthorized(String message) {
        throw new FailureMessageException(message, FailureCode.UNAUTHORIZED.code);
    }

    public static void throwForbidden(String message) {
        throw new FailureMessageException(message, FailureCode.FORBIDDEN.code);
    }

    public static void throwNotFound(String message) {
        throw new FailureMessageException(message, FailureCode.NOT_FOUND.code);
    }

    public static void throwTimeout(String message) {
        throw new FailureMessageException(message, FailureCode.TIMEOUT.code);
    }

    public static void throwConflic(String message) {
        throw new FailureMessageException(message, FailureCode.CONFLICT.code);
    }

    public static void throwGone(String message) {
        throw new FailureMessageException(message, FailureCode.GONE.code);
    }

    public static void throwTooLarge(String message) {
        throw new FailureMessageException(message, FailureCode.TOO_LARGE.code);
    }

    public static void throwInternalError(String message) {
        throw new FailureMessageException(message, FailureCode.INTERNAL_ERROR.code);
    }

    public static void throwNotImplemented(String message) {
        throw new FailureMessageException(message, FailureCode.NOT_IMPLEMENTED.code);
    }

    public static void throwServiceUnavailable(String message) {
        throw new FailureMessageException(message, FailureCode.BAD_REQUEST.code);
    }

    public Integer getCode() {
        return code;
    }

    public String getProtobufMessage() {
        return message;
    }

    @Override
    public String getMessage() {
        return codeToString(code) + ": " + message;
    }

    static String codeToString(Integer code) {
        if (code == FailureCode.BAD_REQUEST.code) {
            return FailureCode.BAD_REQUEST.toString();
        } else if (code == FailureCode.UNAUTHORIZED.code) {
            return FailureCode.UNAUTHORIZED.toString();
        } else if (code == FailureCode.FORBIDDEN.code) {
            return FailureCode.FORBIDDEN.toString();
        } else if (code == FailureCode.NOT_FOUND.code) {
            return FailureCode.NOT_FOUND.toString();
        } else if (code == FailureCode.TIMEOUT.code) {
            return FailureCode.TIMEOUT.toString();
        } else if (code == FailureCode.CONFLICT.code) {
            return FailureCode.CONFLICT.toString();
        } else if (code == FailureCode.GONE.code) {
            return FailureCode.GONE.toString();
        } else if (code == FailureCode.TOO_LARGE.code) {
            return FailureCode.TOO_LARGE.toString();
        } else if (code == FailureCode.INTERNAL_ERROR.code) {
            return FailureCode.INTERNAL_ERROR.toString();
        } else if (code == FailureCode.NOT_IMPLEMENTED.code) {
            return FailureCode.NOT_IMPLEMENTED.toString();
        } else if (code == FailureCode.SERVICE_UNAVAILABLE.code) {
            return FailureCode.SERVICE_UNAVAILABLE.toString();
        } else {
            return Integer.toString(code);
        }
    }

    public static enum FailureCode {
        BAD_REQUEST(1), UNAUTHORIZED(2), FORBIDDEN(3), NOT_FOUND(4), TIMEOUT(5), CONFLICT(6), GONE(
                6), TOO_LARGE(7), INTERNAL_ERROR(8), NOT_IMPLEMENTED(9), SERVICE_UNAVAILABLE(10);
        private final Integer code;

        private FailureCode(Integer code) {
            this.code = code;
        }

        public Integer getCode() {
            return code;
        }
    }

}
