import liuyao.utils.SerializeUtils;
import liuyao.utils.http.HTTPClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SerTest {

    public static void main(String[] args) throws IOException {

        HTTPResponse resp = new HTTPResponse<>();
        resp.body = "adf";
        resp.httpVersion = "1.1";
        resp.statusCode = 200;
        resp.reasonPhrase = "OK";

        System.out.println(resp.toString());
        System.out.println(resp);
        String aaaa = "adfadf";

        System.out.println(HTTPClient.class.getName());

        String s = SerializeUtils.serializeString(aaaa);
        System.out.println(s);
        String o = SerializeUtils.deSerializeString(s);
        System.out.println(o);


        String serialize = SerializeUtils.serializeString(resp);

        System.out.println(serialize);

        HTTPResponse t = SerializeUtils.deSerializeString(serialize);
        System.out.println(t);
        System.out.println(t == resp);
        System.out.println(t.httpVersion);

    }

}
