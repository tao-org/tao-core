import org.junit.Before;
import org.junit.Test;
import ro.cs.tao.serialization.MediaType;
import ro.cs.tao.serialization.Serializer;
import ro.cs.tao.serialization.SerializerFactory;

import java.lang.reflect.ParameterizedType;

/**
 * @author Cosmin Cara
 */
public abstract class BaseSerializationTest<T> {
    protected Class<T> entityClass;
    protected T entity;
    protected String entityJSON;
    protected String entityXML;

    public BaseSerializationTest() {
        this.entityJSON = referenceJSON();
        this.entityXML = referenceXML();
    }

    @Before
    public void setUp() throws Exception {
        this.entityClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.entity = this.entityClass.newInstance();
    }

    @Test
    public void serializeToJSON() throws Exception {
        Serializer<T, String> serializer = SerializerFactory.create(this.entityClass, MediaType.JSON);
        String output = serializer.serialize(this.entity);
        System.out.println("JSON: " + output);
        //Assert.assertEquals(this.entityJSON.replaceAll("[\n\r]", ""), output.replaceAll("[\n\r]", ""));
    }

    @Test
    public void serializeToXML() throws Exception {
        Serializer<T, String> serializer = SerializerFactory.create(this.entityClass, MediaType.XML);
        String output = serializer.serialize(this.entity);
        System.out.println("XML: " + output);
        //Assert.assertEquals(this.entityXML.replaceAll("[\n\r]",""), output.replaceAll("[\n\r]",""));
    }

    protected T deserializeJson() throws Exception {
        Serializer<T, String> serializer = SerializerFactory.create(this.entityClass, MediaType.JSON);
        return serializer.deserialize(this.entityJSON);
    }

    public T deserializeXml() throws Exception {
        Serializer<T, String> serializer = SerializerFactory.create(this.entityClass, MediaType.XML);
        return serializer.deserialize(this.entityXML);
    }

    protected abstract String referenceJSON();

    protected abstract String referenceXML();
}
