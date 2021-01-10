package com.github.unchama.generic.ratelimiting

import cats.effect.concurrent.Ref
import com.github.unchama.generic.algebra.typeclasses.OrderedMonus

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
   * Aは減算演算があるモノイドであることが要求される
   */
  protected val A: OrderedMonus[A]

  /**
   * [[A]] の値によって指定されるリクエスト量を送る申請をする作用。
   * 作用の結果として、送って良いリクエスト量を表す [[A]] の値が返される。
   *
   * @param a 申請するリクエスト送信量
   * @return [[A]]の値を返す作用
   */
  def requestPermission(a: A): F[A]

}

object RateLimiter {

  import OrderedMonus._
  import cats.implicits._

  /**
   * 送信したリクエスト数を保持する参照セルの情報を見る [[RateLimiter]] を作成する。
   */
  def fromCountRef[F[_], A: OrderedMonus](countRef: Ref[F, A])(maxCount: A): RateLimiter[F, A] =
    new RateLimiter[F, A] {
      override protected val A: OrderedMonus[A] = implicitly

      override def requestPermission(a: A): F[A] =
        countRef.modify { count =>
          val newCount = (count |+| a) min maxCount
          (newCount, newCount |-| count)
        }
    }

}
