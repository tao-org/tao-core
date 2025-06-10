package ro.cs.tao.eodata.naming;

import java.util.ArrayList;
import java.util.List;

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
