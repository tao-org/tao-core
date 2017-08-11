package src.main.java.ro.cs.tao.common.exceptions;

/**
 * @author Alexandru Ilioiu - 19/07/2017
 */
public abstract class TaoBaseException extends Exception {
    private static final long serialVersionUID = -2105876605890377083L;

    private final String messageID;

    private final String[] parameters;

    /**
     * Constructs an TaoBaseException.
     *
     * @param cause the exception at origin
     * @param messageID the message identifier
     * @param parameters the message parameters
     */
    public TaoBaseException(final Throwable cause, final String messageID, final String... parameters) {
        super(cause);
        this.messageID = messageID;
        this.parameters = parameters;
    }

    /**
     * Constructs an TaoBaseException.
     *
     * @param messageID the message identifier
     * @param parameters the message parameters
     */
    public TaoBaseException(final String messageID, final String... parameters) {
        this.messageID = messageID;
        this.parameters = parameters;
    }

    public String getMessageID() {
        return messageID;
    }

    public String[] getParameters() {
        return parameters;
    }
}
