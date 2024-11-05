package ro.cs.tao.utils;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicHeader;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;

public final class WrappedCloseableHttpClient {

    private final CloseableHttpClient closeableHttpClient;
    private final CookieStore cookieStore;
    private final CredentialsProvider credentialsProvider;

    private WrappedCloseableHttpClient(CloseableHttpClient closeableHttpClient, CookieStore cookieStore, CredentialsProvider credentialsProvider) {
        this.closeableHttpClient = closeableHttpClient;
        this.cookieStore = cookieStore;
        this.credentialsProvider = credentialsProvider;
    }

    public CloseableHttpClient getCloseableHttpClient() {
        return closeableHttpClient;
    }

    public CookieStore getCookieStore() {
        return cookieStore;
    }

    public void setCredentials(URI uri, Credentials credentials) {
        final AuthScope authScope = new AuthScope(uri.getHost(), uri.getPort());
        if (this.credentialsProvider != null && this.credentialsProvider.getCredentials(authScope) == null) {
            this.credentialsProvider.setCredentials(authScope, credentials);
        }
    }

    public static WrappedCloseableHttpClient create(){
        return create(null);
    }

    public static WrappedCloseableHttpClient create(CredentialsProvider credentialsProvider) {
        final CookieStore cookieStore = new BasicCookieStore();
        return new WrappedCloseableHttpClient(createHttpClient(cookieStore, credentialsProvider), cookieStore, credentialsProvider);
    }

    public static CloseableHttpClient createHttpClient(CookieStore cookieStore, CredentialsProvider credentialsProvider) {
        if (credentialsProvider != null) {
            return defaultClientBuilder(cookieStore).setDefaultCredentialsProvider(credentialsProvider).build();
        } else {
            return defaultClientBuilder(cookieStore).build();
        }
    }

    private static HttpClientBuilder defaultClientBuilder(CookieStore cookieStore) {
        return HttpClients.custom()
                .setConnectionManagerShared(true)
                .setDefaultCookieStore(cookieStore)
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; …) Gecko/20100101 Firefox/57.0")
                .setSSLSocketFactory(sslConnectionSocketFactory())
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultHeaders(List.of(new BasicHeader("Accept", "*/*")));
    }

    private static SSLConnectionSocketFactory sslConnectionSocketFactory() {
        SSLConnectionSocketFactory sslsf = null;
        try {
            // This is to accept self-signed certificates
            final SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[0], new TrustManager[]{
                            new X509TrustManager() {
                                @Override
                                public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
                                }

                                @Override
                                public void checkServerTrusted(X509Certificate[] arg0, String arg1) {
                                }

                                @Override
                                public X509Certificate[] getAcceptedIssuers() {
                                    return null;
                                }
                            }},
                    new SecureRandom());
            sslsf = new SSLConnectionSocketFactory(ctx, NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return sslsf;
    }

}
