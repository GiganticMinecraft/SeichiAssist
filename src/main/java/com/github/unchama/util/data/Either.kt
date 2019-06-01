package com.github.unchama.util.data

import arrow.core.Either

/**
 * [Either.Left]も[Either.Right]も単一の[T]を含んでいるとき, その値を取り出す.
 *
 * @return この[Either]に入っている値
 */
fun <T> Either<T, T>.merge(): T = this.fold({ it }, { it })
