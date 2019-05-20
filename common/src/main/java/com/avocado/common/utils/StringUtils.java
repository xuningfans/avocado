package com.avocado.common.utils;

/**
 * StringUtils class
 *
 * @author xuning
 * @date 2019-05-17 09:54
 */
public class StringUtils {
    public static boolean equalsAny(String operation, String... str) {
        if (operation == null) {
            return false;
        }
        for (String s : str) {
            if (operation.equals(s)) {
                return true;
            }
        }

        return false;
    }
}
