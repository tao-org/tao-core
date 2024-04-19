import org.apache.commons.codec.digest.Crypt;
import ro.cs.tao.utils.Crypto;
import ro.cs.tao.utils.DateUtils;
import ro.cs.tao.utils.executors.ProcessHelper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Other {

    public static void main(String[] args) throws IOException {
        //testDateFormat();
        System.out.println(Crypto.decrypt("HM6p+OYI9WRfTPB4jdfn40lNR98InEyw5AGCoIU1rc0=", "eouser"));
        //final List<String> jupyterlite = ProcessHelper.tokenizeCommands("docker ps --filter \"ancestor=jupyterlite\" --filter \"name=1c48b074-0e26-41a9-ba95-8e3780dbff01\" --filter \"status=running\"");
        //jupyterlite.forEach(s -> System.out.println(s));
    }

    private static void testDateFormat() {
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
        for (String date : dates) {
            LocalDateTime.parse(date, formatter);
            System.out.println(date + " -> OK");
        }
    }
}
