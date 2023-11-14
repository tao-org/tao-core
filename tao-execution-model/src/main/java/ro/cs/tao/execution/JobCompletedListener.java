package ro.cs.tao.execution;

import ro.cs.tao.execution.model.ExecutionJob;

public interface JobCompletedListener {
    void onCompleted(ExecutionJob job);
}
