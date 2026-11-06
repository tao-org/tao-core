package ro.cs.tao.utils;

import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class PublicHttpTest {
    @Test public void rejectsNonPublicAddressRanges() {
        for (String ip : new String[]{"127.0.0.1", "10.0.0.1", "192.168.0.1", "169.254.169.254",
                "100.64.0.1", "0.0.0.0", "::1", "fc00::1", "fe80::1", "::ffff:127.0.0.1", "2002:7f00:1::"}) {
            assertThrows(ip, UnknownHostException.class, () -> PublicHttp.publicAddresses(ip));
        }
    }

    @Test public void acceptsPublicLiteralWithoutConnecting() throws Exception {
        assertEquals("8.8.8.8", PublicHttp.publicAddresses("8.8.8.8")[0].getHostAddress());
    }

    @Test public void refusesUnsafeSchemesAuthoritiesAndPorts() {
        for (String url : new String[]{"file:///etc/passwd", "ftp://example.com/x", "http://user:password@example.com/",
                "http://example.com:8080/", "http://127.0.0.1/", "http://169.254.169.254/"}) {
            assertThrows(IOException.class, () -> PublicHttp.get(url, null, 100));
        }
    }
}
