package com.github.unchama.util.collection


object ImmutableListFactory {

  @JvmStatic
  // Checked, no kotlin usage found
  fun <E> of(): List<E> {
    return emptyList()
  }

  // Checked, no kotlin usage found
  @JvmStatic
  fun <E> of(o: E): List<E> {
    return listOf(o)
  }

  @SafeVarargs
  @JvmStatic
  // Checked, no kotlin usage found
  fun <E> of(vararg o: E): List<E> {
    return listOf(*o)
  }
}
