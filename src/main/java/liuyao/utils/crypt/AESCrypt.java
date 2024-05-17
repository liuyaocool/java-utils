package liuyao.utils.crypt;

import javax.crypto.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * AES加密工具
 */
public class AESCrypt {

    /** 密钥长度: 128, 192 or 256 */
    private static final int KEY_SIZE = 128;
    /** 加密/解密算法名称 */
    private static final String ALGORITHM = "AES";
    /** 随机数生成器（RNG）算法名称 */
    private static final String RNG_ALGORITHM = "SHA1PRNG";

    private byte[] secret;
    private Cipher encryptCipher;
    private Cipher decryptCipher;

    public AESCrypt(String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public AESCrypt init() {
        if (null != encryptCipher) {
            return this;
        }
        synchronized (this) {
            if (null != encryptCipher) {
                return this;
            }
            // 创建安全随机数生成器
            try {
                SecureRandom random = SecureRandom.getInstance(RNG_ALGORITHM);
                // 设置 密钥key的字节数组 作为安全随机数生成器的种子
                random.setSeed(this.secret);
                // 创建 AES算法生成器
                KeyGenerator gen = KeyGenerator.getInstance(ALGORITHM);
                // 初始化算法生成器
                gen.init(KEY_SIZE, random);
                // 生成 AES密钥对象, 也可以直接创建密钥对象: return new SecretKeySpec(key, ALGORITHM);
                SecretKey secretKey = gen.generateKey();

                // 加密器
                this.encryptCipher = Cipher.getInstance(ALGORITHM);
                this.encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);

                // 解密器
                this.decryptCipher = Cipher.getInstance(ALGORITHM);
                this.decryptCipher.init(Cipher.DECRYPT_MODE, secretKey);

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
            return this;
        }
    }

    public byte[] encrypt(byte[] bytes) {
        init();
        return doFinal(this.encryptCipher, bytes);
    }

    public byte[] decrypt(byte[] bytes) {
        init();
        return doFinal(this.decryptCipher, bytes);
    }

    private byte[] doFinal(Cipher cipher, byte[] data) {
        try {
            return cipher.doFinal(data);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

}