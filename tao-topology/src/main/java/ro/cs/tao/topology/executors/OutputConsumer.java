package ro.cs.tao.topology.executors;

/**
 * @author Cosmin Cara
 */
public interface OutputConsumer {
    default void consume(String message) { System.out.println(message); }
}
