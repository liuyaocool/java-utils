import liuyao.utils.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Test {


    public static void main(String[] args) throws IOException {
        String path = "/tmp/testaaa";
        for (int i = 0; i < 10; i++) {
            IOUtils.writeToFile(path,
                    (UUID.randomUUID().toString() + "\n").getBytes(StandardCharsets.UTF_8));
        }
        IOUtils.closeWriteChannel(path);
    }

}
