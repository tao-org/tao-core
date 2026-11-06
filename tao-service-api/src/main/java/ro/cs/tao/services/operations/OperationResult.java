package ro.cs.tao.services.operations;

import java.util.logging.Logger;

/**
 * Represents the result of an operation, encapsulating success status,
 * message, data, error code, and replay status.
 */
public class OperationResult {
    private static final Logger logger = Logger.getLogger(OperationResult.class.getName());
    private boolean success;
    private String message;
    private Object data;
    private String errorCode;
    private boolean replayed;

    public OperationResult() {
    }

    public static OperationResult ok(Object data) {
        OperationResult result = new OperationResult();
        result.success = true;
        result.data = data;
        return result;
    }

    public static OperationResult empty() {
        OperationResult result = new OperationResult();
        result.success = true;
        result.data = "";
        return result;
    }

    public static OperationResult fail(String errorCode, String message) {
        OperationResult result = new OperationResult();
        result.success = false;
        result.errorCode = errorCode;
        result.message = message;
        logger.warning(errorCode + " " + message);
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isReplayed() {
        return replayed;
    }

    public void setReplayed(boolean replayed) {
        this.replayed = replayed;
    }
}

