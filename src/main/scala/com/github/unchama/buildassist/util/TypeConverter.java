package com.github.unchama.buildassist.util;

public final class TypeConverter {
    // 不必要なインスタンス化を防ぐため封印
    private TypeConverter() {

    }

    //String -> double
    public static double toDouble(String s) {
        return Double.parseDouble(s);
    }

    //String -> int
    public static int toInt(String s) {
        return Integer.parseInt(s);
    }

}