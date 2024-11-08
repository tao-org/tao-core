import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.NodeFlavor;
import ro.cs.tao.topology.ToolCommandsTokens;
import ro.cs.tao.topology.TopologyManager;

import java.util.List;

//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.mockito.runners.MockitoJUnitRunner;
//import org.testng.annotations.BeforeMethod;

/**
 * Created by cosmin on 8/30/2017.
 */
//@RunWith(MockitoJUnitRunner.class)
@ImportResource({"classpath*:META-INF/persistence.xml" })
public class TopologyTest {

    private static Log logger = LogFactory.getLog(TopologyTest.class);
/*    @Mock
    private PersistenceManager persistenceMng;

    @BeforeMethod
    public void initMocks(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddNodeDescription() {
        NodeDescription masterInfo = new NodeDescription();
        masterInfo.setHostName("master.testtorque.ro");
        TopologyManager.getInstance().setMasterNodeInfo(masterInfo);

        NodeDescription nodeInfo = new NodeDescription();
        nodeInfo.setHostName("node01.testtorque.ro");
        nodeInfo.setUserName("sen2agri");
        nodeInfo.setUserPass("sen2agri");
        nodeInfo.setProcessorCount(2);
        TopologyManager.getInstance().add(nodeInfo);
    }
*/

    public static void main(String[] args) {
        testAddTopologyNode();
    }


    private static void testAddTopologyNode() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:tao-topology-context.xml");

        List<String> tokens = ToolCommandsTokens.getDefinedTokensList();
        System.out.println(tokens);
        NodeDescription masterInfo = new NodeDescription();
        masterInfo.setId("master.testtorque.ro");
        try {
            TopologyManager.setMasterNode(masterInfo);
        } catch(Exception e) {
            logger.error(e.getMessage());
        }


        NodeDescription nodeInfo = new NodeDescription();
        nodeInfo.setId("node01.testtorque.ro");
        nodeInfo.setUserName("sen2agri");
        nodeInfo.setUserPass("sen2agri");
        NodeFlavor flavor = new NodeFlavor();
        flavor.setCpu(2);
        flavor.setMemory(1024);
        flavor.setDisk(120);
        flavor.setRxtxFactor(1.0f);
        nodeInfo.setFlavor(flavor);
        TopologyManager.getInstance().addNode(nodeInfo);

        // close application context
        context.close();
    }

}
