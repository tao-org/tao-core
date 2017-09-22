package ro.cs.tao.execution;

import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.execution.ExecutionTask;
import ro.cs.tao.persistence.data.ProcessingComponent;

/**
 * Created by cosmin on 9/12/2017.
 */
public interface IExecutor {
    void initialize() throws ExecutionException ;
    void close() throws ExecutionException ;
    boolean supports(TaoComponent component);
    void execute(ExecutionTask component) throws ExecutionException;
    void stop(ExecutionTask component) throws ExecutionException;
    void suspend(ExecutionTask component) throws ExecutionException;
    void resume(ExecutionTask component) throws ExecutionException;
}
