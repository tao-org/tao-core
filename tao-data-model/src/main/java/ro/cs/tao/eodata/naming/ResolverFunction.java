package ro.cs.tao.eodata.naming;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a function that can be utilized during token resolution processes or parsed operations.
 * This class encapsulates details about a function, including its name, return type, description,
 * category, and the list of arguments it accepts.
 *
 * ResolverFunction serves as a descriptive model for functions that can be supported by
 * {@code TokenResolver} implementations or other related components.
 *
 * Responsibilities:
 * - Define metadata about a function, such as its name, return type, category, and description.
 * - Maintain a list of arguments required by the function.
 * - Provide mechanisms to add new arguments programmatically.
 *
 * Thread Safety:
 * - This class is not inherently thread-safe. When accessing or modifying its state from multiple threads,
 *   external synchronization is required.
 *
 * Examples of Use:
 * - Extending the functionality of token resolvers by representing functions callable within expressions.
 * - Defining a catalog of resolvable or executable functions in a given runtime or parser.
 */
public class ResolverFunction {
    private final String category;
    private final String name;
    private final String returnType;
    private final String description;
    private final List<Argument> arguments;

    public ResolverFunction(String name, String returnType, String description, String category) {
        this.name = name;
        this.returnType = returnType;
        this.description = description;
        this.category = category;
        this.arguments = new ArrayList<>();
    }

    public String getCategory() {
        return category;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public String getName() {
        return name;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getDescription() {
        return description;
    }

    public void addArgument(Argument argument) {
        this.arguments.add(argument);
    }

    public void addArgument(String name, String type, String description) {
        this.arguments.add(new Argument(name, type, description));
    }

    private static class Argument {
        private final String name;
        private final String type;
        private final String description;

        public Argument(String name, String type, String description) {
            this.description = description;
            this.name = name;
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }
}
