package ro.cs.tao.topology;

import ro.cs.tao.utils.executors.OutputConsumer;

/**
 * Created by cosmin on 8/24/2017.
 */
public class StepExecutionOutputConsumer implements OutputConsumer {
    private ToolInstallStep step;
    public StepExecutionOutputConsumer(ToolInstallStep step) {
        this.step = step;
    }
    public void consume(String message) {
        System.out.println(message);
        step.addExecutionMessage(message);
    }
}
