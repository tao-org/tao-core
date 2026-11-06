package ro.cs.tao.component;

import jakarta.annotation.Nonnull;
import java.util.Objects;

/**
 * Class representing node affinity, which specifies constraints or preferences
 * about the placement or execution of tasks or entities in relation to nodes.
 * Node affinity can determine how tasks interact with the underlying system or infrastructure.
 *
 * This class provides predefined affinities, such as {@code Any} and {@code SameNode},
 * and allows the creation of custom affinities.
 *
 * Instances of this class are immutable.
 */
public final class NodeAffinity {
    public static final NodeAffinity Any = new NodeAffinity("Any");
    public static final NodeAffinity SameNode = new NodeAffinity("Same");

    private final String value;

    public static NodeAffinity of(@Nonnull String value) {
        switch (value) {
            case "Any":
                return Any;
            case "Same":
                return SameNode;
            default:
                return new NodeAffinity(value);
        }
    }

    private NodeAffinity(@Nonnull String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof NodeAffinity) {
            return Objects.equals(this.value, ((NodeAffinity) obj).value);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.value;
    }
}

