package ro.cs.tao.utils;

import org.apache.http.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.util.Locale;

/**
 * Since the CloseableHttpClient is internal to NetUtils, it needs not to close in an internal try-with-resources,
 * otherwise chunked responses will fail to be all read. Instead, NetUtils returns this wrapper that will eventually
 * close both the response and the client.
 */
public class CloseableHttpResponse implements org.apache.http.client.methods.CloseableHttpResponse {

    private final CloseableHttpClient httpClient;
    private final org.apache.http.client.methods.CloseableHttpResponse httpResponse;
    private final boolean closeClientOnResponseClose;

    CloseableHttpResponse(CloseableHttpClient httpClient, org.apache.http.client.methods.CloseableHttpResponse httpResponse, boolean keepOpen) throws IOException {
        this.httpClient = httpClient;
        if (httpResponse == null) {
            throw new IOException("Null response (maybe url is not reachable)");
        }
        this.httpResponse = httpResponse;
        this.closeClientOnResponseClose = keepOpen;
    }

    @Override
    public StatusLine getStatusLine() {
        return httpResponse.getStatusLine();
    }

    @Override
    public void setStatusLine(StatusLine statusLine) {
        httpResponse.setStatusLine(statusLine);
    }

    @Override
    public void setStatusLine(ProtocolVersion protocolVersion, int i) {
        httpResponse.setStatusLine(protocolVersion, i);
    }

    @Override
    public void setStatusLine(ProtocolVersion protocolVersion, int i, String s) {
        httpResponse.setStatusLine(protocolVersion, i, s);
    }

    @Override
    public void setStatusCode(int i) throws IllegalStateException {
        httpResponse.setStatusCode(i);
    }

    @Override
    public void setReasonPhrase(String s) throws IllegalStateException {
        httpResponse.setReasonPhrase(s);
    }

    @Override
    public HttpEntity getEntity() {
        return httpResponse.getEntity();
    }

    @Override
    public void setEntity(HttpEntity httpEntity) {
        httpResponse.setEntity(httpEntity);
    }

    @Override
    public Locale getLocale() {
        return httpResponse.getLocale();
    }

    @Override
    public void setLocale(Locale locale) {
        httpResponse.setLocale(locale);
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return httpResponse.getProtocolVersion();
    }

    @Override
    public boolean containsHeader(String s) {
        return httpResponse.containsHeader(s);
    }

    @Override
    public Header[] getHeaders(String s) {
        return httpResponse.getHeaders(s);
    }

    @Override
    public Header getFirstHeader(String s) {
        return httpResponse.getFirstHeader(s);
    }

    @Override
    public Header getLastHeader(String s) {
        return httpResponse.getLastHeader(s);
    }

    @Override
    public Header[] getAllHeaders() {
        return httpResponse.getAllHeaders();
    }

    @Override
    public void addHeader(Header header) {
        httpResponse.addHeader(header);
    }

    @Override
    public void addHeader(String s, String s1) {
        httpResponse.addHeader(s, s1);
    }

    @Override
    public void setHeader(Header header) {
        httpResponse.setHeader(header);
    }

    @Override
    public void setHeader(String s, String s1) {
        httpResponse.setHeader(s, s1);
    }

    @Override
    public void setHeaders(Header[] headers) {
        httpResponse.setHeaders(headers);
    }

    @Override
    public void removeHeader(Header header) {
        httpResponse.removeHeader(header);
    }

    @Override
    public void removeHeaders(String s) {
        httpResponse.removeHeaders(s);
    }

    @Override
    public HeaderIterator headerIterator() {
        return httpResponse.headerIterator();
    }

    @Override
    public HeaderIterator headerIterator(String s) {
        return httpResponse.headerIterator(s);
    }

    @Override
    @Deprecated
    public HttpParams getParams() {
        return httpResponse.getParams();
    }

    @Override
    @Deprecated
    public void setParams(HttpParams httpParams) {
        httpResponse.setParams(httpParams);
    }

    @Override
    public void close() throws IOException {
        httpResponse.close();
        if (this.closeClientOnResponseClose) {
            httpClient.close();
        }
    }
}
