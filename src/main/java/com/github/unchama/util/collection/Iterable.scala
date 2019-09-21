package com.github.unchama.util.collection

import kotlin.collections.IndexedValue

object CollectionUtils {
    implicit class IndexedValuesOps[T](val receiver: Iterable[IndexedValue[T]]) {
        def mapValues[R](f: T => R): List[IndexedValue[R]] = receiver.map { case (index, value) => IndexedValue(index, f(value)) }

        def toMap(): Map[Int, T] = receiver.map { case (index, value) => index -> value }.toMap
    }
}
