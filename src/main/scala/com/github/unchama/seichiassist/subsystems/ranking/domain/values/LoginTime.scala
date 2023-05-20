package com.github.unchama.seichiassist.subsystems.ranking.domain.values

import cats.kernel.{Monoid, Order}

/**
 * TODO: rankingサブシステムから移す
 */
case class LoginTime(inTick: Long) {
  val formatted = s"${inTick / 3600 / 20}時間${inTick / 60 / 20 % 60}分${inTick / 20 % 60}秒"
}

object LoginTime {
  implicit val isMonoid: Monoid[LoginTime] =
    Monoid.instance(LoginTime(0), (a, b) => LoginTime(a.inTick + b.inTick))

  implicit val isOrdered: Order[LoginTime] = Order.by(_.inTick)
}
