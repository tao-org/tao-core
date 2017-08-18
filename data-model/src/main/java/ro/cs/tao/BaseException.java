package ro.cs.tao;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class BaseException extends Throwable {
    private Map<String, Object> additionalInfo;

    public BaseException() {
        super();
    }

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
        addAdditionalInfo(cause.getClass().getSimpleName(), cause.getMessage());
    }

    public BaseException(Throwable cause) {
        super(cause);
    }

    public Map<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }

    public void addAdditionalInfo(String key, Object info) {
        if (this.additionalInfo == null) {
            this.additionalInfo = new HashMap<>(2);
        }
        this.additionalInfo.put(key, info);
    }
}
