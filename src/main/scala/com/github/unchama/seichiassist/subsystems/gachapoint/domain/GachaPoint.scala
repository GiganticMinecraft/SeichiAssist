package com.github.unchama.seichiassist.subsystems.gachapoint.domain

import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount

/**
 * ガチャポイントとはプレーヤーが持つ「消費可能な整地経験量」である。
 */
case class GachaPoint(exp: SeichiExpAmount) {

  import cats.implicits._

  /**
   * このガチャポイントから `point` 分を減算する。もしこのガチャポイントが `point` 未満だった場合 `None` を返す。
   */
  def use(point: GachaPoint): Option[GachaPoint] = {
    val subtracted = SeichiExpAmount.orderedMonus.subtractTruncate(exp, point.exp)

    Option.when((subtracted |+| point.exp) == exp) {
      GachaPoint(subtracted)
    }
  }

  def times(n: BigInt): GachaPoint = GachaPoint(exp.mapAmount(_ * BigDecimal(n)))

  def add(point: GachaPoint): GachaPoint = GachaPoint(exp.add(point.exp))

  def div(point: GachaPoint): BigInt = (exp.amount /% point.exp.amount)._1.toBigInt

}

object GachaPoint {

  def ofNonNegative(x: BigDecimal): GachaPoint = GachaPoint(SeichiExpAmount.ofNonNegative(x))

  final val perGachaTicket: GachaPoint = GachaPoint.ofNonNegative(1000)

}
