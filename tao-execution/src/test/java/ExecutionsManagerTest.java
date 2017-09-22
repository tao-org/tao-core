import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.ParameterType;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.execution.ExecutionTask;
import ro.cs.tao.component.template.BasicTemplate;
import ro.cs.tao.execution.ExecutionsManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cosmin on 9/21/2017.
 */
public class ExecutionsManagerTest {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:tao-persistence-context.xml");

        testExecuteTask();
    }

    static void testExecuteTask() {
        BasicTemplate template = new BasicTemplate();
        template.setContents("echo #foreach($a in $id)$a #end");

        ProcessingComponent processingComponent = new ProcessingComponent();
        processingComponent.setTemplate(template);
        processingComponent.setTemplateName("testTemplate");
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
        ExecutionTask task = new ExecutionTask(processingComponent);
        ExecutionsManager.getInstance().execute(task);
    }

}
