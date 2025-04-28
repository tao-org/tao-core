package ro.cs.tao.eodata.naming;

/**
 * Represents a resolver capable of resolving and transforming string expressions containing tokens.
 * A token is typically a placeholder or a reference embedded within a string that requires substitution,
 * transformation, or evaluation into a concrete value.
 *
 * This interface defines a single method, {@link #resolve(String)}, which takes a string expression
 * as input and returns the resolved and transformed string result. Implementations of this interface
 * are responsible for defining the specific token patterns they support and the logic for processing them.
 *
 * Example use cases include substituting placeholders in a string with computed values,
 * or transforming specific substrings based on predefined rules.
 *
 * Thread Safety:
 * Implementations of this interface are expected to be thread-safe unless otherwise stated.
 *
 * Error Handling:
 * Implementations may handle invalid or malformed input gracefully, returning the input unchanged
 * or logging warnings/errors depending on specific resolver behavior.
 *
 * Responsibilities:
 * - Define token patterns to be resolved (e.g., using regular expressions).
 * - Substitute or transform detected tokens within an input string.
 * - Ensure flexibility to handle a variety of string formats as required.
 */
public interface TokenResolver {

    /**
     * Resolves the given string expression by replacing or transforming its tokens based on
     * the defined resolvers. The method ensures that the expression is processed using
     * all registered {@code TokenResolver} implementations and returns the final resolved string.
     *
     * @param expression The string expression to be resolved, potentially containing tokens
     *                   or placeholders that need to be replaced or transformed.
     * @return The resolved string after applying all token substitutions and transformations
     *         using the available resolvers.
     */
    String resolve(final String expression);

}
