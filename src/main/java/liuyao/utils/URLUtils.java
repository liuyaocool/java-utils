package liuyao.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

public class URLUtils {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    // 补位字符
    private static final byte COVER = '@';

    // '+' --> '-'
    // '/' --> '_'
    // '=' --> '@'
    public static String encode(String url) {
        if (Stringutils.isBlank(url)) return "";
        return new String(encode(url.getBytes(CHARSET)), CHARSET);
    }

    public static String decode(String str) {
        if (Stringutils.isBlank(str)) return "";
        return new String(decode(str.getBytes(CHARSET)), CHARSET);
    }

    public static byte[] encode(byte[] url) {
        return coverReplace(Base64.getUrlEncoder().encode(url), (byte)'=', COVER);
    }

    public static byte[] decode(byte[] str) {
        return Base64.getUrlDecoder().decode(coverReplace(str, COVER, (byte)'='));
    }

    private static byte[] coverReplace(byte[] bytes, byte src, byte target) {
        if (src == bytes[bytes.length-1]) {
            bytes[bytes.length-1] = target;
        }
        if (src == bytes[bytes.length-2]) {
            bytes[bytes.length-2] = target;
        }
        return bytes;
    }

    public static void main(String[] args) {
        // 62, 63
        int i62_63[] = {0b00111110, 0b00111111};
        int or[] = {0, 0b01, 0b10, 0b11};

        // "ÿ".getBytes() 不是字符的ascii码数字
        byte[] str = new byte[i62_63.length * or.length * 3 - 1];

        // 62:248~251 63:252~255
        for (int i = 0, tmp, idx = 0; i < i62_63.length; i++) {
            for (int j = 0; j < or.length; j++) {
                tmp = i62_63[i] << 2 ^ or[j];
                str[idx++] = (byte) tmp;
                str[idx++] = (byte) (idx * j);
                // str[idx++] = (byte) (idx * tmp);
            }
        }

        byte[] encodeByte = encode(str);
        String encode = new String(encodeByte, CHARSET);
        System.out.println("en: " + encode);

        String decode = decode(encode);
        System.out.println("equals: " + decode.equals(new String(str, CHARSET)));

        Random random = new Random();
        for (int i = 1; i < 32; i++) {
            str = new byte[i];
            random.nextBytes(str);
            // System.out.format("len%s: %s\n", i, new String(encode(str), CHARSET));
        }

        System.out.println("=: " + (int) '=');
        System.out.println("@: " + (int) '@');

    }
}
