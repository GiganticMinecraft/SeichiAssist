package com.github.unchama.util.collection

import java.util.Arrays
import java.util.Collections

object ImmutableListFactory {

  @JvmStatic
  fun <E> of(): List<E> {
    return emptyList()
  }

  @JvmStatic
  fun <E> of(o: E): List<E> {
    return listOf(o)
  }

  @SafeVarargs
  @JvmStatic
  fun <E> of(vararg o: E): List<E> {
    return listOf(*o)
  }
}
