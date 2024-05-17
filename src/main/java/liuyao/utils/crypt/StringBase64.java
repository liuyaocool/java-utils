package liuyao.utils.crypt;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Base64 编码 String 扩展
 *
 */
@SuppressWarnings("unused")
public class StringBase64 extends Base64Rule {

    // 补位 字符
    private char fillChar = '=';
    // 编码表 默认
    private char[] code_table = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };
    // 解码表
    private final Map<Character, Byte> code_table_t = new HashMap<>();

    public StringBase64() {
        init();
    }

    @Override
    protected byte getFillByte() {
        return -1;
    }

    /**
     * 编码表 只取前64个
     * @param codeTable
     * @param replenish
     */
    public StringBase64(char[] codeTable, char fillChar) {
        if (null == codeTable || codeTable.length < 64) {
            throw new IllegalArgumentException("codeTable length is not 64.");
        }
        this.code_table = codeTable;
        this.fillChar = fillChar;
        init();
    }

    public StringBase64(char en_63, char en_64, char fillChar) {
        this.code_table[62] = en_63;
        this.code_table[63] = en_64;
        this.fillChar = fillChar;
        init();
    }

    public void init() {
        for (byte i = 0; i < 64; i++) {
            code_table_t.put(code_table[i], i);
        }
        this.code_table_t.put(fillChar, getFillByte());
    }

    public String toBase64String(byte[] b64Bytes) {
        StringBuilder sb = new StringBuilder(b64Bytes.length);
        for (byte aByte : b64Bytes) {
            if (-1 == aByte) {
                sb.append(fillChar);
            } else {
                sb.append(code_table[aByte]);
            }
        }
        return sb.toString();
    }

    public byte[] toBase64Bytes(String b64Str) {
        if (isBlank(b64Str) || b64Str.length() % 4 > 0) {
            throw new IllegalArgumentException(b64Str + " string is not a base64 string");
        }
        byte[] b64 = new byte[b64Str.length()];
        for (int i = 0; i < b64.length; i++) {
            char key = b64Str.charAt(i);
            if (!code_table_t.containsKey(key)) {
                throw new IllegalArgumentException(key + " is not a base64 char");
            }
            b64[i] = code_table_t.get(key);
        }
        return b64;
    }

    public String encode(String str) {
        return encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    public String encodeToString(byte[] bytes) {
        StringBuilder encode = new StringBuilder(calcEncodeLength(bytes));
        encode(bytes, (i, b) -> {
            if (-1 == b) {
                encode.append(fillChar);
            } else {
                encode.append(code_table[b]);
            }
        });
        return encode.toString();
    }

    public String decode(String encodeStr) {
        return new String(decodeToByte(encodeStr), StandardCharsets.UTF_8);
    }

    public byte[] decodeToByte(String encodeStr) {
        if (isEmpty(encodeStr) || encodeStr.length() % 4 > 0) {
            throw new IllegalArgumentException(encodeStr + " is not a correct base64 string");
        }
        return decode(encodeStr.length(), i -> code_table_t.get(encodeStr.charAt(i)), EMPTY_CODE_CALLBACK);
    }

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isBlank(CharSequence cs) {
        if (cs != null) {
            int length = cs.length();
            for(int i = 0; i < length; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

}
