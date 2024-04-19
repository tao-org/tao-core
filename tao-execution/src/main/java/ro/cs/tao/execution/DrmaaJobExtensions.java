package ro.cs.tao.execution;

/**
 * Holder for DRMAA Job Template extension attribute names.
 *
 * @author  Cosmin Cara
 * @since   1.2
 */
public class DrmaaJobExtensions {

    public static final String MEMORY_REQUIREMENTS_ATTRIBUTE = "MemoryRequirements";
    public static final String NODE_ATTRIBUTE = "NODE";
    public static final String CONTAINER_ATTRIBUTE = "ContainterInfo";
    public static final String SIMULATE_EXECUTION_ATTRIBUTE = "SimulateExecution";
    public static final String TASK_NAME = "TaskName";
    public static final String TASK_ID = "TaskId";
    public static final String TASK_ANCESTOR_ID = "ParentId";
    public static final String TASK_ANCESTOR_OUTPUT = "ParentOutput";
    public static final String TASK_OUTPUT = "TaskOutput";
    public static final String IS_TERMINAL_TASK = "TerminalTask";
    public static final String JOB_ID = "JobId";
    public static final String USER = "User";
    public static final String SCRIPT_FORMAT = "ScriptFormat";
    public static final String SCRIPT_PATH = "ScriptPath";
}
