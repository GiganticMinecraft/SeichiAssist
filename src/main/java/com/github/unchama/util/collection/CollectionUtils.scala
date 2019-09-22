package com.github.unchama.util.collection

import kotlin.collections.IndexedValue

@Deprecated
object CollectionUtils {
  implicit class IndexedValuesOps[T](val receiver: Iterable[IndexedValue[T]]) {
    def mapValues[R](f: T => R): Iterable[IndexedValue[R]] = receiver.map {
      v => new IndexedValue(v.getIndex, f(v.getValue))
    }

    def toMap: Map[Int, T] = receiver.map { v => v.getIndex -> v.getValue }.toMap
  }
}
