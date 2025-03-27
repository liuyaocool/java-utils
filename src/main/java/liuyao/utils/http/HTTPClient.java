package liuyao.utils.http;

import liuyao.utils.IOUtils;
import liuyao.utils.annotation.NotNull;
import liuyao.utils.annotation.Nullable;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map.Entry;

/**
 * http pool 请求公共类
 */
public class HTTPClient {

    private static final CloseableHttpClient HTTP_CLIENT;

    static {
        /** 指定安全套接字协议 */
        final String PROTOCOL = "TLS";
        /** 提供用于安全套接字包 */
        SSLContext context = null;
        final int RETYR_TIMES = 5;
        /** http 最大连接数 */
        final int MAX_TOTAL = 400;
        /** 到每个主机(ip:port)的最大连接数 */
        final int DEFAULT_MAX_PER_ROUTE = 50;
        /** 默认超时时间 毫秒 创建连接超时 */
        final int CONNECT_TIMEOUT = 5000;
        /** 默认超时时间 毫秒 数据传输超时时间 */
        final int SOCKET_TIMEOUT = 16000;
        final int REQUEST_TIMEOUT = SOCKET_TIMEOUT;

        /** 取消检测SSL 验证效验 */
        X509TrustManager manager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] xcs, String string) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] xcs, String string) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        try {
            context = SSLContext.getInstance(PROTOCOL);
            context.init(null, new TrustManager[]{manager}, new SecureRandom());
        } catch (Exception e) {
        }

        /** 此类是用于主机名验证的基接口 验证主机名和服务器验证方案的匹配是可接受的。 hostname - 主机名 session - 到主机的连接上使用的SSLSession */
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        };

        /** 关闭 HTTP持久连接 */
        HttpResponseInterceptor itcp = new HttpResponseInterceptor() {
            @Override
            public void process(HttpResponse response, HttpContext context)
                    throws HttpException, IOException {
                response.setHeader(HttpHeaders.CONNECTION, "close");
            }
        };

        RequestConfig defaultRequestConfig = getConfig(5000, 16000, null, 0);

        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", SSLConnectionSocketFactory.getSocketFactory()).build();
        PoolingHttpClientConnectionManager manager1 = new PoolingHttpClientConnectionManager(registry);
        manager1.setMaxTotal(MAX_TOTAL);
        manager1.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE);

        HTTP_CLIENT = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig)
                .setSSLHostnameVerifier(hostnameVerifier)
                .setSSLContext(context)
                .setMaxConnTotal(MAX_TOTAL)
                .setMaxConnPerRoute(DEFAULT_MAX_PER_ROUTE)
                .addInterceptorFirst(itcp)
                .setRetryHandler((e, c, ctx) -> c <= RETYR_TIMES)
//                .setConnectionManager(manager1)
                .build();
    }

    public static RequestConfig getConfig(
            int connTimeout, int socketTimeout,
            @Nullable String proxyHost, int proxyPort
    ) {
        RequestConfig.Builder build = RequestConfig.custom();
        if (null != proxyHost) {
            build.setProxy(new HttpHost(proxyHost, proxyPort));
        }
        return build.setConnectTimeout(connTimeout).setSocketTimeout(socketTimeout).build();
    }

    public static HttpRequestBase fillHeaders(HttpRequestBase request, Entry<String, String>... headers) {
        if (null != headers) {
            for (Entry<String, String> header : headers) {
                request.addHeader(header.getKey(), header.getValue());
            }
        }
        return request;
    }

    public static CloseableHttpResponse http(
            @NotNull HttpRequestBase request,
            @Nullable RequestConfig customConfig
    ) throws IOException {
        if (null != customConfig) request.setConfig(customConfig);
        return HTTP_CLIENT.execute(request);
    }

    public static HTTPResponse<String> httpString(
            @NotNull HttpRequestBase http,
            @Nullable RequestConfig customConfig
    ) throws IOException {
        CloseableHttpResponse response = http(http, customConfig);
        HTTPResponse<String> respData = new HTTPResponse<>();
        respData.statusLine = response.getStatusLine().toString();
        respData.statusCode = response.getStatusLine().getStatusCode();
        respData.httpVersion = response.getStatusLine().getProtocolVersion().toString();
        respData.reasonPhrase = response.getStatusLine().getReasonPhrase();
        for (Header h : response.getAllHeaders()) {
            respData.headers.put(h.getName(), h.getValue());
        }
        if (null != response.getEntity())
            respData.body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        IOUtils.close(response);
        http.releaseConnection();
        return respData;
    }

    public static HTTPResponse<String> proxyHttpString(
            @Nullable String proxyHost, int proxyPort,
            @NotNull HttpRequestBase http
    ) throws IOException {
        RequestConfig config = null;
        if (null != proxyHost) {
            config = getConfig(8000, 16000, proxyHost, proxyPort);
        }
        return httpString(http, config);
    }

    public static HTTPResponse<String> proxyHttpPostJsonString(
            String proxyHost, int proxyPort,
            String url, String body, Entry<String, String>... headers) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        fillHeaders(httpPost, headers);
        fillHeaders(httpPost, new HeaderEntry<>(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf-8"));
        return proxyHttpString(proxyHost, proxyPort, httpPost);
    }

    public static HTTPResponse<String> httpPostString(String url, String body, Entry<String, String>... headers) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        fillHeaders(httpPost, headers);
        return httpString(httpPost, null);
    }

    public static HTTPResponse<String> httpJsonPostString(
            String url, String body, Entry<String, String>... headers) throws IOException {
        return proxyHttpPostJsonString(null, 0, url, body, headers);
    }

    public static HTTPResponse<String> httpGet(String url, Entry<String, String>... headers) throws IOException {
        return httpString(fillHeaders(new HttpGet(url), headers), null);
    }

    public static HTTPResponse httpHead(String url, Entry<String, String>... headers) throws IOException {
        return httpString(fillHeaders(new HttpHead(url), headers), null);
    }

}
