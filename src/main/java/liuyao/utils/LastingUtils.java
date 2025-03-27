package liuyao.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 持久化工具类
 */
public class LastingUtils {

    private static final String RESOURCE_PATH = "/tmp/LY_LastingUtils_";

    public static void saveKey(String key, String value) throws IOException {
        ArrayList<String> contene = new ArrayList<>();
        contene.add(value);
        IOUtils.coverFile(new File(RESOURCE_PATH+key), contene, StandardCharsets.UTF_8);
    }

    public static String getKey(String key) throws IOException {
        String[] text = new String[1];
        IOUtils.readTextFile(new File(RESOURCE_PATH+key), line -> {
            text[0] = line;
        });
        return text[0];
    }

    public static void main(String[] args) throws IOException {
        System.out.println(getKey("aaa"));
    }
}
