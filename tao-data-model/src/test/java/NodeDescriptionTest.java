import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ro.cs.tao.topology.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Cara
 */
public class NodeDescriptionTest extends BaseSerializationTest<NodeDescription> {

    @Override
    protected String referenceJSON() {
        return "{\"id\":\"host_sample\",\"servicesStatus\":[{\"service\":{\"name\":\"Docker\",\"version\":\"1.9\",\"description\":\"Docker description\"},\"status\":1},{\"service\":{\"name\":\"Torque\",\"version\":\"1.5\",\"description\":\"Torque CRM\"},\"status\":0}],\"tags\":null,\"userName\":\"user\",\"password\":\"drowssap\",\"flavor\":{\"id\":\"test\",\"cpu\":4,\"memory\":4096,\"disk\":500,\"swap\":0,\"rxtxFactor\":0.0},\"description\":null,\"role\":\"WORKER\",\"active\":true,\"volatile\":false}";
    }

    @Override
    protected String referenceXML() {
        return "<node><id>host_sample</id><services><servicesStatus><service><name>Docker</name><version>1.9</version><description>Docker description</description></service><status>1</status></servicesStatus><servicesStatus><service><name>Torque</name><version>1.5</version><description>Torque CRM</description></service><status>0</status></servicesStatus></services><userName>user</userName><password>drowssap</password><flavor><id>test</id><cpu>4</cpu><memory>4096</memory><disk>500</disk><swap>0</swap><rxtxFactor>0.0</rxtxFactor></flavor><description/><role>WORKER</role><active>true</active><volatile>false</volatile></node>";
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        entity = new NodeDescription();
        entity.setId("host_sample");
        entity.setUserName("user");
        entity.setUserPass("drowssap");
        NodeFlavor flavor = new NodeFlavor();
        flavor.setId("test");
        flavor.setCpu(4);
        flavor.setMemory(4096);
        flavor.setDisk(500);
        entity.setFlavor(flavor);
        entity.setActive(false);
        entity.setRole(NodeRole.WORKER);
        List<NodeServiceStatus> servicesStatus = new ArrayList<>();
        servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Docker", "1.9", "Docker description"), ServiceStatus.INSTALLED));
        servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Torque", "1.5", "Torque CRM"), ServiceStatus.NOT_FOUND));
        entity.setServicesStatus(servicesStatus);
    }

    @Test
    public void deserializeFromJSON() throws Exception {
        NodeDescription object = deserializeJson();
        Assert.assertEquals("host_sample", object.getId());
    }

    @Test
    public void deserializeFromXML() throws Exception {
        NodeDescription object = deserializeXml();
        Assert.assertEquals("host_sample", object.getId());
    }

}
