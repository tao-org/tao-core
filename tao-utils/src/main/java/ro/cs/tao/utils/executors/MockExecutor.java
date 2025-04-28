package ro.cs.tao.utils.executors;

import java.util.List;

/**
 * The MockExecutor class is a mock implementation of the Executor class.
 * It is designed to simulate the behavior of executing commands without
 * performing any real action. This mock implementation is useful for testing
 * purposes or cases where actual execution is not required.
 */
public class MockExecutor extends Executor<Object> {

    MockExecutor(String host, List<String> args, boolean asSU) {
        super(host, args, asSU);
    }

    MockExecutor(String host, List<String> args, boolean asSU, boolean ensureTokenizedArgs) {
        super(host, args, asSU, ensureTokenizedArgs);
    }

    @Override
    public boolean canConnect() {
        return true;
    }

    @Override
    public int execute(boolean logMessages) throws Exception {
        this.logger.fine("[" + this.host + "] " + String.join(" ", formatArguments()));
        return 0;
    }

}
