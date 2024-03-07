/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package ro.cs.tao.utils;

import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import ro.cs.tao.utils.logger.Logger;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.*;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Helper class to check availability of a given url.
 *
 * @author Cosmin Cara
 */
public class NetUtils {

    private static final AutoEvictableCache<CompositeKey, WrappedCloseableHttpClient> httpClients;
    private static Proxy javaNetProxy;
    private static HttpHost apacheHttpProxy;
    private static CredentialsProvider proxyCredentials;
    private static int timeout = 30000;

    static {
        allowSelfSignedCertificates();
        httpClients = new AutoEvictableCache<>(compositeKey -> WrappedCloseableHttpClient.create(), 3600);
    }

    public static String getAuthToken(final String username, final String password) {
        String authToken = null;
        if(isNotEmpty(username) && isNotEmpty(password)){
            authToken = "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes()));
        }
        return authToken;
    }

    public static void setProxy(String type, final String host, final int port, final String user, final String pwd) {
        if (type != null && host != null && !type.isEmpty() && !host.isEmpty()) {
            Proxy.Type proxyType = Enum.valueOf(Proxy.Type.class, type.toUpperCase());
            javaNetProxy = new Proxy(proxyType, new InetSocketAddress(host, port));
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pwd.toCharArray());
                }
            });
            if (user != null && pwd != null) {
                proxyCredentials = new BasicCredentialsProvider();
                proxyCredentials.setCredentials(new AuthScope(host, port), new UsernamePasswordCredentials(user, pwd));
            }
            apacheHttpProxy = new HttpHost(host, port, proxyType.name());
        }
    }

    public static void setTimeout(int newTimeout) {
        timeout = newTimeout;
    }

    public static boolean isAvailable(String url, String user, String password) {
        boolean status;
        try {
            Logger.getRootLogger().debug("Verifying url: %s", url);
            HttpURLConnection connection = openConnection(url, getAuthToken(user, password));
            connection.setRequestMethod("GET");
            connection.connect();
            status = (200 == connection.getResponseCode() || 400 == connection.getResponseCode());
            Logger.getRootLogger().debug("Url status: %s [code %s]", url, connection.getResponseCode());
        } catch (Exception e) {
            Logger.getRootLogger().debug("Verification failed: %s", e.getMessage());
            status = false;
        }
        return status;
    }

    public static void allowSelfSignedCertificates() {
        try {
            /*
             *  fix for
             *    Exception in thread "main" javax.net.ssl.SSLHandshakeException:
             *       sun.security.validator.ValidatorException:
             *           PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
             *               unable to find valid certification path to requested target
             */
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }

                    }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            /*
             * end of the fix
             */
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(NetUtils.class.getName()).severe("Cannot apply https/localhost fix. Reason: " + ex.getMessage());
        }
    }

    public static HttpURLConnection openConnection(String url) {
        return openConnection(url, (String) null);
    }

    public static HttpURLConnection openConnection(String url, String authToken) {
        return openConnection(HttpMethod.GET, url, authToken, null);
    }

    public static HttpURLConnection openConnection(String url, Header authToken) {
        return openURLConnection(HttpMethod.GET, url, authToken, null);
    }

    public static HttpURLConnection openConnection(HttpMethod method, String url, String authToken, List<NameValuePair> requestProperties) {
        return openURLConnection(method, url, new BasicHeader("Authorization", authToken), requestProperties);
    }

    public static HttpURLConnection openURLConnection(HttpMethod method, String url, Header authHeader, List<NameValuePair> requestProperties) {
        HttpURLConnection connection = null;
        try {
            URL urlObj = new URL(url);
            if (javaNetProxy == null) {
                connection = (HttpURLConnection) urlObj.openConnection();
                Logger.getRootLogger().debug("Proxyless connection to %s opened", url);
            } else {
                connection = (HttpURLConnection) urlObj.openConnection(javaNetProxy);
                Logger.getRootLogger().debug("Proxy connection to %s opened", url);
            }
            connection.setRequestMethod(method.name());
            if (authHeader != null) {
                connection.setRequestProperty(authHeader.getName(), authHeader.getValue());
            }
            if (requestProperties != null && !requestProperties.isEmpty()) {
                for (NameValuePair requestProperty : requestProperties) {
                    connection.setRequestProperty(requestProperty.getName(), requestProperty.getValue());
                }
            }
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
        } catch (IOException e) {
            Logger.getRootLogger().debug("Could not open connection to %s [%s]", url, e.getMessage());
        }
        if (connection != null) {
            StringBuilder builder = new StringBuilder();
            Map<String, List<String>> currentRequestProperties = connection.getRequestProperties();
            for (Map.Entry<String, List<String>> entry : currentRequestProperties.entrySet()) {
                builder.append(entry.getKey()).append("=");
                for (String value : entry.getValue()) {
                    builder.append(value).append(",");
                }
                builder.append(";");
            }
            if (builder.length() > 0) {
                Logger.getRootLogger().debug("Request details: %s", builder.toString());
            }
        }
        return connection;
    }

    public static ro.cs.tao.utils.CloseableHttpResponse openConnection(HttpMethod method, String url, Credentials credentials) throws IOException {
        return openConnection(method, url, credentials, (List<NameValuePair>) null);
    }

    public static ro.cs.tao.utils.CloseableHttpResponse openConnection(HttpMethod method, String url, Credentials credentials, String json) throws IOException {
        CloseableHttpResponse response;
        try {
            final URI uri = new URI(url);
            final CloseableHttpClient httpClient = getOrCreateHttpClient(uri, credentials);
            final HttpRequestBase requestBase;
            switch (method) {
                case GET:
                    requestBase = new HttpGet(uri);
                    break;
                case POST:
                    requestBase = new HttpPost(uri);
                    if (json != null) {
                        StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
                        ((HttpPost) requestBase).setEntity(requestEntity);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Method not supported");
            }
            final RequestConfig.Builder requestBuilder = RequestConfig.custom()
                                                                      .setConnectionRequestTimeout(timeout)
                                                                      .setConnectTimeout(timeout)
                                                                      .setSocketTimeout(timeout);
            if (apacheHttpProxy != null) {
                requestBuilder.setProxy(apacheHttpProxy);
            }
            requestBase.setConfig(requestBuilder.build());
            Logger.getRootLogger().debug("Details: %s", requestBase.getConfig().toString());
            response = httpClient.execute(requestBase);
            Logger.getRootLogger().debug("HTTP %s %s returned %s", method.toString(), url, response.getStatusLine().getStatusCode());
            return new ro.cs.tao.utils.CloseableHttpResponse(httpClient, response, false);
        } catch (URISyntaxException | IOException e) {
            Logger.getRootLogger().debug("Could not create connection to %s : %s", url, e.getMessage());
            throw new IOException(e);
        }
    }

    public static ro.cs.tao.utils.CloseableHttpResponse openConnection(HttpMethod method, String url, String header, String authToken, String json) throws IOException {
        CloseableHttpResponse response;
        try {
            final URI uri = new URI(url);
            final Header authHeader = new BasicHeader(header, authToken);
            final CloseableHttpClient httpClient = getOrCreateHttpClient(uri, header);
            final HttpRequestBase requestBase;
                switch (method) {
                    case GET:
                        requestBase = new HttpGet(uri);
                        break;
                    case POST:
                        requestBase = new HttpPost(uri);
                        if (json != null) {
                            StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
                            ((HttpPost) requestBase).setEntity(requestEntity);
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Method not supported");
                }
                final RequestConfig.Builder requestBuilder = RequestConfig.custom()
                                                                          .setConnectionRequestTimeout(timeout)
                                                                          .setConnectTimeout(timeout)
                                                                          .setSocketTimeout(timeout);
                if (apacheHttpProxy != null) {
                    requestBuilder.setProxy(apacheHttpProxy);
                }
                requestBase.setConfig(requestBuilder.build());
                requestBase.addHeader(authHeader);
                requestBase.addHeader("Content-Type", "application/json");
                Logger.getRootLogger().debug("Details: %s", requestBase.getConfig().toString());
                response = httpClient.execute(requestBase);
                Logger.getRootLogger().debug("HTTP %s %s returned %s", method.toString(), url, response.getStatusLine().getStatusCode());
                return new ro.cs.tao.utils.CloseableHttpResponse(httpClient, response, false);
        } catch (URISyntaxException | IOException e) {
            Logger.getRootLogger().debug("Could not create connection to %s : %s", url, e.getMessage());
            throw new IOException(e);
        }
    }

    public static ro.cs.tao.utils.CloseableHttpResponse openConnection(HttpMethod method, String url, Header header, List<NameValuePair> parameters) throws IOException {
        CloseableHttpResponse response;
        try {
            final URI uri = new URI(url);
            final CloseableHttpClient httpClient = getOrCreateHttpClient(uri, header);
            final HttpRequestBase requestBase;
            switch (method) {
                case GET:
                    requestBase = new HttpGet(uri);
                    break;
                case POST:
                    requestBase = new HttpPost(uri);
                    if (parameters != null) {
                        ((HttpPost) requestBase).setEntity(new UrlEncodedFormEntity(parameters));
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Method not supported");
            }
            final RequestConfig.Builder requestBuilder = RequestConfig.custom()
                                                                      .setConnectionRequestTimeout(timeout)
                                                                      .setConnectTimeout(timeout)
                                                                      .setSocketTimeout(timeout);
            if (apacheHttpProxy != null) {
                requestBuilder.setProxy(apacheHttpProxy);
            }
            requestBase.setConfig(requestBuilder.build());
            requestBase.setHeader(header);
            Logger.getRootLogger().debug("Details: %s", requestBase.getConfig().toString());
            response = httpClient.execute(requestBase);
            Logger.getRootLogger().debug("HTTP %s %s returned %s", method.toString(), url, response.getStatusLine().getStatusCode());
            return new ro.cs.tao.utils.CloseableHttpResponse(httpClient, response, true);
        } catch (URISyntaxException | IOException e) {
            Logger.getRootLogger().debug("Could not create connection to %s : %s", url, e.getMessage());
            throw new IOException(e);
        }
    }

    public static ro.cs.tao.utils.CloseableHttpResponse openConnection(HttpMethod method, String url, List<Header> headers, List<NameValuePair> parameters) throws IOException {
        return openConnection(method, url, headers, parameters, timeout);
    }

    public static ro.cs.tao.utils.CloseableHttpResponse openConnection(HttpMethod method, String url, List<Header> headers, List<NameValuePair> parameters, int timeoutMillis) throws IOException {
        CloseableHttpResponse response;
        try {
            final URI uri = new URI(url);
            final CloseableHttpClient httpClient = getOrCreateHttpClient(uri, "");
            final HttpRequestBase requestBase;
            switch (method) {
                case GET:
                    requestBase = new HttpGet(uri);
                    break;
                case POST:
                    requestBase = new HttpPost(uri);
                    if (parameters != null) {
                        ((HttpPost) requestBase).setEntity(new UrlEncodedFormEntity(parameters));
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Method not supported");
            }
            final RequestConfig.Builder requestBuilder = RequestConfig.custom().setRedirectsEnabled(true)
                                                                      .setConnectionRequestTimeout(timeoutMillis)
                                                                      .setConnectTimeout(timeoutMillis)
                                                                      .setSocketTimeout(timeoutMillis);
            if (apacheHttpProxy != null) {
                requestBuilder.setProxy(apacheHttpProxy);
            }
            requestBase.setConfig(requestBuilder.build());
            if (headers != null) {
                for (Header header : headers) {
                    requestBase.setHeader(header);
                }
            }
            Logger.getRootLogger().debug("Details: %s", requestBase.getConfig().toString());
            response = httpClient.execute(requestBase);
            Logger.getRootLogger().debug("HTTP %s %s returned %s", method.toString(), url, response.getStatusLine().getStatusCode());
            return new ro.cs.tao.utils.CloseableHttpResponse(httpClient, response, true);
        } catch (URISyntaxException | IOException e) {
            Logger.getRootLogger().debug("Could not create connection to %s : %s", url, e.getMessage());
            throw new IOException(e);
        }
    }

    public static ro.cs.tao.utils.CloseableHttpResponse openConnection(HttpMethod method, String url, Credentials credentials, List<NameValuePair> parameters) throws IOException {
        return openConnection(method, url, credentials, parameters, timeout);
    }

    public static ro.cs.tao.utils.CloseableHttpResponse openConnection(HttpMethod method, String url, Credentials credentials, List<NameValuePair> parameters, int timeoutMillis) throws IOException {
        CloseableHttpResponse response;
        try {
            URI uri = new URI(url);
            CloseableHttpClient httpClient = getOrCreateHttpClient(uri, credentials);
            HttpRequestBase requestBase;
            switch (method) {
                case GET:
                    requestBase = new HttpGet(uri);
                    break;
                case POST:
                    requestBase = new HttpPost(uri);
                    if (parameters != null) {
                        ((HttpPost) requestBase).setEntity(new UrlEncodedFormEntity(parameters));
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Method not supported");
            }
            final RequestConfig.Builder requestBuilder = RequestConfig.custom()
                    .setConnectionRequestTimeout(timeoutMillis)
                    .setConnectTimeout(timeoutMillis)
                    .setSocketTimeout(timeoutMillis);
            if (apacheHttpProxy != null) {
                requestBuilder.setProxy(apacheHttpProxy);
            }
            requestBase.setConfig(requestBuilder.build());
            Logger.getRootLogger().debug("Details: %s", requestBase.getConfig().toString());
            response = httpClient.execute(requestBase);
            Logger.getRootLogger().debug("HTTP %s %s returned %s", method.toString(), url, response.getStatusLine().getStatusCode());
            return new ro.cs.tao.utils.CloseableHttpResponse(httpClient, response, false);
        } catch (URISyntaxException | IOException e) {
            Logger.getRootLogger().debug("Could not create connection to %s : %s", url, e.getMessage());
            throw new IOException(e);
        }
    }

    public static String getResponseAsString(String url) throws IOException {
        return getResponseAsString(url, null, null);
    }

    public static String getResponseAsString(String url, Credentials credentials, List<NameValuePair> parameters) throws IOException {
        String result = null;
        try (ro.cs.tao.utils.CloseableHttpResponse response = NetUtils.openConnection(HttpMethod.GET, url, credentials, parameters)) {
            switch (response.getStatusLine().getStatusCode()) {
                case 200:
                    result = EntityUtils.toString(response.getEntity());
                    break;
                case 401:
                    Logger.getRootLogger().warn("The supplied credentials are invalid!");
                    break;
                default:
                    Logger.getRootLogger().warn("The request was not successful. Reason: %s", response.getStatusLine().getReasonPhrase());
                    break;
            }
        }
        return result;
    }

    public static String getResponseAsString(String url, List<NameValuePair> parameters) throws IOException {
        return getResponseAsString(url, null, parameters);
    }

    public static NetStreamResponse getResponseAsStream(String url) throws IOException {
        return getResponseAsStream(url, null);
    }

    public static NetStreamResponse getResponseAsStream(String url, Credentials credentials) throws IOException {
        NetStreamResponse result;
        try (ro.cs.tao.utils.CloseableHttpResponse response = NetUtils.openConnection(HttpMethod.GET, url, credentials)) {
            StatusLine statusLine = response.getStatusLine();
            switch (statusLine.getStatusCode()) {
                case 200:
                    HttpEntity entity = response.getEntity();
                    Header contentType = entity.getContentType();
                    Header[] headers = response.getHeaders("Content-Disposition");
                    String name;
                    if (headers.length > 0) {
                        name = headers[0].getValue();
                        if (name.toLowerCase().contains("filename")) {
                            name = name.toLowerCase().substring(name.lastIndexOf("=") + 1).replace("\"", "");
                        }
                    } else {
                        name = url.substring(url.lastIndexOf("/") + 1);
                    }
                    result = new NetStreamResponse(name, entity.getContentLength(),
                                                   contentType != null ? contentType.getValue() : "applicaiton/otctet-stream",
                                                   EntityUtils.toByteArray(entity));

                    break;
                case 401:
                    throw new IOException("401: Unauthorized or the supplied credentials are invalid");
                default:
                    throw new IOException(statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
            }
        }
        return result;
    }

    public static List<Cookie> getCookies(String url, Credentials credentials) throws IOException {
        return getCookies(url, credentials.getUserPrincipal().getName() + ":" + credentials.getPassword());
    }

    public static List<Cookie> getCookies(String url, String authToken) throws IOException {
        try {
            final URI uri = new URI(url);
            final String domain = extractDomainFromURL(uri);
            final CompositeKey key = new CompositeKey(domain, authToken);
            final WrappedCloseableHttpClient wrappedCloseableHttpClient;
            synchronized (httpClients) {
                wrappedCloseableHttpClient = httpClients.get(key);
            }
            if (wrappedCloseableHttpClient != null) {
                return wrappedCloseableHttpClient.getCookieStore().getCookies().stream().filter(c -> c.getValue() != null && !c.getValue().isEmpty()).collect(Collectors.toList());
            }
            Logger.getRootLogger().debug("Could not get cookies for %s : HttpClient not found.", url);
            return new ArrayList<>();
        } catch (URISyntaxException e) {
            Logger.getRootLogger().debug("Could not get cookies for %s : %s", url, e.getMessage());
            throw new IOException(e);
        }
    }

    private static String extractDomainFromURL(URI uri) throws URISyntaxException {
        String domain = uri.getHost();
        if (domain.indexOf(".") > 0) {
            String[] tokens = domain.split("\\.");
            domain = tokens[tokens.length - 2] + "." + tokens[tokens.length - 1];
        }
        return domain;
    }

    private static CloseableHttpClient getOrCreateHttpClient(URI uri, Credentials credentials) throws URISyntaxException {
        if (credentials != null) {
            return getOrCreateHttpClient(uri, credentials.getUserPrincipal().getName() + ":" + credentials.getPassword(), credentials);
        }
        return getOrCreateHttpClient(uri, "");
    }

    private static CloseableHttpClient getOrCreateHttpClient(URI uri, Header authHeader) throws URISyntaxException {
        if (authHeader != null) {
            return getOrCreateHttpClient(uri, authHeader.getName() + ":" + authHeader.getValue());
        }
        return getOrCreateHttpClient(uri, "");
    }

    private static CloseableHttpClient getOrCreateHttpClient(URI uri, String authToken) throws URISyntaxException {
        return getOrCreateHttpClient(uri, authToken, null);
    }

    private static CloseableHttpClient getOrCreateHttpClient(URI uri, String authToken, Credentials credentials) throws URISyntaxException {
        final String domain = extractDomainFromURL(uri);
        final CompositeKey key = new CompositeKey(domain, authToken);
        synchronized (httpClients) {
            if (!httpClients.keySet().contains(key)) {
                final CredentialsProvider credentialsProvider = proxyCredentials != null ? proxyCredentials : new BasicCredentialsProvider();
                httpClients.put(key, WrappedCloseableHttpClient.create(credentialsProvider));
            }
        }
        final WrappedCloseableHttpClient wrappedCloseableHttpClient = httpClients.get(key);
        if (credentials != null) {
            wrappedCloseableHttpClient.setCredentials(uri, credentials);
        }
        return wrappedCloseableHttpClient.getCloseableHttpClient();

    }
}
