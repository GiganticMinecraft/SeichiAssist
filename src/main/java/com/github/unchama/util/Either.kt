package com.github.unchama.util

import arrow.core.*

fun <T> Either<T, T>.merge(): T = fold({ it }, { it })
