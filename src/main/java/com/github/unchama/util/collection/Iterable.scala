package com.github.unchama.util.collection

object CollectionUtils {
    def Iterable[IndexedValue[T]].mapValues[T, R](f: (T) => R): List[IndexedValue[R]] =
    map { (index, value) => IndexedValue(index, f(value)) }

    def Iterable[IndexedValue[T]].toMap[T](): Map[Int, T] = map { (index, value) => index to value }.toMap()
}
