package com.github.unchama.generic

import scala.collection.mutable

/**
 * 前回計算した値が内部のMapに保存されるような関数型オブジェクトのクラス。
 *
 * 計算は[[generator]]によって行われる。
 */
case class CachedFunction[A, B](generator: A => B) extends (A => B) {
  private val computedValueMap: mutable.Map[A, B] = mutable.Map.empty

  override def apply(v1: A): B = computedValueMap.getOrElseUpdate(v1, generator(v1))
}
