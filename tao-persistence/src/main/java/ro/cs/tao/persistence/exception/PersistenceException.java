package ro.cs.tao.persistence.exception;

/**
 * Persistence exception
 * @author oana
 *
 */
public class PersistenceException extends Exception{

    public PersistenceException() { super(); }
    public PersistenceException(String message) { super(message); }
    public PersistenceException(String message, Throwable cause) { super(message, cause); }
    public PersistenceException(Throwable cause) { super(cause); }

}
