package com.github.unchama.generic.ratelimiting

import cats.Functor
import cats.effect.concurrent.Ref

/**
 * Rate Limitの実装を提供するオブジェクト。
 *
 * TODO 数値型について多相的にする。Spireを導入すると良い。
 */
trait RateLimiter[F[_]] {

  /**
   * 単一のリクエストを送る申請をする作用。
   * 作用の結果として、リクエストを送って良いかを表す[[Boolean]]が返る。
   */
  def requestPermission(implicit F: Functor[F]): F[Boolean] = F.map(requestPermissionN(1))(_ == 1)

  /**
   * `n` 個のリクエストを送る申請をする作用。
   * 作用の結果として、送って良いリクエストの数を表す0以上 `n` 以下の整数が返される。
   *
   * @param n 申請するリクエスト送信数。非負整数であることが要求される。
   */
  def requestPermissionN(n: Int): F[Int]

}

object RateLimiter {

  def fromPermitRef[F[_]](permitRef: Ref[F, Int]): RateLimiter[F] =
    (n: Int) =>
      permitRef.modify { permits =>
        val newPermitCount = (permits - n) max 0
        (newPermitCount, permits - newPermitCount)
      }

}
