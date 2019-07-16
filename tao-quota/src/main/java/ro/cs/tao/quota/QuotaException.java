/**
 * 
 */
package ro.cs.tao.quota;

/**
 * Quota related exceptions.
 * 
 * @author Lucian Barbulescu
 */
public class QuotaException extends Exception {

	public QuotaException() {
		super();
	}

	public QuotaException(String message, Throwable cause) {
		super(message, cause);
	}

	public QuotaException(String message) {
		super(message);
	}

	public QuotaException(Throwable cause) {
		super(cause);
	}

}
