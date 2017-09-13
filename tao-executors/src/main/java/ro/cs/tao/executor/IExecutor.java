package ro.cs.tao.executor;

import ro.cs.tao.component.ProcessingComponent;

/**
 * Created by cosmin on 9/12/2017.
 */
public interface IExecutor {
    void executeComponent(ProcessingComponent component) throws ExecutionException;
    void stopExecution(ProcessingComponent component) throws ExecutionException;
}
