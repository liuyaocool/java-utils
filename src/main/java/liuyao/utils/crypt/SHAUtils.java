package liuyao.utils.crypt;

import liuyao.utils.Arrayutils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("unused")
public class SHAUtils {

    public static String sha256ToHex(String str) {
        return Arrayutils.toHexString(sha256(str.getBytes(StandardCharsets.UTF_8)));
    }

    public static byte[] sha256(byte[] data) {
        return sha2(data, "SHA-256");
    }

    public static String sha512ToHex(String str) {
        return Arrayutils.toHexString(sha512(str.getBytes(StandardCharsets.UTF_8)));
    }

    private static byte[] sha512(byte[] data) {
        return sha2(data, "SHA-512");
    }

    protected static byte[] sha2(byte[] data, String type) {
        MessageDigest sha256 = null;
        try {
            // 非线程安全
            sha256 = MessageDigest.getInstance(type);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        sha256.update(data);
        return sha256.digest();
    }
}
