import org.apache.commons.lang3.SystemUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.enums.ParameterType;
import ro.cs.tao.component.enums.ProcessingComponentVisibility;
import ro.cs.tao.component.template.BasicTemplate;
import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.template.TemplateType;
import ro.cs.tao.execution.ExecutionsManager;
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.execution.model.ProcessingExecutionTask;
import ro.cs.tao.execution.persistence.ExecutionJobProvider;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.persistence.NodeDBProvider;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.ProcessingComponentProvider;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.topology.NodeDescription;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by cosmin on 9/21/2017.
 */
public class ExecutionsManagerTest {
    private static final String PING_WIN_CMD = "ping -t localhost";
    private static final String PING_LIN_CMD = "ping localhost";
    private static Logger logger = Logger.getLogger(ExecutionsManagerTest.class.getName());
    public static void main(String[] args) throws PersistenceException {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:tao-spring-bridge-context.xml");

        testExecuteTask();
        testSuspendResumeStopTask();
    }

    static void testExecuteTask() throws PersistenceException {

        ExecutionTaskProvider taskProvider = SpringContextBridge.services().getService(ExecutionTaskProvider.class);
        ExecutionJobProvider jobProvider = SpringContextBridge.services().getService(ExecutionJobProvider.class);

        // Add an execution node if it does not exist
        createNode("test_hostname");

        Template template = createTemplate("echo $id > /tmp/tao_test.txt");
        ProcessingComponent processingComponent = createProcessingComponent(template);
        addDescriptorToProcessingComponent(processingComponent, "id", new String[]{"1", "2"}, "2");
        saveProcessingComponent(processingComponent);

        ExecutionJob job = new ExecutionJob();
        job.setExecutionStatus(ExecutionStatus.RUNNING);
        job.setWorkflowId(1L);
        job.setUserId("admin");

        ExecutionTask task = creatTask(processingComponent, "test_hostname",
            Collections.unmodifiableMap(Stream.of(
                    new AbstractMap.SimpleEntry<>("id", "1")).
                    collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue))));
        job.addTask(task);

        jobProvider.save(job);
        for (int i = 0; i < 10; i++) {
            taskProvider.save(task, job);
            ExecutionsManager.getInstance().execute(task);
            // reset the parameters of the task object in order to be used again
            task = creatTask(processingComponent, "test_hostname",
                    Collections.unmodifiableMap(Stream.of(
                            new AbstractMap.SimpleEntry<>("id", "1")).
                            collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue))));
            job.addTask(task);
            jobProvider.update(job);
        }
        int curRunningTasks = taskProvider.listRunning().size();
        while(curRunningTasks > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<ExecutionTask> runningTasks = taskProvider.listRunning();
            curRunningTasks = runningTasks.size();
        }
    }

    static void testSuspendResumeStopTask() throws PersistenceException {

        ExecutionTaskProvider taskProvider = SpringContextBridge.services().getService(ExecutionTaskProvider.class);
        ExecutionJobProvider jobProvider = SpringContextBridge.services().getService(ExecutionJobProvider.class);

        // Add an execution node if it does not exist
        createNode("test_hostname");

        Template template = createTemplate(SystemUtils.IS_OS_WINDOWS ?
                PING_WIN_CMD : PING_LIN_CMD);
        ProcessingComponent processingComponent = createProcessingComponent(template);
        //addDescriptorToProcessingComponent(processingComponent, "id", new String[]{"1", "2"}, "2");
        saveProcessingComponent(processingComponent);

        ExecutionJob job = new ExecutionJob();
        job.setExecutionStatus(ExecutionStatus.RUNNING);
        job.setWorkflowId(1L);
        job.setUserId("admin");

        ExecutionTask task = creatTask(processingComponent, "test_hostname", new HashMap<>());
        job.addTask(task);
        jobProvider.save(job);

        taskProvider.save(task, job);
        ExecutionsManager.getInstance().execute(task);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ExecutionsManager.getInstance().suspend(task);
        // Get from the database the task
        task = taskProvider.get(task.getId());
        if(task.getExecutionStatus() != ExecutionStatus.SUSPENDED) {
            logger.info("Suspend failed!");
        } else {
            logger.info("Suspend succeeded!");
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ExecutionsManager.getInstance().resume(task);

        if(task.getExecutionStatus() != ExecutionStatus.RUNNING) {
            logger.info("Resume failed!");
        } else {
            logger.info("Resume succeeded!");
        }

        ExecutionsManager.getInstance().stop(task);

        boolean stopOk = false;
        for(int i = 0; i<20; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Get from the database the task
            task = taskProvider.get(task.getId());
            ExecutionStatus status = task.getExecutionStatus();
            if(status == ExecutionStatus.FAILED) {
                logger.info("Stop succeeded!");
                stopOk = true;
                break;
            }
        }
        if (!stopOk) {
            logger.info("Stop failed!");
        }

    }

    private static NodeDescription createNode(String nodeName) throws PersistenceException {
        NodeDBProvider nodeDBProvider = SpringContextBridge.services().getService(NodeDBProvider.class);

        NodeDescription node = new NodeDescription();
        node.setActive(Boolean.TRUE);
        node.setDescription("");
        node.setId(nodeName);
        node.setUserName("test");
        node.setUserPass("test");

        try {
            nodeDBProvider.update(node);
        } catch (Exception e) {
            nodeDBProvider.save(node);
        }
        return node;
    }

    private static Template createTemplate(String cmd) {
        BasicTemplate template = new BasicTemplate();
        template.setContents(cmd);
        return template;
    }

    private static ProcessingComponent createProcessingComponent(Template template) {
        ProcessingComponent processingComponent = new ProcessingComponent();
        processingComponent.setFileLocation("/usr/bin/echo");
        processingComponent.setMultiThread(Boolean.TRUE);
        processingComponent.setTemplateType(TemplateType.VELOCITY);
        processingComponent.setWorkingDirectory("/tmp/");
        processingComponent.setTemplate(template);
        processingComponent.setLabel("");
        processingComponent.setDescription("");
        processingComponent.setAuthors("");
        processingComponent.setVersion("1.0");
        processingComponent.setCopyright("");
        processingComponent.setActive(true);
        processingComponent.setVisibility(ProcessingComponentVisibility.USER);
        /*processingComponent.setSourceCardinality(0);
        processingComponent.setTargetCardinality(0);*/
        processingComponent.setContainerId("container01");

        return processingComponent;
    }

    private static void addDescriptorToProcessingComponent(ProcessingComponent component, String descrId,
                                                           String[] valueSet, String defaultVal) {
        ParameterDescriptor parameterDescriptor = new ParameterDescriptor(descrId);
        parameterDescriptor.setDataType(Integer.class);
        parameterDescriptor.setType(ParameterType.REGULAR);
        parameterDescriptor.setDefaultValue(defaultVal);
        parameterDescriptor.setDescription("some description");
        parameterDescriptor.setLabel("label");
        parameterDescriptor.setUnit("m");
        parameterDescriptor.setValueSet(valueSet);

        Set<ParameterDescriptor> descriptors = component.getParameterDescriptors();
        descriptors.add(parameterDescriptor);
        component.setParameterDescriptors(descriptors);
    }

    private static void saveProcessingComponent(ProcessingComponent component) throws PersistenceException {
        ProcessingComponentProvider componentProvider = SpringContextBridge.services().getService(ProcessingComponentProvider.class);
        try {
            componentProvider.update(component);
        } catch (PersistenceException e) {
            componentProvider.save(component);
        }
    }

    private static ProcessingExecutionTask creatTask(ProcessingComponent processingComponent, String hostName, Map<String, String> values) {
        ProcessingExecutionTask task = new ProcessingExecutionTask();
        task.setComponent(processingComponent);
        task.setExecutionNodeHostName(hostName);
        task.setWorkflowNodeId(1L);
        values.forEach(task::setInputParameterValue);

        return task;
    }

}
