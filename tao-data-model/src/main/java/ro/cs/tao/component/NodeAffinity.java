package ro.cs.tao.component;

import javax.annotation.Nonnull;
import java.util.Objects;

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
