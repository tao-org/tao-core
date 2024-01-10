package ro.cs.tao.execution.callback;

import org.apache.http.NameValuePair;

import java.util.List;

public interface ResponseConverter {
    String convert(List<NameValuePair> parameters);
}
