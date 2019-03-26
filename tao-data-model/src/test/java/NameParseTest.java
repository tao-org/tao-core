import org.junit.Assert;
import org.junit.Test;
import ro.cs.tao.eodata.naming.NameExpressionParser;
import ro.cs.tao.eodata.naming.NameToken;
import ro.cs.tao.eodata.naming.NamingRule;

import java.util.ArrayList;
import java.util.List;

public class NameParseTest {

    @Test
    public void parseNames() throws Exception {
        String[] names = new String[] { "S2B_MSIL1C_20170719T094029_N0205_R036_T34UFA_20170719T094031.SAFE",
                                        "S2A_MSIL1C_20170726T083601_N0205_R064_T36UWB_20170726T083655.SAFE" };
        String expression = "${1:ADATE}_${1:TILE}_${2:ADATE}_${2:TILE}.tif";
        String expectedResult = "20170719_T34UFA_20170726_T36UWB.tif";
        String s2regEx = "(S2[A-B])_(MSIL1C)_(\\d{8})T(\\d{6})_(N\\d{4})_R(\\d{3})_(T\\d{2}\\w{3})_(\\d{8})T(\\d{6})(?:.SAFE)?";
        NamingRule rule = new NamingRule();
        rule.setRegEx(s2regEx);
        rule.setSensor("Sentinel2");
        List<NameToken> tokens = new ArrayList<>();
        tokens.add(new NameToken() {{
            setMatchingGroupNumber(1);
            setName("SATELLITE");
            setDescription("Satellite");
        }});
        tokens.add(new NameToken() {{
            setMatchingGroupNumber(2);
            setName("LEVEL");
            setDescription("Product Level");
        }});
        tokens.add(new NameToken() {{
            setMatchingGroupNumber(3);
            setName("ADATE");
            setDescription("Acquisition Date");
        }});
        tokens.add(new NameToken() {{
            setMatchingGroupNumber(4);
            setName("ATIME");
            setDescription("Acquisition Time");
        }});
        tokens.add(new NameToken() {{
            setMatchingGroupNumber(5);
            setName("BASELINE");
            setDescription("Processing baseline");
        }});
        tokens.add(new NameToken() {{
            setMatchingGroupNumber(6);
            setName("ORBIT");
            setDescription("Relative orbit");
        }});
        tokens.add(new NameToken() {{
            setMatchingGroupNumber(7);
            setName("TILE");
            setDescription("UTM Tile");
        }});
        tokens.add(new NameToken() {{
            setMatchingGroupNumber(8);
            setName("IDATE");
            setDescription("Ingestion Date");
        }});
        tokens.add(new NameToken() {{
            setMatchingGroupNumber(9);
            setName("ITIME");
            setDescription("Ingestion Time");
        }});
        rule.setTokens(tokens);
        NameExpressionParser parser = new NameExpressionParser(rule);
        String transformed = parser.resolve(expression, names);
        Assert.assertEquals(expectedResult, transformed);
    }
}
