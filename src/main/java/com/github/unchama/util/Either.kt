package com.github.unchama.util

import arrow.core.Either

fun <T> Either<T, T>.merge(): T = when (this) {
    is Either.Left -> this.a
    is Either.Right -> this.b
}
