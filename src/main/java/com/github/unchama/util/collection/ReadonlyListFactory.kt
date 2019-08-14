package com.github.unchama.util.collection

object ReadonlyListFactory {

  fun <E> of(): List<E> {
    return emptyList()
  }

  fun <E> of(o: E): List<E> {
    return listOf(o)
  }

  @SafeVarargs
  fun <E> of(vararg o: E): List<E> {
    return listOf(*o)
  }
}
