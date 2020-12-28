package com.github.unchama.generic.ratelimiting

import cats.effect.concurrent.Ref
import cats.kernel.Group
import com.github.unchama.generic.algebra.typeclasses.TotallyOrderedGroup

/**
 * Rate Limitの実装を提供するオブジェクト。
 * 内部状態を持ち、一定時間当たりにいくらかの量の「リクエスト」が送信されることを許可する。
 *
 * 例えばデータ転送等のコンテキストにおいて、
 * 時間当たり一定量のバイト数だけ送信を許可したいという要求がある。
 * ここでの転送するデータに相当する概念を「リクエスト」と呼ぶことにする。
 *
 * このオブジェクトの使用者は、一定の量のリクエストの送信を申請する。
 * このオブジェクトはリクエスト量の制限に基づいて、
 * あとどれくらいリクエストを送って良いかという指標を返す。
 * リクエスト量にどのような制限を掛けるかは実装依存である。
 *
 * @tparam A リクエスト量を記述できる型
 */
trait RateLimiter[F[_], A] {

  /**
   * Aは全順序群であることが要求される
   */
  protected val A: TotallyOrderedGroup[A]

  /**
   * [[A]] の値によって指定されるリクエスト量を送る申請をする作用。
   * 作用の結果として、送って良いリクエスト量を表す [[A]] の(全順序群での意味で)非負の値が返される。
   *
   * @param a 申請するリクエスト送信量
   * @return 非負の[[A]]の値を返す作用
   */
  def requestPermission(a: A): F[A]

}

object RateLimiter {

  import cats.implicits._

  def fromPermitRef[F[_], A: TotallyOrderedGroup](permitRef: Ref[F, A]): RateLimiter[F, A] =
    new RateLimiter[F, A] {
      override protected val A: TotallyOrderedGroup[A] = implicitly

      override def requestPermission(a: A): F[A] =
        permitRef.modify { permits =>
          val zero = Group[A].empty
          val newPermitCount = (permits |+| a.inverse()) max zero
          (newPermitCount, (permits |+| newPermitCount.inverse()) max zero)
        }
    }

}
