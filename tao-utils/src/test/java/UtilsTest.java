import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import ro.cs.tao.utils.Crypto;
import ro.cs.tao.utils.DateUtils;
import ro.cs.tao.utils.executors.ProcessHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
        String result = Crypto.decrypt("S8V2XqOPxN1y7/ovrtSvAg==", "silvia.ricolfi");
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

    @Test
    public void highlightMatchingParentheses() {
        final String RED = "\u001B[41m";
        final String BLUE = "\u001B[44m";
        final String BLACK = "\u001B[0m";
        final String test = "((?:[A-Za-z0-9_]*?(?=\\d{8}))((\\d{4})(\\d{2})(\\d{2}))?(?:[A-Za-z0-9_]*))\\.(tif|TIF|tiff|TIFF+)";
        final List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        for (int i = 0; i < test.length(); i++) {
            if (test.charAt(i) == '(') {
                pairs.add(Pair.of(i, null));
            }
            if (test.charAt(i) == ')') {
                if (pairs.size() == 0) {
                    pairs.add(Pair.of(null, i));
                } else {
                    int idx = pairs.size() - 1;
                    boolean found = false;
                    for (int j = pairs.size() - 1; j >= 0; j--) {
                        if (pairs.get(j).getRight() == null) {
                            idx = j;
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        final Pair<Integer, Integer> pair = pairs.get(idx);
                        pairs.set(idx, Pair.of(pair.getLeft(), i));
                    } else {
                        pairs.add(Pair.of(null, i));
                    }
                }
            }
        }
        Integer fromStart, fromEnd;
        for (int i = 0; i < pairs.size(); i++) {
            fromStart = pairs.get(i).getLeft();
            fromEnd = pairs.get(i).getRight();
            if (fromStart == null) {
                System.out.println(test.substring(0, fromEnd) + RED + test.charAt(fromEnd)
                                           + BLACK + test.substring(fromEnd + 1));
            } else if (fromEnd == null) {
                System.out.println(test.substring(0, fromStart) + RED + test.charAt(fromStart)
                                           + BLACK + test.substring(fromStart + 1));
            } else {
                System.out.println(test.substring(0, fromStart) + BLUE + test.charAt(fromStart)
                                           + BLACK + test.substring(fromStart + 1, fromEnd)
                                           + BLUE + test.charAt(fromEnd)
                                           + BLACK + test.substring(fromEnd + 1));
            }
        }

    }
}
