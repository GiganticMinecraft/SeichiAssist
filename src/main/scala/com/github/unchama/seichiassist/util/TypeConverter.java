package com.github.unchama.seichiassist.util;

public final class TypeConverter {
    // 不必要なインスタンス化を防ぐため封印
    private TypeConverter() {

    }

    public static long toSecond(long _tick) {
        return _tick / 20L;
    }

    public static String toTimeString(long seconds) {
        final long totalMinutes = seconds / 60L;
        final long hours = totalMinutes / 60L;
        final long minutes = totalMinutes % 60L;

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
