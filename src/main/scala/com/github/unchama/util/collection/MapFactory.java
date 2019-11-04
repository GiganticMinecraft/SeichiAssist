package com.github.unchama.util.collection;

import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public final class MapFactory {
    private MapFactory() {
    }

    public static <K, V> Map<K, V> of() {
        return new HashMap<>();
    }

    @SafeVarargs
    public static <K, V> Map<K, V> of(Pair<K, V>... mappings) {
        final Map<K, V> resultMap = MapFactory.of();
        for (final Pair<K, V> mapping : mappings) {
            resultMap.put(mapping.getKey(), mapping.getRight());
        }
        return resultMap;
    }
}
