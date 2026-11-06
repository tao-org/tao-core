package ro.cs.tao.component;

import ro.cs.tao.BaseException;

/**
 * Represents an exception that occurs during the aggregation process.
 * This exception is a specialization of the BaseException class, allowing
 * for customized error messages and additional contextual information.
 */
public class AggregationException extends BaseException {

    public AggregationException(String message) {
        super(message);
    }
}
