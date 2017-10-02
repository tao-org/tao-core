import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.ParameterType;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.enums.ProcessingComponentVisibility;
import ro.cs.tao.component.execution.ExecutionJob;
import ro.cs.tao.component.execution.ExecutionStatus;
import ro.cs.tao.component.execution.ExecutionTask;
import ro.cs.tao.component.template.BasicTemplate;
import ro.cs.tao.component.template.TemplateType;
import ro.cs.tao.execution.ExecutionsManager;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.data.ExecutionNode;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.topology.NodeDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cosmin on 9/21/2017.
 */
public class ExecutionsManagerTest {
    public static void main(String[] args) throws PersistenceException {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:tao-persistence-context.xml");

        testExecuteTask();
    }

    static void testExecuteTask() throws PersistenceException {

        PersistenceManager persistenceManager = SpringContextBridge.services().getPersistenceManager();

        try {
            persistenceManager.getNodeByHostName("test_hostname");
        } catch (PersistenceException e) {
            NodeDescription node = new NodeDescription();
            node.setActive(Boolean.TRUE);
            node.setDescription("");
            node.setHostName("test_hostname");
            node.setUserName("test");
            node.setUserPass("test");
            persistenceManager.saveExecutionNode(node);
        }

        BasicTemplate template = new BasicTemplate();
        template.setContents("echo $id > /tmp/tao_test.txt");
        //template.setContents("echo $id");

        ProcessingComponent processingComponent = new ProcessingComponent();
        processingComponent.setFileLocation("/usr/bin/echo");
        processingComponent.setMultiThread(Boolean.TRUE);
        processingComponent.setTemplateType(TemplateType.VELOCITY);
        processingComponent.setWorkingDirectory("/tmp/");
        processingComponent.setTemplate(template);
        processingComponent.setTemplateName("testTemplate");
        processingComponent.setLabel("");
        processingComponent.setDescription("");
        processingComponent.setAuthors("");
        processingComponent.setVersion("1.0");
        processingComponent.setCopyright("");
        processingComponent.setVisibility(ProcessingComponentVisibility.USER);

        List<ParameterDescriptor> descriptors = new ArrayList<ParameterDescriptor>() {{

            ParameterDescriptor parameterDescriptor = new ParameterDescriptor("id");
            parameterDescriptor.setDataType(Integer.class);
            parameterDescriptor.setType(ParameterType.REGULAR);
            parameterDescriptor.setDefaultValue("5");
            parameterDescriptor.setDescription("some description");
            parameterDescriptor.setLabel("label");
            parameterDescriptor.setUnit("m");
            parameterDescriptor.setValueSet(new String[]{"1", "2"});
            add(parameterDescriptor);
        }};
        processingComponent.setParameterDescriptors(descriptors);
        if (persistenceManager.getProcessingComponentById(processingComponent.getId()) == null) {
            persistenceManager.saveProcessingComponent(processingComponent);
        }

        ExecutionJob job = new ExecutionJob();
        job.setExecutionStatus(ExecutionStatus.RUNNING);

        ExecutionTask task = new ExecutionTask(processingComponent);
        task.setExecutionNodeHostName("test_hostname");
        task.setParameterValue("id", "1");
        job.addTask(task);

        persistenceManager.saveExecutionJob(job);
        for (int i = 0; i < 100; i++) {
            persistenceManager.saveExecutionTask(task, job);
            ExecutionsManager.getInstance().execute(task);
            task.setId(null);
            task.setResourceId(null);
        }
    }

}
