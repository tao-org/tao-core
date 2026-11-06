package ro.cs.tao.utils;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

/** Bounded public-Internet fetches. DNS results are checked by the resolver actually used to connect. */
public final class PublicHttp {
    private PublicHttp() { }

    public static byte[] get(String url, Credentials credentials, int maxBytes) throws IOException {
        URI uri;
        try { uri = URI.create(url); } catch (RuntimeException e) { throw new IOException("Invalid URL"); }
        if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                || uri.getHost() == null || uri.getUserInfo() != null || uri.getFragment() != null
                || uri.getPort() != -1 && uri.getPort() != 80 && uri.getPort() != 443) {
            throw new IOException("URL is not an allowed HTTP destination");
        }
        var provider = new BasicCredentialsProvider();
        if (credentials != null) provider.setCredentials(new AuthScope(uri.getHost(), uri.getPort()), credentials);
        try (var client = HttpClients.custom().disableRedirectHandling().disableAutomaticRetries()
                .setDnsResolver(PublicHttp::publicAddresses)
                .setDefaultCredentialsProvider(provider)
                .setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(5000)
                        .setConnectionRequestTimeout(5000).setSocketTimeout(10000).build()).build()) {
            HttpGet request = new HttpGet(uri);
            // A hard deadline also bounds peers that trickle bytes below the socket timeout.
            java.util.concurrent.ScheduledExecutorService timer = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
            var deadline = timer.schedule(request::abort, 30, java.util.concurrent.TimeUnit.SECONDS);
            try (var response = client.execute(request)) {
                if (response.getStatusLine().getStatusCode() != 200 || response.getEntity() == null) {
                    throw new IOException("Remote response rejected (redirects are disabled)");
                }
                if (response.getEntity().getContentLength() > maxBytes) throw new IOException("Remote content too large");
                try (var input = response.getEntity().getContent()) {
                    byte[] data = input.readNBytes(maxBytes + 1);
                    if (data.length > maxBytes) throw new IOException("Remote content too large");
                    return data;
                }
            } finally { deadline.cancel(false); timer.shutdownNow(); }
        }
    }

    static InetAddress[] publicAddresses(String host) throws UnknownHostException {
        InetAddress[] addresses = InetAddress.getAllByName(host);
        for (InetAddress address : addresses) {
            byte[] raw = address.getAddress();
            boolean blocked = address.isAnyLocalAddress() || address.isLoopbackAddress()
                    || address.isLinkLocalAddress() || address.isSiteLocalAddress() || address.isMulticastAddress();
            if (raw.length == 4) {
                int a = raw[0] & 255, b = raw[1] & 255;
                blocked |= a == 0 || a >= 224 || a == 100 && b >= 64 && b <= 127
                        || a == 169 && b == 254 || a == 198 && (b == 18 || b == 19);
            } else {
                // Only global unicast IPv6; excludes ULA, mapped/compatible IPv4 and transition ranges.
                blocked |= (raw[0] & 0xe0) != 0x20
                        || (raw[0] & 255) == 0x20 && (raw[1] & 255) == 0x02
                        || (raw[0] & 255) == 0x20 && (raw[1] & 255) == 0x01 && raw[2] == 0 && raw[3] == 0;
            }
            if (blocked) throw new UnknownHostException("Non-public destination is not allowed");
        }
        return addresses;
    }
}
