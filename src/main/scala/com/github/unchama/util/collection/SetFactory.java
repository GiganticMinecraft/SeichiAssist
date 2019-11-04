package com.github.unchama.util.collection;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class SetFactory {
    private SetFactory() {

    }

    public static <E> Set<E> of() {
        return new HashSet<>();
    }

    @SafeVarargs
    public static <E> Set<E> of(E... elements) {
        final Set<E> ret = new HashSet<>(elements.length);
        Collections.addAll(ret, elements);
        return ret;
    }
}
