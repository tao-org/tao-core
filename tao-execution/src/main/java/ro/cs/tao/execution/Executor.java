package ro.cs.tao.execution;

import ro.cs.tao.component.Identifiable;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.execution.ExecutionTask;

/**
 * Created by cosmin on 9/12/2017.
 */
public abstract class Executor extends Identifiable {
    public abstract void initialize() throws ExecutionException ;
    public abstract void close() throws ExecutionException ;
    public abstract boolean supports(TaoComponent component);
    public abstract void execute(ExecutionTask component) throws ExecutionException;
    public abstract void stop(ExecutionTask component) throws ExecutionException;
    public abstract void suspend(ExecutionTask component) throws ExecutionException;
    public abstract void resume(ExecutionTask component) throws ExecutionException;
}
