package com.github.unchama.util.collection;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ImmutableListFactory {
	private ImmutableListFactory() {

	}

	public static <E> List<E> of() {
		return Collections.emptyList();
	}

	public static <E> List<E> of(E o) {
		return Collections.singletonList(o);
	}

	@SafeVarargs
	public static <E> List<E> of(E... o) {
		return Arrays.asList(o);
	}
}
