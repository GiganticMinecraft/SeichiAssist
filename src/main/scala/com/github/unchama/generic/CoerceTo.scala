package com.github.unchama.generic

import eu.timepit.refined.api.Refined

/**
 * 値の情報を削除してプリミティブな値を生成する。
 * このトレイトのインスタンスは[[CoerceTo$]]で与えられる実装を除き、
 * `case class`などからその型が持つフィールドへの射影として実装されるべきではない。
 */
trait CoerceTo[-From, +To] {
  /**
   * 値の情報を削除する。削除は副作用を起こしてはならず、いかなる例外も起こしてはならず、べき等、かつ一貫した値を生成する必要がある。
   * @param from
   * @return
   */
  def coerceTo(from: From): To

  final def asFunction: From => To = coerceTo
}

object CoerceTo {
  implicit def identity[T]: CoerceTo[T, T] = t => t

  implicit def forgetRefinedPredicate[T, Predicate]: CoerceTo[T Refined Predicate, T] = refined => refined.value
}
