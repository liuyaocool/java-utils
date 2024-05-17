package liuyao.utils;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SerializeUtils {

    public static byte[] serialize(Object o) throws IOException {
        ByteArrayOutputStream baos = null;
        Hessian2Output h2o = null;
        try {
            baos = new ByteArrayOutputStream();
            h2o = new Hessian2Output(baos);
            h2o.writeObject(o);
            h2o.getBytesOutputStream().flush();
            h2o.completeMessage();
            try { h2o.close(); } catch (Exception e) {}
            return baos.toByteArray();
        } finally {
            IOUtils.close(baos);
        }
    }

    public static <T> T deSerialize(byte[] src) throws IOException {
        ByteArrayInputStream bais = null;
        Hessian2Input h2i = null;
        try {
            bais = new ByteArrayInputStream(src);
            h2i = new Hessian2Input(bais);
            return (T) h2i.readObject();
        } finally {
            IOUtils.close(bais);
            if (null != h2i) try { h2i.close(); } catch (Exception e) {}
        }
    }

    public static String serializeString(Object o) throws IOException {
        return new String(Base64.getEncoder().encode(serialize(o)), StandardCharsets.UTF_8);
    }

    public static <T> T deSerializeString(String o) throws IOException {
        return deSerialize(Base64.getDecoder().decode(o.getBytes(StandardCharsets.UTF_8)));
    }

}
