package com.github.unchama.util;

/**
 * 単一の情報量を持たない値が存在するようなシングルトンクラス
 */
public final class Unit {
    private Unit() {}

    public static final Unit instance = new Unit();
}
