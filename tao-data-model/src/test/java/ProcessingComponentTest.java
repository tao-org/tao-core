import org.junit.Assert;
import org.junit.Test;
import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.SourceDescriptor;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.component.Variable;
import ro.cs.tao.component.template.BasicTemplate;
import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.template.TemplateType;
import ro.cs.tao.eodata.enums.DataFormat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Cosmin Cara
 */
public class ProcessingComponentTest extends BaseSerializationTest<ProcessingComponent> {
    @Override
    protected String referenceJSON() {
        return "{\n" +
                "   \"processingComponent\" : {\n" +
                "      \"id\" : \"OTB-Segmentation-CC\",\n" +
                "      \"authors\" : \"King Arthur\",\n" +
                "      \"copyright\" : \"(C) Camelot Productions\",\n" +
                "      \"description\" : \"Performs segmentation of an image, and output either a raster or a vector file. In vector mode, large input datasets are supported.\",\n" +
                "      \"label\" : \"OTB Segmentation CC\",\n" +
                "      \"nodeAffinity\" : \"Any\",\n" +
                "      \"inputs\" : {\n" +
                "         \"sources\" : [ {\n" +
                "            \"id\" : \"sourceProductFile\",\n" +
                "            \"parentId\" : \"OTB-Segmentation-Cc\",\n" +
                "            \"dataType\" : \"RASTER\",\n" +
                "            \"constraints\" : {\n" +
                "               \"constraints\" : [ {\n" +
                "                  \"type\" : \"sensorConstraint\"\n" +
                "               } ]\n" +
                "            }\n" +
                "         } ]\n" +
                "      },\n" +
                "      \"outputs\" : {\n" +
                "         \"targets\" : [ {\n" +
                "            \"id\" : \"out_str\",\n" +
                "            \"parentId\" : \"OTB-Segmentation-Cc\",\n" +
                "            \"dataType\" : \"RASTER\",\n" +
                "            \"constraints\" : {\n" +
                "               \"constraints\" : [ ]\n" +
                "            }\n" +
                "         } ]\n" +
                "      },\n" +
                "      \"version\" : \"1.0\",\n" +
                "      \"fileLocation\" : \"E:\\\\OTB\\\\otbcli_Segmentation.bat\",\n" +
                "      \"parameters\" : {\n" +
                "         \"parameterDescriptors\" : [ {\n" +
                "            \"id\" : \"outmode_string\",\n" +
                "            \"dataType\" : \"java.lang.String\",\n" +
                "            \"defaultValue\" : \"ulco\",\n" +
                "            \"description\" : \"This allows setting the writing behaviour for the output vector file. Please note that the actual behaviour depends on the file format.\"\n" +
                "         }, {\n" +
                "            \"id\" : \"neighbor_bool\",\n" +
                "            \"dataType\" : \"java.lang.Boolean\",\n" +
                "            \"defaultValue\" : \"true\",\n" +
                "            \"description\" : \"Activate 8-Neighborhood connectivity (default is 4).\"\n" +
                "         } ]\n" +
                "      },\n" +
                "      \"template\" : {\n" +
                "         \"type\" : \"basicTemplate\",\n" +
                "         \"contents\" : \"-in\\n$sourceProductFile\\n-filter.cc.expr\\n$expr_string\\n-mode.vector.out\\n$out_str\\n-mode.vector.outmode\\n$outmode_string\\n-mode.vector.neighbor\\n$neighbor_bool\\n-mode.vector.stitch\\n$stitch_bool\\n-mode.vector.minsize\\n$minsize_int\\n-mode.vector.simplify\\n$simplify_float\\n-mode.vector.layername\\n$layername_string\\n-mode.vector.fieldname\\n$fieldname_string\\n-mode.vector.tilesize\\n$tilesize_int\\n-mode.vector.startlabel\\n$startlabel_int\",\n" +
                "         \"id\" : \"segmentation-cc-template.vm\",\n" +
                "         \"templateType\" : 1\n" +
                "      },\n" +
                "      \"variables\" : {\n" +
                "         \"variables\" : [ {\n" +
                "            \"id\" : \"ITK_AUTOLOAD_PATH\",\n" +
                "            \"value\" : \"E:\\\\OTB\\\\bin\"\n" +
                "         } ]\n" +
                "      },\n" +
                "      \"workingDirectory\" : \"E:\\\\OTB\"\n" +
                "   }\n" +
                "}";
    }

    @Override
    protected String referenceXML() {
        return "<processingComponent>\n" +
                "    <id>OTB-Segmentation-CC</id>\n" +
                "    <authors>King Arthur</authors>\n" +
                "    <copyright>(C) Camelot Productions</copyright>\n" +
                "    <description>Performs segmentation of an image, and output either a raster or a vector file. In vector mode, large input datasets are supported.</description>\n" +
                "    <label>OTB Segmentation CC</label>\n" +
                "    <nodeAffinity>Any</nodeAffinity>\n" +
                "    <inputs>\n" +
                "        <sources>\n" +
                "            <id>sourceProductFile</id>\n" +
                "            <parentId>OTB-Segmentation-CC</parentId>\n" +
                "            <dataType>RASTER</dataType>\n" +
                "            <constraints>\n" +
                "                <constraints xsi:type=\"sensorConstraint\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>\n" +
                "            </constraints>\n" +
                "        </sources>\n" +
                "    </inputs>\n" +
                "    <outputs>\n" +
                "        <targets>\n" +
                "            <id>out_str</id>\n" +
                "            <parentId>OTB-Segmentation-CC</parentId>\n" +
                "            <dataType>RASTER</dataType>\n" +
                "            <constraints/>\n" +
                "        </targets>\n" +
                "    </outputs>\n" +
                "    <version>1.0</version>\n" +
                "    <fileLocation>E:\\OTB\\otbcli_Segmentation.bat</fileLocation>\n" +
                "    <parameters>\n" +
                "        <parameterDescriptors>\n" +
                "            <id>outmode_string</id>\n" +
                "            <dataType>java.lang.String</dataType>\n" +
                "            <defaultValue>ulco</defaultValue>\n" +
                "            <description>This allows setting the writing behaviour for the output vector file. Please note that the actual behaviour depends on the file format.</description>\n" +
                "        </parameterDescriptors>\n" +
                "        <parameterDescriptors>\n" +
                "            <id>neighbor_bool</id>\n" +
                "            <dataType>java.lang.Boolean</dataType>\n" +
                "            <defaultValue>true</defaultValue>\n" +
                "            <description>Activate 8-Neighborhood connectivity (default is 4).</description>\n" +
                "        </parameterDescriptors>\n" +
                "    </parameters>\n" +
                "    <template xsi:type=\"basicTemplate\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "        <contents>-in\n" +
                "$sourceProductFile\n" +
                "-filter.cc.expr\n" +
                "$expr_string\n" +
                "-mode.vector.out\n" +
                "$out_str\n" +
                "-mode.vector.outmode\n" +
                "$outmode_string\n" +
                "-mode.vector.neighbor\n" +
                "$neighbor_bool\n" +
                "-mode.vector.stitch\n" +
                "$stitch_bool\n" +
                "-mode.vector.minsize\n" +
                "$minsize_int\n" +
                "-mode.vector.simplify\n" +
                "$simplify_float\n" +
                "-mode.vector.layername\n" +
                "$layername_string\n" +
                "-mode.vector.fieldname\n" +
                "$fieldname_string\n" +
                "-mode.vector.tilesize\n" +
                "$tilesize_int\n" +
                "-mode.vector.startlabel\n" +
                "$startlabel_int</contents>\n" +
                "        <id>segmentation-cc-template.vm</id>\n" +
                "        <templateType>1</templateType>\n" +
                "    </template>\n" +
                "    <variables>\n" +
                "        <variables>\n" +
                "            <id>ITK_AUTOLOAD_PATH</id>\n" +
                "            <value>E:\\OTB\\bin</value>\n" +
                "        </variables>\n" +
                "    </variables>\n" +
                "    <workingDirectory>E:\\OTB</workingDirectory>\n" +
                "</processingComponent>";
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ArrayList<ParameterDescriptor> parameters = new ArrayList<>();
        parameters.add(newParameter("outmode_string",
                                    String.class,
                                    "ulco",
                                    "This allows setting the writing behaviour for the output vector file. Please note that the actual behaviour depends on the file format."));
        parameters.add(newParameter("neighbor_bool",
                                    Boolean.class,
                                    Boolean.TRUE.toString(),
                                    "Activate 8-Neighborhood connectivity (default is 4)."));
        Set<Variable> variables = new HashSet<>();
        variables.add(new Variable("ITK_AUTOLOAD_PATH", "E:\\OTB\\bin"));
        Template template = new BasicTemplate();
        template.setName("segmentation-cc-template.vm");
        template.setTemplateType(TemplateType.VELOCITY);
        template.setContents("-in\n" +
                             "$sourceProductFile\n" +
                             "-filter.cc.expr\n" +
                             "$expr_string\n" +
                             "-mode.vector.out\n" +
                             "$out_str\n" +
                             "-mode.vector.outmode\n" +
                             "$outmode_string\n" +
                             "-mode.vector.neighbor\n" +
                             "$neighbor_bool\n" +
                             "-mode.vector.stitch\n" +
                             "$stitch_bool\n" +
                             "-mode.vector.minsize\n" +
                             "$minsize_int\n" +
                             "-mode.vector.simplify\n" +
                             "$simplify_float\n" +
                             "-mode.vector.layername\n" +
                             "$layername_string\n" +
                             "-mode.vector.fieldname\n" +
                             "$fieldname_string\n" +
                             "-mode.vector.tilesize\n" +
                             "$tilesize_int\n" +
                             "-mode.vector.startlabel\n" +
                             "$startlabel_int", false);
        SourceDescriptor sourceDescriptor = new SourceDescriptor("sourceProductFile");
        sourceDescriptor.setDataType(DataFormat.RASTER);
        TargetDescriptor targetDescriptor = new TargetDescriptor("out_str");
        targetDescriptor.setDataType(DataFormat.RASTER);
        entity = new ProcessingComponent() {{
            setId("OTB-Segmentation-CC");
            setLabel("OTB Segmentation CC");
            setDescription("Performs segmentation of an image, and output either a raster or a vector file. In vector mode, large input datasets are supported.");
            setAuthors("King Arthur");
            setCopyright("(C) Camelot Productions");
            setFileLocation("E:\\OTB\\otbcli_Segmentation.bat");
            setWorkingDirectory("E:\\OTB");
            setNodeAffinity("Any");
            addSource(sourceDescriptor);
            addTarget(targetDescriptor);
            setVersion("1.0");
            setParameterDescriptors(parameters);
            setVariables(variables);
            setTemplateType(TemplateType.VELOCITY);
            setTemplate(template);
        }};
    }

    @Test
    public void deserializeFromXML() throws Exception {
        ProcessingComponent object = deserializeXml();
        Assert.assertEquals("OTB-Segmentation-CC", object.getId());
    }

    @Test
    public void deserializeFromJSON() throws Exception {
        ProcessingComponent object = deserializeJson();
        Assert.assertEquals("OTB-Segmentation-CC", object.getId());
    }

    private ParameterDescriptor newParameter(String name, Class<?> clazz, String defaultValue, String description) {
        ParameterDescriptor ret = new ParameterDescriptor(name);
        ret.setDataType(clazz);
        ret.setDefaultValue(defaultValue);
        ret.setDescription(description);
        return ret;
    }
}
