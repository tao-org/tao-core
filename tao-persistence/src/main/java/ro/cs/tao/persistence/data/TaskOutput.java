package ro.cs.tao.persistence.data;

import ro.cs.tao.persistence.data.util.TaskOutputKey;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by oana on 7/27/2017.
 */
@Entity
@Table(name = "tao.task_output")
@IdClass(TaskOutputKey.class)
public class TaskOutput {

    /**
     * Task output name column maximum length
     */
    private static final int TASK_OUTPUT_NAME_COLUMN_MAX_LENGTH = 250;

    /**
     * Task output value column maximum length
     */
    private static final int TASK_OUTPUT_VALUE_COLUMN_MAX_LENGTH = 500;

    /**
     * Task to which this output belongs to
     */
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="task_id", nullable = false)
    private Task task;

    /**
     * Task output name
     */
    @Id
    @Column(name = "output_name")
    @NotNull
    @Size(min = 1, max = TASK_OUTPUT_NAME_COLUMN_MAX_LENGTH)
    private String outputName;

    /**
     * Task output value
     */
    @Column(name = "output_value")
    @NotNull
    @Size(min = 1, max = TASK_OUTPUT_VALUE_COLUMN_MAX_LENGTH)
    private String outputValue;

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    public String getOutputValue() {
        return outputValue;
    }

    public void setOutputValue(String outputValue) {
        this.outputValue = outputValue;
    }
}
