package com.github.unchama.util.collection

inline fun <T, R> Iterable<IndexedValue<T>>.mapValues(f: (T) -> R): List<IndexedValue<R>> =
    map { (index, value) -> IndexedValue(index, f(value)) }

fun <T> Iterable<IndexedValue<T>>.toMap(): Map<Int, T> = map { (index, value) -> index to value }.toMap()
