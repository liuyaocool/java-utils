import liuyao.utils.crypt.AESCrypt;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AesTest {


    public static void main(String[] args) throws InterruptedException {
        String uid = UUID.randomUUID().toString();

        Map<String, byte[][]> result = new HashMap<>();

        Thread[] threads = new Thread[10_00];
        for (int i = 0; i < threads.length; i++) {
            int finalI = i;
            final AESCrypt aesUtil = new AESCrypt(uid);
            threads[i] = new Thread(() -> test(result, aesUtil, "hello"+ finalI));
        }

        long t = System.currentTimeMillis();
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        long cost = System.currentTimeMillis() - t;

        printResult(result);

        System.out.println(threads.length + "次 花费(ms):" + cost);
        System.out.println(uid);

//        ExecutorService exe = Executors.newCachedThreadPool();
//        for (int i = 0; i < 100; i++) {
//            int finalI = i;
//            exe.submit(() -> test(aesUtil, "hello"+ finalI));
//        }

    }

    private static void printResult(Map<String, byte[][]> result) {
        for (String str : result.keySet()) {
            String x = new String(result.get(str)[1]);
            if (str.equals(x)) {
                continue;
            }
            System.out.print(str);
            System.out.print(" -加密-> ");
            System.out.print(new String(result.get(str)[0]));
            System.out.print(" -解密-> ");
            System.out.println(x);
        }
    }

    private static void test(Map<String, byte[][]> result, AESCrypt aesUtil, String str) {
        /*
        并不是每个字节数和编码集上的字符都有对应关系,如果一个字节数在编码集上没有对应,
        编码new String(byte[]) 后,往往解出来的会是一些乱码无意义的符号:例如:��
         */
        byte[] bytes = str.getBytes();
        byte[] encryptByte = aesUtil.encrypt(bytes);
        byte[] decryptByte = aesUtil.decrypt(encryptByte);
        result.put(str, new byte[][]{encryptByte, decryptByte});
    }
}
