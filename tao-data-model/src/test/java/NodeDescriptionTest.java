import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ro.cs.tao.topology.NodeDescription;

/**
 * @author Cosmin Cara
 */
public class NodeDescriptionTest extends BaseSerializationTest<NodeDescription> {

    @Override
    protected String referenceJSON() {
        return "{\n" +
                "   \"node\" : {\n" +
                "      \"diskSpace\" : 500,\n" +
                "      \"hostName\" : \"host_sample\",\n" +
                "      \"ipAddress\" : \"10.0.0.1\",\n" +
                "      \"memory\" : 16,\n" +
                "      \"processors\" : 4,\n" +
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
                "</node>";
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        entity = new NodeDescription() {{
            setHostName("host_sample");
            setIpAddr("10.0.0.1");
            setUserName("user");
            setUserPass("drowssap");
            setProcessorCount(4);
            setMemorySizeGB(16);
            setDiskSpaceSizeGB(500);
        }};
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
