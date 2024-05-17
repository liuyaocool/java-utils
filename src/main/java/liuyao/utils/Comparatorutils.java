package liuyao.utils;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Comparatorutils {

    /**
     * Long compare
     * @param c1
     * @param c2
     * @return
     *  double null return 0
     */
    public static int compare(Comparable c1, Comparable c2) {
        if (c1 == c2) {
            return 0;
        }
        if (null == c1) {
            return -1;
        }
        if (null == c2) {
            return 1;
        }
        return c1.compareTo(c2);
    }

    /**
     * Long compare
     * @param c1
     * @param c2
     * @return
     *  double null return -1
     */
    public static int compareNotNull(Comparable c1, Comparable c2) {
        if (null == c1 && null == c2) {
            return -1;
        }
        return compare(c1, c2);
    }

    public static <T extends Comparable> int compare(T t1, T t2, Function<T, Comparable> func) {
        if (null == t1) {
            return null != t2 ? -1 : 0;
        }
        if (null == t2) {
            return null != t1 ? 1 : 0;
        }
        Comparable c1 = func.apply(t1);
        Comparable c2 = func.apply(t2);
        return compare(c1, c2);
    }

}
