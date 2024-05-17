package liuyao.utils;

public class Numberutils {

    /**
     * Number equals
     *
     * @param l1
     * @param l2
     * @return double null return true
     */
    public static boolean equals(Number l1, Number l2) {
        return l1 == l2 || (null != l1 && null != l2 ? l1.equals(l2) : false);
    }

    /**
     * Number equals
     *
     * @param l1
     * @param l2
     * @return double null return false
     */
    public static boolean equalsNotNull(Number l1, Number l2) {
        if (null == l1 && null == l2) {
            return false;
        }
        return equals(l1, l2);
    }

    private static enum NumberType {
        INT, DOUBLE, NULL;
    }

    //判断是否是数字
    private static NumberType isNumber(String str) {
        if (null == str || str.isEmpty()) {
            return NumberType.NULL;
        }
        int point = 0;
        for (int i = 0; i < str.length(); i++) {
            if ('.' == str.charAt(i)) {
                point++;
            } else if (str.charAt(i) < 48 || str.charAt(i) > 57) {
                return NumberType.NULL;
            }
        }
        switch (point) {
            case 0:
                return NumberType.INT;
            case 1:
                return NumberType.DOUBLE;
            default:
                return NumberType.NULL;
        }
    }

}
