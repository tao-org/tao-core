import ro.cs.tao.utils.DateUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Other {

    public static void main(String[] args) throws IOException {
        testDateFormat();
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
