package com.github.unchama.generic

import eu.timepit.refined.api.Refined

/**
 * `From`型の値を`To`型の値に「強制」する方法を提供する。
 */
sealed trait CoerceTo[-From, +To] {

  /**
   * 値を「強制」する。強制は、参照透過である必要があり、いかなる例外も投げてはならない。
   * @param from 強制する前の値
   * @return 強制されたあとの値
   */
  def coerceTo(from: From): To

  final def asFunction: From => To = coerceTo
}

object CoerceTo {
  implicit def identity[T]: CoerceTo[T, T] = new CoerceTo[T, T] {

    /**
     * @inheritdoc
     */
    override def coerceTo(from: T): T = from
  }

  implicit def forgetRefinedPredicate[T, Predicate]: CoerceTo[T Refined Predicate, T] =
    new CoerceTo[T Refined Predicate, T] {

      /**
       * @inheritdoc
       */
      override def coerceTo(from: Refined[T, Predicate]): T = from match {
        case Refined(t) => t
      }
    }
}
