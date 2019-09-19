package com.github.unchama.util.collection


object ImmutableListFactory {

  @JvmStatic
  // Checked, no kotlin usage found
  def <E> of(): List<E> {
    return emptyList()
  }

  // Checked, no kotlin usage found
  @JvmStatic
  def <E> of(o: E): List<E> {
    return listOf(o)
  }

  @SafeVarargs
  @JvmStatic
  // Checked, no kotlin usage found
  def <E> of(vararg o: E): List<E> {
    return listOf(*o)
  }
}
