import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ro.cs.tao.serialization.MediaType;
import ro.cs.tao.serialization.Serializer;
import ro.cs.tao.serialization.SerializerFactory;
import ro.cs.tao.topology.NodeDescription;

import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;

/**
 * @author Cosmin Cara
 */
public class SerializationTest {

    private final static String json = "{\n" +
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
    private final static String xml = "<node>\n" +
            "    <diskSpace>500</diskSpace>\n" +
            "    <hostName>host_sample</hostName>\n" +
            "    <ipAddress>10.0.0.1</ipAddress>\n" +
            "    <memory>16</memory>\n" +
            "    <processors>4</processors>\n" +
            "    <userName>user</userName>\n" +
            "    <password>drowssap</password>\n" +
            "</node>";
    private NodeDescription nodeDescription;

    @Before
    public void setUp() throws Exception {
        nodeDescription = new NodeDescription() {{
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
    public void serializeToJSON() throws Exception {
        Serializer<NodeDescription, String> serializer = SerializerFactory.create(NodeDescription.class, MediaType.JSON);
        String output = serializer.serialize(nodeDescription);
        Assert.assertEquals(json.replaceAll("[\n\r]",""), output.replaceAll("[\n\r]",""));
        System.out.println("JSON: " + output);
    }

    @Test
    public void serializeToXML() throws Exception {
        Serializer<NodeDescription, String> serializer = SerializerFactory.create(NodeDescription.class, MediaType.XML);
        String output = serializer.serialize(nodeDescription);
        Assert.assertEquals(xml.replaceAll("[\n\r]",""), output.replaceAll("[\n\r]",""));
        System.out.println("XML: " + output);
    }

    @Test
    public void deserializeFromJSON() throws Exception {
        // JSON
        Serializer<NodeDescription, String> serializer = SerializerFactory.create(NodeDescription.class, MediaType.JSON);
        NodeDescription node = serializer.deserialize(new StreamSource(new StringReader(json)));
        Assert.assertEquals("host_sample", node.getHostName());
    }

    @Test
    public void deserializeFromXML() throws Exception {
        Serializer<NodeDescription, String> serializer = SerializerFactory.create(NodeDescription.class, MediaType.XML);
        NodeDescription node = serializer.deserialize(new StreamSource(new StringReader(xml)));
        Assert.assertEquals("host_sample", node.getHostName());
    }
}
