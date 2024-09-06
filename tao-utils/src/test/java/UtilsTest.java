import org.junit.Test;
import ro.cs.tao.utils.Crypto;
import ro.cs.tao.utils.DateUtils;
import ro.cs.tao.utils.executors.ProcessHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class UtilsTest {

    @Test
    public void testDateFormat(){
        final String[] dates = new String[] {
                "2020-01-01T16:08:00",
                "2020-1-1T16:8:00",
                "2020-01-01T16:08:00.000",
                "2020-01-01T16:08:00.001020",
                "2020-01-01T16:08:00+00:00",
                "2020-01-01T16:08:00Z",
                "2020-01-01T16:08:00.000Z",
                "2020-01-01 16:08:00",
        };
        DateTimeFormatter formatter = DateUtils.getResilientFormatterAtUTC();
        List<LocalDateTime> result = new ArrayList<>();
        for (String date : dates) {
            result.add(LocalDateTime.parse(date, formatter));
            System.out.println(date + " -> OK");
        }
        for(int i = 0; i < result.size(); i++){
            if(i == 3)
                assertEquals("2020-01-01T16:08:00.001020", result.get(i).toString());
            else
                assertEquals("2020-01-01T16:08", result.get(i).toString());
        }
    }
    @Test
    public void testDecrypt(){
        String result = Crypto.decrypt("HM6p+OYI9WRfTPB4jdfn40lNR98InEyw5AGCoIU1rc0=", "eouser");
        assertEquals("PggeAUVVQjCkku1l", result);
    }
    @Test
    public void testJupyter(){
        List<String> expected = List.of("docker", "ps", "--filter", "\"ancestor=jupyterlite\"", "--filter", "\"name=1c48b074-0e26-41a9-ba95-8e3780dbff01\"", "--filter", "\"status=running\"");
        List<String> jupyterLiteTokens = ProcessHelper.tokenizeCommands("docker ps --filter \"ancestor=jupyterlite\" --filter \"name=1c48b074-0e26-41a9-ba95-8e3780dbff01\" --filter \"status=running\"");
        for(int i = 0; i < jupyterLiteTokens.size(); i++) {
            System.out.println(jupyterLiteTokens.get(i));
            assertEquals(expected.get(i), jupyterLiteTokens.get(i));
        }
    }
}
