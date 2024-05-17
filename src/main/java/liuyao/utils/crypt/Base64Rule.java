package liuyao.utils.crypt;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Base64 编码
 *
 * 原理：8 * n / 6， 不足补位
 *      例1：完整版
 *               文本：              M                |               a               |              n
 *             ASCII：              77               |               97              |             110
 *           二进制位： 0   1   0   0   1   1   0   1 | 1   1   1   0   0   0   0   1 | 0   1   1   0   1   1   1   0
 *  Base64索引二进制位： 0   1   0   0   1   1 | 0   1   1   1   1   0 | 0   0   0   1   0   1 | 1   0   1   1   1   0
 *        Base64索引：           19          |           22          |            5          |            46
 *      Base64编码值：           T           |            W          |            F          |             u
 *
 *      例2：不足24位版
 *               文本：              M                |               a
 *             ASCII：              77               |               97
 *           二进制位： 0   1   0   0   1   1   0   1 | 1   1   1   0   0   0   0   1        二进制补位
 *  Base64索引二进制位： 0   1   0   0   1   1 | 0   1   1   1   1   0 | 0   0   0   1   0   0 | x   x   x   x   x   x
 *        Base64索引：           19          |           22          |            4          |            x
 *      Base64编码值：           T           |            W          |             D         |             =
 *
 *               文本：              M                |
 *             ASCII：              77               |
 *           二进制位： 0   1   0   0   1   1   0   1 |         二进制补位
 *  Base64索引二进制位： 0   1   0   0   1   1 | 0   1   0   0   0   0 | x   x   x   x   x   x | x   x   x   x   x   x
 *        Base64索引：           19          |           32          |            x          |            x
 *      Base64编码值：           T           |            f          |            =         |             =
 *
 *
 */
@SuppressWarnings("unused")
public abstract class Base64Rule {

    public static final int BIT_2_1 = 0b11;
    public static final int BIT_4_1 = 0b1111;
    public static final int BIT_6_1 = 0b111111;
    public static final int BIT_8_1 = 0b11111111;
    protected static final BiConsumer<Integer, Byte> EMPTY_CODE_CALLBACK = (i, b) -> {};

    public static final int calcEncodeLength(byte[] bytes) {
        return bytes.length / 3 * 4 + (bytes.length % 3 > 0 ? 4 : 0);
    }

    public Base64Rule() {
    }

    // 补位 byte
    protected abstract byte getFillByte();

    public final byte[] encode(byte[] bytes) {
        return encode(bytes, EMPTY_CODE_CALLBACK);
    }

    /**
     * 编码
     * @param bytes
     * @param enCallback <Inerger, Byte> Inerger:编码后的下标 Byte:编码后的字节
     * @return
     */
    protected final byte[] encode(byte[] bytes, BiConsumer<Integer, Byte> enCallback) {
        byte[] b64Bs = new byte[calcEncodeLength(bytes)];
        int idx = 2, b64BsIdx = 0, b1, b2, b3;
        for (; idx < bytes.length; idx+=3) {
            b1 = bytes[idx-2] & BIT_8_1; // 前24位置位0,包括符号位
            b2 = bytes[idx-1] & BIT_8_1;
            b3 = bytes[idx] & BIT_8_1;
            // 1：第①个数取前6
            enCallback.accept(b64BsIdx, b64Bs[b64BsIdx] = (byte) (b1 >> 2));
            b64BsIdx++;
            // 2: 第①个数取后2 第②个数取前4
            enCallback.accept(b64BsIdx, b64Bs[b64BsIdx] = (byte) (((b1 & BIT_2_1) << 4) | (b2 >> 4)));
            b64BsIdx++;
            // 3: 第②个数取后4 第③个数取前2
            enCallback.accept(b64BsIdx, b64Bs[b64BsIdx] = (byte) (((b2 & BIT_4_1) << 2) | (b3 >> 6)));
            b64BsIdx++;
            // 2: 第③个数取后6
            enCallback.accept(b64BsIdx, b64Bs[b64BsIdx] = (byte) (b3 & BIT_6_1));
            b64BsIdx++;
        }
        // 补位
        switch (idx - bytes.length) {
            case 0: // 剩余2个
                b1 = bytes[idx-2] & BIT_8_1; // 前一个
                b2 = bytes[idx-1] & BIT_8_1; // 后一个
                enCallback.accept(b64BsIdx, b64Bs[b64BsIdx] = (byte) (b1 >> 2));
                b64BsIdx++;
                enCallback.accept(b64BsIdx, b64Bs[b64BsIdx] = (byte) ((b1 & BIT_2_1) << 4 | (b2 >> 4)));
                b64BsIdx++;
                enCallback.accept(b64BsIdx, b64Bs[b64BsIdx] = (byte) ((b2 & BIT_4_1) << 2));
                b64BsIdx++;
                enCallback.accept(b64BsIdx, b64Bs[b64BsIdx] = -1);
                break;
            case 1: // 剩余1个
                b1 = bytes[idx - 2] & 255;// >> 2
                enCallback.accept(b64BsIdx, b64Bs[b64BsIdx] = (byte) (b1 >> 2)); // 前6位
                b64BsIdx++;
                enCallback.accept(b64BsIdx, b64Bs[b64BsIdx] = (byte) ((b1 & BIT_2_1) << 4)); // 后2位
                b64BsIdx++;
                enCallback.accept(b64BsIdx, b64Bs[b64BsIdx] = -1);
                b64BsIdx++;
                enCallback.accept(b64BsIdx, b64Bs[b64BsIdx] = -1);
                break;
        }
        return b64Bs;
    }

    public final byte[] decode(byte[] bytes) {
        return decode(bytes.length, i -> bytes[i], EMPTY_CODE_CALLBACK);
    }

    /**
     * 解码
     * @param deLen
     * @param getByteFunc <Integer, Byte> Integer: Byte:
     * @param deCallback
     * @return
     */
    protected final byte[] decode(int deLen, Function<Integer, Byte> getByteFunc, BiConsumer<Integer, Byte> deCallback) {
        // 去掉补位后的长度
        while (getFillByte() == getByteFunc.apply(deLen - 1)) {
            deLen--;
        }
        // 补位的长度
        int fillDecodeLen = deLen % 4 - 1;
        fillDecodeLen = fillDecodeLen < 0 ? 0 : fillDecodeLen;
        // 不加补位的组长度
        int decodeGroup = deLen / 4;
        byte[] decode = new byte[decodeGroup * 3 + fillDecodeLen];
        int b1, b2, b3, b4;
        int enIdx = 0, deIdx = 0;
        for (int i = 0; i < decodeGroup; i++) {
            b1 = getByteFunc.apply(enIdx++);
            b2 = getByteFunc.apply(enIdx++);
            b3 = getByteFunc.apply(enIdx++);
            b4 = getByteFunc.apply(enIdx++);
            deCallback.accept( deIdx, decode[deIdx] = (byte) ((b1 << 2) | (b2 >> 4)) );
            deIdx++;
            deCallback.accept( deIdx, decode[deIdx] = (byte) (((b2 & BIT_4_1) << 4) | (b3 >> 2)) );
            deIdx++;
            deCallback.accept( deIdx, decode[deIdx] = (byte) (((b3 & BIT_2_1) << 6) | b4) );
            deIdx++;
        }
        switch (fillDecodeLen) {
            case 2: // 余 3个 转为 2个
                b1 = getByteFunc.apply(enIdx++);
                b2 = getByteFunc.apply(enIdx++);
                b3 = getByteFunc.apply(enIdx);
                deCallback.accept( deIdx, decode[deIdx] = (byte) ((b1 << 2) | (b2 >> 4)) );
                deIdx++;
                deCallback.accept( deIdx, decode[deIdx] = (byte) (((b2 & BIT_4_1) << 4) | (b3 >> 2)) );
                break;
            case 1: // 余 2个 转为 1个
                b1 = getByteFunc.apply(enIdx++);
                b2 = getByteFunc.apply(enIdx);
                deCallback.accept( deIdx, decode[deIdx] = (byte) ((b1 << 2) | (b2 >> 4)) );
                break;
        }
        return decode;
    }

}
