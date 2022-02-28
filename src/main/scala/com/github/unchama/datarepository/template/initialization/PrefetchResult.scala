package com.github.unchama.datarepository.template.initialization

import cats.{Applicative, Eval, Traverse}

/**
 * データリポジトリの中間データの生成結果。
 *   - [[R]] の値が入っている
 *   - 読み込みが失敗して(プレーヤーに表示するための)メッセージが入っている
 *
 * のどちらかである。
 */
sealed trait PrefetchResult[+R]

object PrefetchResult {

  case class Failed(kickMessage: Option[String]) extends PrefetchResult[Nothing]

  case class Success[+R](data: R) extends PrefetchResult[R]

  implicit val traverseInstance: Traverse[PrefetchResult] = new Traverse[PrefetchResult] {
    override def traverse[G[_], A, B](
      fa: PrefetchResult[A]
    )(f: A => G[B])(implicit G: Applicative[G]): G[PrefetchResult[B]] =
      fa match {
        case f @ Failed(_) => G.pure(f)
        case Success(data) => G.map(f(data))(PrefetchResult.Success.apply)
      }

    override def foldLeft[A, B](fa: PrefetchResult[A], b: B)(f: (B, A) => B): B =
      fa match {
        case Failed(_)     => b
        case Success(data) => f(b, data)
      }

    override def foldRight[A, B](fa: PrefetchResult[A], lb: Eval[B])(
      f: (A, Eval[B]) => Eval[B]
    ): Eval[B] =
      fa match {
        case Failed(_)     => lb
        case Success(data) => f(data, lb)
      }
  }
}
