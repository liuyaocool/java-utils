package liuyao.utils.crypt;

import liuyao.utils.URLUtils;
import liuyao.utils.func.BiConsumerEx;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

/**
 * java 支持公钥加密的 spec:
 *  Only RSAPublicKeySpec and X509EncodedKeySpec supported for RSA public keys
 *
 * java 支持私钥加密的 spec:
 *  Only RSAPrivate(Crt)KeySpec and PKCS8EncodedKeySpec supported for RSA private keys
 *
 *   RSA加解密中必须考虑到的密钥长度、明文长度和密文长度问题。明文长度需要小于密钥长度，而密文长度则等于密钥长度。
 * 因此当加密内容长度大于密钥长度时，有效的RSA加解密就需要对内容进行分段。
 *   这是因为，RSA算法本身要求加密内容也就是明文长度m必须0<m<密钥长度n。如果小于这个长度就需要进行padding，因为
 * 如果没有padding，就无法确定解密后内容的真实长度，字符串之类的内容问题还不大，以0作为结束符，但对二进制数据就很
 * 难，因为不确定后面的0是内容还是内容结束符。而只要用到padding，那么就要占用实际的明文长度，于是实际明文长度需要
 * 减去padding字节长度。我们一般使用的padding标准有NoPPadding、OAEPPadding、PKCS1Padding等，其中PKCS#1建
 * 议的padding就占用了11个字节。
 *   这样，对于1024长度的密钥。128字节（1024bits）-减去11字节正好是117字节，但对于RSA加密来讲，padding也是参
 * 与加密的，所以，依然按照1024bits去理解，但实际的明文只有117字节了。
 *   所以如果要对任意长度的数据进行加密，就需要将数据分段后进行逐一加密，并将结果进行拼接。同样，解码也需要分段解
 * 码，并将结果进行拼接。
 */
public class RSACrypt {

    public static String[] gainKey() throws Exception {
        // KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = keyPairGen = KeyPairGenerator.getInstance("RSA");
        // 初始化密钥对生成器，密钥大小为96-1024位
        keyPairGen.initialize(1024, new SecureRandom());
//        keyPairGen.initialize(512);
        // 生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();   // 得到私钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();  // 得到公钥
        return new String[] {
                new String(URLUtils.encode(publicKey.getEncoded()), StandardCharsets.UTF_8),
                new String(URLUtils.encode(privateKey.getEncoded()), StandardCharsets.UTF_8)
        };
    }

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public RSACrypt() { }

    public RSACrypt init(String pubKey, String priKey) throws InvalidKeySpecException{
        byte[] pubKeyByte = URLUtils.decode(pubKey.getBytes(StandardCharsets.UTF_8));
        byte[] priKeyByte = URLUtils.decode(priKey.getBytes(StandardCharsets.UTF_8));

        PKCS8EncodedKeySpec priKeySpec = new PKCS8EncodedKeySpec(priKeyByte);
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubKeyByte);

        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        this.privateKey = keyFactory.generatePrivate(priKeySpec);
        this.publicKey = keyFactory.generatePublic(pubKeySpec);
        return this;
    }

    public String privateEncrypt(String str ) throws Exception {
        return RSAEncrypt(str, this.privateKey);
    }

    public String publicDecrypt(String str ) throws Exception {
        return RSADecrypt(str, this.publicKey);
    }

    public String publicEncrypt(String str ) throws Exception {
        return RSAEncrypt(str, this.publicKey);
    }

    public String privateDecrypt(String str) throws Exception {
        return RSADecrypt(str, this.privateKey);
    }

    //RSA加密
    protected String RSAEncrypt(String str, Key applyKey) throws Exception {
        int dataMaxLen = ((RSAKey) applyKey).getModulus().bitLength() / 8 - 11; // 去掉 11个padding 占用字符
        // cipher 不是线程安全的， 这里不能多线程共享
        Cipher cipher = cipherGen(Cipher.ENCRYPT_MODE, applyKey);

        byte[] data = str.getBytes(StandardCharsets.UTF_8);
        byte[] encryptBytes = new byte[data.length / dataMaxLen * 128 + (data.length % dataMaxLen > 0 ? 128 : 0)];
        final int[] idx = {0};
        // 要加密字符串过长 需要分段加密
        splitByte(data, dataMaxLen, (position, length) -> {
            byte[] enBytes = cipher.doFinal(data, position, length);
            print("en ---> allLen: %s  splitLen: %s  enlen: %s  idx: %s\n", encryptBytes.length, length, enBytes.length, idx[0]);
            System.arraycopy(enBytes, 0, encryptBytes, idx[0], enBytes.length);
            idx[0] += enBytes.length;
        });
        print("-- strLen: %s \n", data.length);

        return new String(URLUtils.encode(encryptBytes), StandardCharsets.UTF_8);
    }

    // RSA解密
    protected String RSADecrypt(String str, Key applyKey) throws Exception {
        // 加密后数据是定长的
        int keylen = ((RSAKey) applyKey).getModulus().bitLength() / 8;
        Cipher cipher = cipherGen(Cipher.DECRYPT_MODE, applyKey);

        // 解密 要加密字符串过长 分开解密问题，且最后一部分不一定是最大可加密长度
        List<byte[]> datas = new ArrayList<>();
        final int[] deTotalLen = {0};
        byte[] decodeData = URLUtils.decode(str.getBytes(StandardCharsets.UTF_8));
        print("-- strLen: %s  strDeLen: %s \n", str.length(), decodeData.length);
        splitByte(decodeData, keylen, (position, length) -> {
            byte[] deBytes = cipher.doFinal(decodeData, position, length);
            print("de ---> allLen: %s  splitLen: %s  delen: %s  idx: %s\n", deTotalLen[0], length, deBytes.length, datas.size());
            datas.add(deBytes);
            deTotalLen[0] += deBytes.length;
        });
        byte[] decryptBytes = new byte[deTotalLen[0]];
        int idx = 0;
        for (byte[] data : datas) {
            System.arraycopy(data, 0, decryptBytes, idx, data.length);
            idx += data.length;
        }
        return new String(decryptBytes, StandardCharsets.UTF_8);
    }

    protected Cipher cipherGen(int mode, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(mode, key);
        return cipher;
    }

    private void print(String format, Object... param) {
//        System.out.format(format, param);
    }

    private void splitByte(byte[] bytes, int len, BiConsumerEx<Integer, Integer> call) throws Exception {
        int dataLen = bytes.length;
        for (int i = 0; i < dataLen; i+=len) {
            call.accept(i, Math.min(len, dataLen - i));
        }
    }

}
