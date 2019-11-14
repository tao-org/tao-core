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
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import ro.cs.tao.utils.logger.Logger;

import java.io.IOException;
import java.net.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * Helper class to check availability of a given url.
 *
 * @author Cosmin Cara
 */
public class NetUtils {

    private static final Map<CompositeKey, CloseableHttpClient> httpClients = new HashMap<>();
    private static Proxy javaNetProxy;
    private static HttpHost apacheHttpProxy;
    private static CredentialsProvider proxyCredentials;
    private static int timeout = 30000;

    public static String getAuthToken(final String username, final String password) {
        String authToken = null;
        if(isNotEmpty(username) && isNotEmpty(password)){
            authToken = "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes()));
        }
        return authToken;
    }

    public static void setProxy(String type, final String host, final int port, final String user, final String pwd) {
        if (type != null && host != null && !"".equals(type) && !"".equals(host)) {
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

    public static HttpURLConnection openConnection(String url) {
        return openConnection(url, null);
    }

    public static HttpURLConnection openConnection(String url, String authToken) {
        return openConnection(HttpMethod.GET, url, authToken, null);
    }

    public static HttpURLConnection openConnection(HttpMethod method, String url, String authToken, List<NameValuePair> requestProperties) {
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
            if (authToken != null && !authToken.isEmpty()) {
                connection.setRequestProperty("Authorization", authToken);
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

    public static CloseableHttpResponse openConnection(HttpMethod method, String url, Credentials credentials) throws IOException {
        return openConnection(method, url, credentials, (List<NameValuePair>) null);
    }

    public static CloseableHttpResponse openConnection(HttpMethod method, String url, Credentials credentials, String json) throws IOException {
        CloseableHttpResponse response;
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            if (domain.indexOf(".") > 0) {
                String[] tokens = domain.split("\\.");
                domain = tokens[tokens.length - 2] + "." + tokens[tokens.length - 1];
            }
            final CompositeKey key;
            if (credentials != null) {
                key = new CompositeKey(domain, credentials.getUserPrincipal(), credentials.getPassword());
            } else {
                key = new CompositeKey(domain, null);
            }
            synchronized (httpClients) {
                if (!httpClients.containsKey(key)) {
                    //ThreadLocal<CloseableHttpClient> local = new ThreadLocal<>();
                    CredentialsProvider credentialsProvider = null;
                    if (credentials != null) {
                        credentialsProvider = proxyCredentials != null ? proxyCredentials : new BasicCredentialsProvider();
                        credentialsProvider.setCredentials(new AuthScope(uri.getHost(), uri.getPort()), credentials);
                    }
                    CloseableHttpClient httpClient;
                    if (credentialsProvider != null) {
                        httpClient = HttpClients.custom().setDefaultCookieStore(new BasicCookieStore())
                                .setDefaultCredentialsProvider(credentialsProvider)
                                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; …) Gecko/20100101 Firefox/57.0")
                                .build();
                    } else {
                        httpClient = HttpClients.custom()
                                .setDefaultCookieStore(new BasicCookieStore())
                                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; …) Gecko/20100101 Firefox/57.0")
                                .build();
                    }
                    httpClients.put(key, httpClient);
                }
            }
            CloseableHttpClient httpClient = httpClients.get(key);
            HttpRequestBase requestBase;
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
            if (apacheHttpProxy != null) {
                RequestConfig config = RequestConfig.custom().setProxy(apacheHttpProxy).build();
                requestBase.setConfig(config);
            }
            RequestConfig config = requestBase.getConfig();
            if (config != null) {
                Logger.getRootLogger().debug("Details: %s", config.toString());
            }
            response = httpClient.execute(requestBase);
            Logger.getRootLogger().debug("HTTP %s %s returned %s", method.toString(), url, response.getStatusLine().getStatusCode());
        } catch (URISyntaxException | IOException e) {
            Logger.getRootLogger().debug("Could not create connection to %s : %s", url, e.getMessage());
            throw new IOException(e);
        }
        return response;
    }

    public static CloseableHttpResponse openConnection(HttpMethod method, String url, Credentials credentials, List<NameValuePair> parameters) throws IOException {
        CloseableHttpResponse response;
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            if (domain.indexOf(".") > 0) {
                String[] tokens = domain.split("\\.");
                domain = tokens[tokens.length - 2] + "." + tokens[tokens.length - 1];
            }
            final CompositeKey key;
            if (credentials != null) {
                key = new CompositeKey(domain, credentials.getUserPrincipal(), credentials.getPassword());
            } else {
                key = new CompositeKey(domain, null);
            }
            synchronized (httpClients) {
                if (!httpClients.containsKey(key)) {
                    //ThreadLocal<CloseableHttpClient> local = new ThreadLocal<>();
                    CredentialsProvider credentialsProvider = null;
                    if (credentials != null) {
                        credentialsProvider = proxyCredentials != null ? proxyCredentials : new BasicCredentialsProvider();
                        credentialsProvider.setCredentials(new AuthScope(uri.getHost(), uri.getPort()), credentials);
                    }
                    CloseableHttpClient httpClient;
                    if (credentialsProvider != null) {
                        httpClient = HttpClients.custom().setDefaultCookieStore(new BasicCookieStore())
                                .setDefaultCredentialsProvider(credentialsProvider)
                                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; …) Gecko/20100101 Firefox/57.0")
                                .build();
                    } else {
                        httpClient = HttpClients.custom()
                                .setDefaultCookieStore(new BasicCookieStore())
                                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; …) Gecko/20100101 Firefox/57.0")
                                .build();
                    }
                    httpClients.put(key, httpClient);
                }
            }
            CloseableHttpClient httpClient = httpClients.get(key);
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
            if (apacheHttpProxy != null) {
                RequestConfig config = RequestConfig.custom().setProxy(apacheHttpProxy).build();
                requestBase.setConfig(config);
            }
            RequestConfig config = requestBase.getConfig();
            if (config != null) {
                Logger.getRootLogger().debug("Details: %s", config.toString());
            }
            response = httpClient.execute(requestBase);
            Logger.getRootLogger().debug("HTTP %s %s returned %s", method.toString(), url, response.getStatusLine().getStatusCode());
        } catch (URISyntaxException | IOException e) {
            Logger.getRootLogger().debug("Could not create connection to %s : %s", url, e.getMessage());
            throw new IOException(e);
        }
        return response;
    }

    public static String getResponseAsString(String url) throws IOException {
        return getResponseAsString(url, null);
    }

    public static String getResponseAsString(String url, Credentials credentials) throws IOException {
        String result = null;
        try (CloseableHttpResponse response = NetUtils.openConnection(HttpMethod.GET, url, credentials)) {
            if (response != null) {
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
            } else {
                Logger.getRootLogger().warn(String.format("The url %s was not reachable", url));
            }
        }
        return result;
    }

    public static NetStreamResponse getResponseAsStream(String url) throws IOException {
        return getResponseAsStream(url, null);
    }

    public static NetStreamResponse getResponseAsStream(String url, Credentials credentials) throws IOException {
        NetStreamResponse result = null;
        try (CloseableHttpResponse response = NetUtils.openConnection(HttpMethod.GET, url, credentials)) {
            if (response != null) {
                StatusLine statusLine = response.getStatusLine();
                switch (statusLine.getStatusCode()) {
                    case 200:
                        HttpEntity entity = response.getEntity();
                        Header contentType = entity.getContentType();
                        Header[] headers = response.getHeaders("Content-Disposition");
                        String name = null;
                        if (headers.length > 0) {
                            name = headers[0].getValue();
                            if (name.toLowerCase().contains("filename")) {
                                name = name.toLowerCase().substring(name.lastIndexOf("=") + 1).replace("\"", "");
                            }
                        }
                        result = new NetStreamResponse(name, entity.getContentLength(), contentType.getValue(),
                                                       EntityUtils.toByteArray(entity));

                        break;
                    case 401:
                        throw new IOException("401: Unauthorized or the supplied credentials are invalid");
                    default:
                        throw new IOException(String.valueOf(statusLine.getStatusCode()) + ": " + statusLine.getReasonPhrase());
                }
            } else {
                throw new IOException(String.format("Null response (maybe url %s is not reachable", url));
            }
        }
        return result;
    }
}
