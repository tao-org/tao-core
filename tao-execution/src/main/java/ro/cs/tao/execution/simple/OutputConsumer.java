package ro.cs.tao.execution.simple;

/**
 * @author Cosmin Cara
 */
public interface OutputConsumer {
    default void consume(String message) { System.out.println(message); }
}
