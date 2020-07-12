package com.github.unchama.seichiassist.util;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

public final class TypeConverter {
    // 不必要なインスタンス化を防ぐため封印
    private TypeConverter() {

    }

    //double -> .1double
    public static double Decimal(double d) {
        BigDecimal bi = new BigDecimal(String.valueOf(d));
        return bi.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static int toSecond(int _tick) {
        return _tick / 20;
    }

    public static String toTimeString(int second) {
        final int minute;
        final int hour;
        String time = "";
        // Math.floor は double を返す
        hour = (int) Math.floor(second / 3600);
        second -= hour * 3600;
        minute = Math.round(second / 60);
        if (hour != 0) {
            time = hour + "時間";
        }
        if (minute != 0) {
            time += minute + "分";
        }
		/*
		if(second != 0){
			time = time + second + "秒";
		}
		*/
        return time;
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
