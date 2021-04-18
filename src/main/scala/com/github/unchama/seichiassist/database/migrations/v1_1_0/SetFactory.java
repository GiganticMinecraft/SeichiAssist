package com.github.unchama.seichiassist.database.migrations.v1_1_0;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class SetFactory {
    private SetFactory() {

    }

    @SafeVarargs
    public static <E> Set<E> of(E... elements) {
        final Set<E> ret = new HashSet<>(elements.length);
        Collections.addAll(ret, elements);
        return ret;
    }
}
