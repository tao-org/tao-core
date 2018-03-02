package ro.cs.tao.component.execution;

public interface StatusChangeListener {
    default void statusChanged(ExecutionTask changedTask) { }
}
