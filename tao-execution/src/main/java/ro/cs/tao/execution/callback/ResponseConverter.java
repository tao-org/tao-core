package ro.cs.tao.execution.callback;

import org.apache.http.NameValuePair;

import java.util.List;

/**
 * An interface for converting a list of {@link NameValuePair} parameters into
 * a specific format intended for communication with external systems. The actual
 * conversion logic is delegated to implementations of this interface, allowing
 * different protocols or data representations to be handled.
 *
 * This is commonly utilized in the context of custom callback mechanisms to
 * transform parameters before they are sent to external endpoints.
 *
 * Implementations of this interface should ensure thread safety if shared
 * across multiple threads.
 */
public interface ResponseConverter {
    /**
     * Converts a list of {@link NameValuePair} objects into a string representation
     * according to the implementation's specific format.
     * This method is typically used to prepare parameters for communication with
     * external systems.
     *
     * @param parameters A list of {@link NameValuePair} objects representing key-value pairs
     *                   to be converted into a specific formatted string.
     * @return A formatted string resulting from the conversion of the input parameters.
     *         The format is determined by the implementation.
     */
    String convert(List<NameValuePair> parameters);
}
