import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.ToolCommandsTokens;
import ro.cs.tao.topology.TopologyManager;

import java.util.List;

/**
 * Created by cosmin on 8/30/2017.
 */
public class TopologyTest {

    public static void main(String[] args) {
        testAddTopologyNode();
    }


    private static void testAddTopologyNode() {
        List<String> tokens = ToolCommandsTokens.getDefinedTokensList();
        System.out.println(tokens);
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
}
