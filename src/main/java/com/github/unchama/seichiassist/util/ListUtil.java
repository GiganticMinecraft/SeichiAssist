package com.github.unchama.seichiassist.util;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by karayuu on 2019/05/03
 */
public class ListUtil {
    @SuppressWarnings("all")
    public static <T> List<T> addAll(List<T> base, @Nullable List<T> add) {
        add.forEach(element -> base.add(element));
        return base;
    }
}
