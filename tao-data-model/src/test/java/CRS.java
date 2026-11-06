import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import ro.cs.tao.component.template.BasicTemplate;
import ro.cs.tao.component.template.engine.XsltTemplateEngine;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class CRS {

    public static void main(String[] args) throws Exception {
        XsltTemplateEngine engine = new XsltTemplateEngine();
        BasicTemplate template = new BasicTemplate();
        template.setContents(Files.readString(Paths.get("D:\\sample.xml")));
        System.out.println(engine.transform(template, new HashMap<>() /*{{ put("snap12-subset-copyMetadata", "true"); }}*/));
        System.exit(0);
    }
}
