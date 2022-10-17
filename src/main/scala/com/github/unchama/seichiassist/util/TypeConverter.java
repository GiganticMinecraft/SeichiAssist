package com.github.unchama.seichiassist.util;

public final class TypeConverter {
    // 不必要なインスタンス化を防ぐため封印
    private TypeConverter() {

    }

    public static int toSecond(int _tick) {
        return _tick / 20;
    }

    public static String toTimeString(int seconds) {
        final int totalMinutes = seconds / 60;
        final int hours = totalMinutes / 60;
        final int minutes = totalMinutes % 60;

        return (hours == 0 ? "" : hours + "時間") + minutes + "分";
    }

    /**
     * 与えられた文字列がintに変換できるかどうかを判定する
     * 実際の変換結果を捨て変換可能であるかのみを見たいときに有用
     *
     * @param string パースを試みる文字列
     * @return 変換可能性
     */
    public static boolean isParsableToInteger(final String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
