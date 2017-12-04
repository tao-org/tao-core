package ro.cs.tao.persistence.data;
//
//import ro.cs.tao.persistence.data.util.TaskInputKey;
//
//import javax.persistence.*;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;
//import java.io.Serializable;
//
///**
// * TaskInput persistent entity
// *
// * @author oana
// *
// */
//@Entity
//@Table(name = "tao.task_input")
//@IdClass(TaskInputKey.class)
//public class TaskInput implements Serializable {
//
//    /**
//     * Task input name column maximum length
//     */
//    private static final int TASK_INPUT_NAME_COLUMN_MAX_LENGTH = 250;
//
//    /**
//     * Task input value column maximum length
//     */
//    private static final int TASK_INPUT_VALUE_COLUMN_MAX_LENGTH = 500;
//
//    /**
//     * Task to which this input belongs to
//     */
//    @Id
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name="task_id", nullable = false)
//    private Task task;
//
//    /**
//     * Task input name
//     */
//    @Id
//    @Column(name = "input_name")
//    @NotNull
//    @Size(min = 1, max = TASK_INPUT_NAME_COLUMN_MAX_LENGTH)
//    private String inputName;
//
//    /**
//     * Task input value
//     */
//    @Column(name = "input_value")
//    @NotNull
//    @Size(min = 1, max = TASK_INPUT_VALUE_COLUMN_MAX_LENGTH)
//    private String inputValue;
//
//    public Task getTask() {
//        return task;
//    }
//
//    public void setTask(Task task) {
//        this.task = task;
//    }
//
//    public String getInputName() {
//        return inputName;
//    }
//
//    public void setInputName(String inputName) {
//        this.inputName = inputName;
//    }
//
//    public String getInputValue() {
//        return inputValue;
//    }
//
//    public void setInputValue(String inputValue) {
//        this.inputValue = inputValue;
//    }
//}
