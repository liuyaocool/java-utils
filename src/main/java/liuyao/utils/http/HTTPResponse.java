package liuyao.utils.http;

import java.util.HashMap;
import java.util.Map;

public class HTTPResponse<BODY> {

    public String statusLine;
    public int statusCode;
    public String httpVersion;
    public String reasonPhrase;
    public final Map<String, String> headers = new HashMap<>();
    public BODY body;

    public HTTPResponse() {}

    public void putHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public String getHeader(String key) {
        return this.headers.get(key);
    }

}