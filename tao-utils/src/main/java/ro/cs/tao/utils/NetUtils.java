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

    public static final int DEFAULT_CONNECT_TIMEOUT = 10000;
    public static final int DEFAULT_READ_TIMEOUT = 10000;
    private static final AutoEvictableCache<CompositeKey, WrappedCloseableHttpClient> httpClients;
    private static Proxy javaNetProxy;
    private static HttpHost apacheHttpProxy;
    private static CredentialsProvider proxyCredentials;
    private static int timeout = DEFAULT_CONNECT_TIMEOUT;

    static {
        allowSelfSignedCertificates();
        httpClients = new AutoEvictableCache<>(compositeKey -> WrappedCloseableHttpClient.create(), 3600);
    }

    /**
     * Generates a basic authentication token encoded in Base64 format using the provided username and password.
     * The returned token can be used for HTTP Basic Authentication.
     *
     * @param username the username to be included in the token; must not be null or empty
     * @param password the password to be included in the token; must not be null or empty
     * @return the Base64 encoded authentication token, or null if either the username or password is null or empty
     */
    public static String getAuthToken(final String username, final String password) {
        String authToken = null;
        if(isNotEmpty(username) && isNotEmpty(password)){
            authToken = "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes()));
        }
        return authToken;
    }

    /**
     * Configures an HTTP proxy for network connections by specifying the type, host,
     * port, username, and password. Supports authentication and applies the settings
     * for both Java's networking API and Apache HTTP client.
     *
     * @param type the type of the proxy, e.g., "HTTP" or "SOCKS"; must not be null or empty
     * @param host the hostname or IP address of the proxy server; must not be null or empty
     * @param port the port number on which the proxy server is listening
     * @param user the username for proxy authentication; can be null if no authentication is required
     * @param pwd the password for proxy authentication; can be null if no authentication is required
     */
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

    /**
     * Sets a new timeout value for network operations.
     *
     * @param newTimeout the new timeout value in milliseconds; must be a non-negative integer
     */
    public static void setTimeout(int newTimeout) {
        timeout = newTimeout;
    }

    /**
     * Checks the availability of a given URL by sending an HTTP GET request using the provided
     * user credentials. The method determines the availability based on whether the HTTP response
     * code is 200 (OK) or 400 (Bad Request).
     *
     * @param url the URL to verify; must not be null or empty
     * @param user the username to authenticate the request; must not be null or empty
     * @param password the password to authenticate the request; must not be null or empty
     * @return true if the URL is accessible and returns a response code of 200 or 400; false otherwise
     */
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

    /**
     * Configures the application to trust all SSL certificates, including self-signed certificates.
     * This is achieved by disabling SSL validation checks for certificates and hostname verification.
     *
     * Note: This method bypasses standard SSL security checks, creating potential security risks.
     * It should only be used in controlled environments where such risks are acceptable, such as during development
     * or testing. Avoid using this method in production environments.
     *
     * In case of a failure, an error message is logged using the standard Java logging framework.
     */
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

    /**
     * Opens an HTTP connection to the specified URL using a GET request.
     *
     * @param url the URL to connect to; must not be null or empty
     * @return an {@link HttpURLConnection} object corresponding to the open connection
     */
    public static HttpURLConnection openConnection(String url) {
        return openConnection(url, (String) null);
    }

    /**
     * Opens an HTTP connection to the specified URL using the GET method and an authentication token.
     *
     * @param url the URL to connect to; must not be null or empty
     * @param authToken the authentication token to include in the request; must not be null or empty
     * @return an {@link HttpURLConnection} object corresponding to the open connection
     */
    public static HttpURLConnection openConnection(String url, String authToken) {
        return openConnection(HttpMethod.GET, url, authToken, null);
    }

    /**
     * Opens an HTTP connection to the specified URL using the GET method and the provided authentication header.
     *
     * @param url the URL to connect to; must not be null or empty
     * @param authToken the authentication header to include in the request; can be null if no authentication is needed
     * @return an {@link HttpURLConnection} object corresponding to the open connection, or null if the connection could not be opened
     */
    public static HttpURLConnection openConnection(String url, Header authToken) {
        return openURLConnection(HttpMethod.GET, url, authToken, null);
    }

    /**
     * Opens an HTTP connection to the specified URL using the provided HTTP method, authentication token,
     * and additional request properties. The method leverages the internal utility to establish
     * the connection with the specified configuration.
     *
     * @param method the HTTP method to use for the connection (e.g., GET, POST); must not be null
     * @param url the URL to connect to; must not be null or empty
     * @param authToken the authentication token to include in the request; can be null if no authentication is needed
     * @param requestProperties a list of additional request properties to set on the connection;
     *                           can be null or empty if no extra properties are needed
     * @return an {@link HttpURLConnection} object representing the open connection; can return null if the
     *         connection could not be established
     */
    public static HttpURLConnection openConnection(HttpMethod method, String url, String authToken, List<NameValuePair> requestProperties) {
        return openURLConnection(method, url, new BasicHeader("Authorization", authToken), requestProperties);
    }

    /**
     * Opens an HTTP connection to the specified URL using the specified HTTP method, authentication header,
     * and additional request properties. Configures the connection to use a proxy if one is set.
     *
     * @param method the HTTP method to use for the connection (e.g., GET, POST); must not be null
     * @param url the URL to connect to; must not be null or empty
     * @param authHeader the authentication header to include in the request; can be null if no authentication is required
     * @param requestProperties a list of additional request properties to set on the connection;
     *                          can be null or empty if no extra properties are needed
     * @return an {@link HttpURLConnection} object representing the open connection; can return null if the connection
     *         could not be established
     */
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

    /**
     * Opens an HTTP connection to the specified URL using the provided HTTP method
     * and credentials. This method allows for establishing a connection with
     * authentication enabled.
     *
     * @param method the HTTP method to use (e.g., GET, POST); must not be null
     * @param url the URL to connect to; must not be null or empty
     * @param credentials the credentials to authenticate the request; must not be null
     * @return a {@link ro.cs.tao.utils.CloseableHttpResponse} object representing the HTTP response
     * @throws IOException if an I/O error occurs while opening the connection
     */
    public static ro.cs.tao.utils.CloseableHttpResponse openConnection(HttpMethod method, String url, Credentials credentials) throws IOException {
        return openConnection(method, url, credentials, (List<NameValuePair>) null);
    }

    /**
     * Opens a connection to the given URL using the specified HTTP method, credentials, and optional JSON payload.
     *
     * @param method      the HTTP method to be used for the request (e.g., GET, POST)
     * @param url         the URL to which the connection will be made
     * @param credentials the credentials to authenticate the request
     * @param json        the optional JSON payload for POST requests, can be null
     * @return a {@link ro.cs.tao.utils.CloseableHttpResponse} object encapsulating the HTTP response
     * @throws IOException if an I/O error occurs while attempting to create or execute the connection
     */
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

    /**
     * Opens an HTTP connection to the specified URL using the specified HTTP method, headers, and payload.
     *
     * @param method the HTTP method to be used for the connection (e.g., GET, POST).
     * @param url the target URL to which the connection is made.
     * @param header the name of the header to be added to the HTTP request (e.g., Authorization).
     * @param authToken the value of the authorization token to be included in the header.
     * @param json the JSON payload to be sent with the request (applicable for POST method only, can be null).
     * @return a CloseableHttpResponse object encapsulating the HTTP response.
     * @throws IOException if an I/O error occurs during the connection process.
     */
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

    /**
     * Opens an HTTP connection to the specified URL using the given HTTP method, headers, and parameters.
     *
     * @param method the HTTP method to use for the connection, e.g., GET or POST
     * @param url the URL to connect to
     * @param header the request header to include in the connection
     * @param parameters the request parameters to include in the connection, applicable for POST requests
     * @return a {@code ro.cs.tao.utils.CloseableHttpResponse} object representing the HTTP response
     * @throws IOException if an I/O error occurs during the connection process
     */
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

    /**
     * Opens an HTTP connection using the specified method, URL, headers, and parameters.
     *
     * @param method     the HTTP method to be used for the connection (e.g., GET, POST).
     * @param url        the URL to connect to.
     * @param headers    a list of headers to include in the request.
     * @param parameters a list of parameters to include in the request.
     * @return a CloseableHttpResponse object representing the response from the server.
     * @throws IOException if an I/O error occurs during the connection.
     */
    public static ro.cs.tao.utils.CloseableHttpResponse openConnection(HttpMethod method, String url, List<Header> headers, List<NameValuePair> parameters) throws IOException {
        return openConnection(method, url, headers, parameters, timeout);
    }

    /**
     * Opens an HTTP connection with the specified parameters and returns the response.
     *
     * @param method the HTTP method to be used (e.g., GET, POST)
     * @param url the URL to which the connection will be made
     * @param headers a list of headers to include in the request
     * @param parameters a list of parameters to include in the request
     * @param timeoutMillis the timeout value in milliseconds for the connection
     * @return the HTTP response received from making the connection
     * @throws IOException if an input or output error occurs during the connection
     */
    public static ro.cs.tao.utils.CloseableHttpResponse openConnection(HttpMethod method, String url, List<Header> headers, List<NameValuePair> parameters, int timeoutMillis) throws IOException {
        return openConnection(method, url, null, headers, parameters, timeoutMillis);
    }

    /**
     * Opens an HTTP connection using the specified method, URL, credentials, headers, and parameters.
     *
     * @param method the HttpMethod to execute, such as GET or POST
     * @param url the URL to connect to
     * @param credentials the credentials for authentication, or null if no credentials are required
     * @param headers a list of headers to include in the request, or null if no headers should be added
     * @param parameters a list of NameValuePair objects representing the parameters for POST requests, or null if no parameters are required
     * @param timeoutMillis the timeout value in milliseconds for the connection, request, and socket operations
     * @return a CloseableHttpResponse containing the response from the server
     * @throws IOException if an I/O error occurs during the connection or response handling
     */
    public static ro.cs.tao.utils.CloseableHttpResponse openConnection(HttpMethod method, String url, Credentials credentials,List<Header> headers, List<NameValuePair> parameters, int timeoutMillis) throws IOException {
        CloseableHttpResponse response;
        try {
            final URI uri = new URI(url);
            final CloseableHttpClient httpClient;
            if (credentials == null) {
                httpClient = getOrCreateHttpClient(uri, "");
            } else {
                httpClient = getOrCreateHttpClient(uri, credentials);
            }
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

    /**
     * Opens an HTTP connection with the specified method, URL, credentials, and parameters.
     *
     * @param method       the HTTP method to be used (e.g., GET, POST).
     * @param url          the target URL for the connection.
     * @param credentials  the credentials to be used for authentication.
     * @param parameters   the list of additional parameters to be included in the request.
     * @return a CloseableHttpResponse object representing the response from the connection.
     * @throws IOException if an I/O error occurs during the connection.
     */
    public static ro.cs.tao.utils.CloseableHttpResponse openConnection(HttpMethod method, String url, Credentials credentials, List<NameValuePair> parameters) throws IOException {
        return openConnection(method, url, credentials, parameters, timeout);
    }

    /**
     * Opens an HTTP connection to a specified URL using the provided method, credentials,
     * parameters, and timeout settings. Configures the request and executes it, returning the
     * HTTP response received from the server.
     *
     * @param method         the HTTP method to be used (e.g., GET, POST)
     * @param url            the URL to which the connection should be made
     * @param credentials    the credentials for authentication, if required
     * @param parameters     the list of parameters to include in the request, applicable for
     *                       certain HTTP methods like POST (can be null)
     * @param timeoutMillis  the timeout in milliseconds for the connection, socket, and
     *                       connection request
     * @return a CloseableHttpResponse object containing the HTTP response
     * @throws IOException   if an I/O error occurs during the connection or request execution
     */
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

    /**
     * Retrieves the response from the specified URL as a String.
     *
     * @param url the URL from which the response is to be fetched
     * @return the response content as a String
     * @throws IOException if an I/O error occurs during the request
     */
    public static String getResponseAsString(String url) throws IOException {
        return getResponseAsString(url, null, null);
    }

    /**
     * Sends an HTTP GET request to the specified URL with the provided credentials
     * and parameters, and returns the response content as a string.
     *
     * @param url the target URL to send the HTTP GET request
     * @param credentials the credentials used for authentication
     * @param parameters the list of name-value pairs to be included as request parameters
     * @return the response content as a string, or null if the request fails or is unauthorized
     * @throws IOException if an I/O error occurs while performing the request
     */
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

    /**
     * Sends an HTTP request to the specified URL with the given parameters and returns the response as a String.
     *
     * @param url the target URL to which the request will be sent
     * @param parameters a list of name-value pairs to be included as parameters in the request
     * @return the response from the server as a String
     * @throws IOException if an I/O error occurs during the request or response handling
     */
    public static String getResponseAsString(String url, List<NameValuePair> parameters) throws IOException {
        return getResponseAsString(url, null, parameters);
    }

    /**
     * Retrieves the response of an HTTP request to the specified URL as a stream.
     *
     * @param url the URL to send the HTTP request to
     * @return a NetStreamResponse object containing the response as a stream
     * @throws IOException if an I/O error occurs during the request
     */
    public static NetStreamResponse getResponseAsStream(String url) throws IOException {
        return getResponseAsStream(url, null);
    }

    /**
     * Retrieves the response from the specified URL as a stream wrapped in a NetStreamResponse object.
     * This method performs an HTTP GET request to the given URL using the provided credentials.
     *
     * @param url the URL to connect to and retrieve the response from
     * @param credentials the credentials used for authenticating the connection
     * @return a NetStreamResponse object containing the response stream, content type, file name,
     *         and content length
     * @throws IOException if an I/O error occurs while connecting or retrieving the response,
     *                     or if the server returns an HTTP error code
     */
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

    /**
     * Retrieves a list of cookies for the specified URL using the provided credentials.
     *
     * @param url the URL for which cookies need to be retrieved
     * @param credentials the credentials containing the username and password for authentication
     * @return a list of cookies associated with the specified URL
     * @throws IOException if an I/O error occurs while retrieving the cookies
     */
    public static List<Cookie> getCookies(String url, Credentials credentials) throws IOException {
        return getCookies(url, credentials.getUserPrincipal().getName() + ":" + credentials.getPassword());
    }

    /**
     * Retrieves a list of cookies associated with a given URL and authentication token.
     * If a corresponding HTTP client is found, it extracts cookies from the client's cookie store,
     * filtering out cookies with null or empty values. If no HTTP client is available, an empty
     * list of cookies is returned.
     *
     * @param url the URL for which cookies are to be retrieved
     * @param authToken the authentication token used to identify the HTTP client
     * @return a list of cookies associated with the given URL and authentication token;
     *         returns an empty list if no HTTP client is found
     * @throws IOException if there is a problem parsing the URL or retrieving cookies
     */
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
