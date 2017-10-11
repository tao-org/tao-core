import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.NodeServiceStatus;
import ro.cs.tao.topology.ServiceDescription;
import ro.cs.tao.topology.ServiceStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Cara
 */
public class NodeDescriptionTest extends BaseSerializationTest<NodeDescription> {

    @Override
    protected String referenceJSON() {
        return "{\n" +
                "   \"node\" : {\n" +
                "      \"active\" : true,\n" +
                "      \"diskSpace\" : 500,\n" +
                "      \"hostName\" : \"host_sample\",\n" +
                "      \"memory\" : 16,\n" +
                "      \"processors\" : 4,\n" +
                "      \"services\" : {\n" +
                "         \"servicesStatus\" : [ {\n" +
                "            \"service\" : {\n" +
                "               \"description\" : \"Docker description\",\n" +
                "               \"name\" : \"Docker\",\n" +
                "               \"version\" : \"1.9\"\n" +
                "            },\n" +
                "            \"status\" : \"INSTALLED\"\n" +
                "        }, {\n" +
                "            \"service\" : {\n" +
                "               \"description\" : \"Torque CRM\",\n" +
                "               \"name\" : \"Torque\",\n" +
                "               \"version\" : \"1.5\"\n" +
                "            },\n" +
                "            \"status\" : \"NOT_FOUND\"\n" +
                "        } ]\n" +
                "    },\n" +
                "      \"userName\" : \"user\",\n" +
                "      \"password\" : \"drowssap\"\n" +
                "   }\n" +
                "}";
    }

    @Override
    protected String referenceXML() {
        return "<node>\n" +
                "   <diskSpace>500</diskSpace>\n" +
                "   <hostName>host_sample</hostName>\n" +
                "   <ipAddress>10.0.0.1</ipAddress>\n" +
                "   <memory>16</memory>\n" +
                "   <processors>4</processors>\n" +
                "   <userName>user</userName>\n" +
                "   <password>drowssap</password>\n" +
                "   <services>\n" +
                "      <servicesStatus>" +
                "         <service>\n" +
                "            <name>Docker</name>\n" +
                "            <version>1.9</version>\n" +
                "            <description>Docker description</description>\n" +
                "         </service>\n" +
                "         <status>INSTALLED</status>\n" +
                "      </servicesStatus>\n" +
                "      <servicesStatus>" +
                "         <service>\n" +
                "            <name>Torque</name>\n" +
                "            <version>1.5</version>\n" +
                "            <description>Torque CRM</description>\n" +
                "         </service>\n" +
                "         <status>NOT_FOUND</status>\n" +
                "      </servicesStatus>\n" +
                "   </services>\n" +
                "</node>";
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        entity = new NodeDescription();
        entity.setHostName("host_sample");
        entity.setUserName("user");
        entity.setUserPass("drowssap");
        entity.setProcessorCount(4);
        entity.setMemorySizeGB(16);
        entity.setDiskSpaceSizeGB(500);
        List<NodeServiceStatus> servicesStatus = new ArrayList<>();
        servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Docker", "1.9", "Docker description"), ServiceStatus.INSTALLED));
        servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Torque", "1.5", "Torque CRM"), ServiceStatus.NOT_FOUND));
        entity.setServicesStatus(servicesStatus);
    }

    @Test
    public void deserializeFromJSON() throws Exception {
        NodeDescription object = deserializeJson();
        Assert.assertEquals("host_sample", object.getHostName());
    }

    @Test
    public void deserializeFromXML() throws Exception {
        NodeDescription object = deserializeXml();
        Assert.assertEquals("host_sample", object.getHostName());
    }

}
