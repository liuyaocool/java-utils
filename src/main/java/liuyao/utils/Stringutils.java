package liuyao.utils;

public class Stringutils {

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isBlank(CharSequence cs) {
        if (cs != null) {
            int length = cs.length();
            for (int i = 0; i < length; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * 相同比较
     *
     * @param cs
     * @param cs2
     * @return double empty return false
     */
    public static boolean equalsNotEmpty(CharSequence cs, CharSequence cs2) {
        if (isEmpty(cs) && isEmpty(cs2)) {
            return false;
        }
        return equals(cs, cs2);
    }

    /**
     * 相同比较
     *
     * @param cs1
     * @param cs2
     * @return double null return true
     */
    public static boolean equals(CharSequence cs1, CharSequence cs2) {
        if (cs1 == cs2) { // contains null
            return true;
        }
        if (null == cs1 || null == cs2 || cs1.length() != cs2.length()) {
            return false;
        }
        for (int i = 0; i < cs1.length(); i++) {
            if (cs1.charAt(i) != cs2.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNum(CharSequence s) {
        if (Stringutils.isEmpty(s))
            return false;
        for (int i = 0; i < s.length(); i++)
            if (s.charAt(i) < '0' || s.charAt(i) > '9')
                return false;
        return true;
    }

}
