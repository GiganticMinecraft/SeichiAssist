package com.github.unchama.generic

import cats.effect.Sync
import cats.{Functor, ~>}

import scala.ref.WeakReference

/**
 * [[A]] への、アクセスが [[F]] の文脈で行われる弱参照を表す。
 */
sealed trait WeakRef[F[_], A <: AnyRef] {

  /**
   * 参照しているオブジェクトを取る。
   *
   * `F[_]: Monad` の時、任意のアクション `f: F[U]` について、次が成り立つ：
   *   - {{{ get.flatMap(r1 => f.flatMap(_ => get.flatMap(r2 => // 空になった場合、その後はずっと空 !(r1.isEmpty
   *     && r2.nonempty) ))) == F.pure(true) }}}
   *   - {{{ get.flatMap(r1 => f.flatMap(_ => get.flatMap(r2 => // 二度 Some を返してきた場合、参照が等しい
   *     (r2.isEmpty) || (r1.get) eq (r2.get) ))) == F.pure(true) }}}
   */
  def get: F[Option[A]]

  def map[B <: AnyRef](f: A => B)(implicit F: Functor[F]): WeakRef[F, B] =
    new WeakRef[F, B] {
      override def get: F[Option[B]] = F.map(WeakRef.this.get)(_.map(f))
    }

  def mapK[G[_]](f: F ~> G): WeakRef[G, A] =
    new WeakRef[G, A] {
      override def get: G[Option[A]] = f(WeakRef.this.get)
    }

}

object WeakRef {

  /**
   * 与えられたオブジェクト `value` を弱参照に包む。 `value` がガベージコレクトされた場合、返却された [[WeakRef]] の `get` 作用は [[None]]
   * を返すようになる。
   */
  def of[F[_]: Sync, A <: AnyRef](value: A): WeakRef[F, A] = {
    val weakReference = new WeakReference(value)
    val getAction = Sync[F].delay(weakReference.get)

    new WeakRef[F, A] {
      val get: F[Option[A]] = getAction
    }
  }

}
