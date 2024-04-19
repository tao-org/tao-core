package ro.cs.tao.execution.callback;

import org.apache.http.NameValuePair;

import java.util.List;

public interface CallbackClient {
    default void setConverter(ResponseConverter converter) { }
    int call(List<NameValuePair> params);
}
