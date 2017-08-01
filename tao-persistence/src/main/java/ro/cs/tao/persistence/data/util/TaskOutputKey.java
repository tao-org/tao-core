package ro.cs.tao.persistence.data.util;

import ro.cs.tao.persistence.data.Task;

import java.io.Serializable;

/**
 * Created by oana on 7/28/2017.
 */
public class TaskOutputKey implements Serializable {

    private Task task;
    private String outputName;

    @Override
    public boolean equals(Object o) {
        if (this == o)
        {
            return true;
        }
        if (o == null|| getClass() != o.getClass())
        {
            return false;
        }

        TaskOutputKey that = (TaskOutputKey) o;

        if (task != null ?
          !task.equals(that.task) : that.task !=null)
            return false;
        if (outputName != null ?
          !outputName.equals(that.outputName) : that.outputName !=null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (task != null ? task.hashCode() : 0);
        result = 31 * result + (outputName != null ? outputName.hashCode() : 0);
        return result;
    }
}
