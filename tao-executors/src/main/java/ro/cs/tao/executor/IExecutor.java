package ro.cs.tao.executor;

import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.TaoComponent;

/**
 * Created by cosmin on 9/12/2017.
 */
public interface IExecutor {
    void initialize() throws ExecutionException ;
    void close() throws ExecutionException ;
    boolean supports(TaoComponent component);
    void executeComponent(TaoComponent component) throws ExecutionException;
    void stopExecution(TaoComponent component) throws ExecutionException;
}
