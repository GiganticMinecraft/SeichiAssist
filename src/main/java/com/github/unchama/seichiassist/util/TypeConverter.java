package com.github.unchama.seichiassist.util;

import java.math.BigDecimal;
import java.util.Optional;

public final class TypeConverter {
	// 不必要なインスタンス化を防ぐため封印
	private TypeConverter() {

	}

	//String -> double
	public static double toDouble(String s){
		return Double.parseDouble(s);
	}

	//String -> int
	public static int toInt(String s) {
		return Integer.parseInt(s);
	}

	//double -> .1double
	public static double Decimal(double d) {
		BigDecimal bi = new BigDecimal(String.valueOf(d));
		return bi.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static int toSecond(int _tick){
		return _tick/20;
	}

	public static String toTimeString(int second) {
		final int minute;
		final int hour;
		String time = "";
		// Math.floor は double を返す
		hour = (int) Math.floor(second / 3600);
		second -= hour * 3600;
		minute = Math.round(second / 60);
		if(hour != 0){
			time = hour + "時間";
		}
		if(minute != 0){
			time += minute + "分";
		}
		/*
		if(second != 0){
			time = time + second + "秒";
		}
		*/
		return time;
	}

	//boolean -> int
	public static int toInt(boolean flag) {
		if(flag){
			return 1;
		}else{
			return 0;
		}
	}

	public static Optional<Integer> tryToParseInt(String parsed) {
		try {
			return Optional.of(Integer.parseInt(parsed));
		} catch (NumberFormatException ex) {
			return Optional.empty();
		}
	}

}
