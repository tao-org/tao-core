/**
 * 
 */
package ro.cs.tao.execution.quota;

import ro.cs.tao.BaseException;

/**
 * @author Lucian Barbulescu
 */
public class QuotaException extends BaseException {

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
