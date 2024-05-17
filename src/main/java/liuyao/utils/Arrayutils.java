package liuyao.utils;

@SuppressWarnings("unused")
public class Arrayutils {

    private static final char[] HEX_CHAR = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'
    };

    public static String toHexString(byte[] data) {
        if (null == data) {
            return "";
        }
        char[] hexChars = new char[data.length * 2];
        int idx = 0;
        for (byte bi : data) {
            // todo 高位取模优化
            hexChars[idx++] = HEX_CHAR[(0xFF & bi) >> 4];
            hexChars[idx++] = HEX_CHAR[0xF & bi];
        }
        return new String(hexChars);
    }
}
