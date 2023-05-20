package com.github.unchama.seichiassist.subsystems.gachapoint.domain.gachapoint

import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount

/**
 * ガチャポイントとはプレーヤーが持つ「消費可能な整地経験量」である。
 *
 * プレーヤーは576個(= 64 * 9スタック)のバッチにてガチャポイントをガチャ券に交換できる。
 */
case class GachaPoint(exp: SeichiExpAmount) {

  /**
   * このガチャポイント量をすべて消費して得られるチケット数。
   */
  lazy val availableTickets: BigInt =
    (exp.amount /% GachaPoint.perGachaTicket.exp.amount)._1.toBigInt

  /**
   * ガチャポイントをバッチでガチャ券に変換した際のポイントの変化を計算する。
   */
  lazy val useInBatch: GachaPoint.Usage = {
    val ticketCount = availableTickets.min(GachaPoint.batchSize).toInt

    val expToUse = GachaPoint.perGachaTicket.exp.amount * ticketCount
    val remaining = GachaPoint.ofNonNegative(exp.amount - expToUse)

    GachaPoint.Usage(remaining, ticketCount)
  }

  /**
   * 次にガチャ券を利用できるようになるまでに必要な整地経験値量
   */
  lazy val amountUntilNextGachaTicket: SeichiExpAmount = {
    val remainder = (exp.amount /% GachaPoint.perGachaTicket.exp.amount)._2
    val required = GachaPoint.perGachaTicket.exp.amount - remainder

    SeichiExpAmount.ofNonNegative(required)
  }

  def add(point: GachaPoint): GachaPoint = GachaPoint(exp.add(point.exp))

  def subtract(point: GachaPoint): GachaPoint = GachaPoint(exp.subtract(point.exp))
}

object GachaPoint {

  def ofNonNegative(x: BigDecimal): GachaPoint = GachaPoint(SeichiExpAmount.ofNonNegative(x))

  /**
   * ガチャポイントを使用してガチャ券へと変換した結果。
   * @param remainingGachaPoint
   *   変換後に残っているガチャポイント
   * @param gachaTicketCount
   *   変換にて得られるガチャ券の総数
   */
  case class Usage(remainingGachaPoint: GachaPoint, gachaTicketCount: Int) {
    require(gachaTicketCount <= GachaPoint.batchSize, "usage must not exceed batch size")

    def asTuple: (GachaPoint, Int) = (remainingGachaPoint, gachaTicketCount)
  }

  /**
   * ガチャ券へのポイント交換にて一度に得られるガチャ券の上限
   */
  final val batchSize = 9 * 64

  /**
   * ガチャポイントの初期値
   */
  final val initial: GachaPoint = GachaPoint.ofNonNegative(0)

  /**
   * ガチャ券1枚あたりのガチャポイント
   */
  private val perGachaTicketPoint = 1000

  /**
   * ガチャ券へのポイントの交換にて、ガチャ券一つ当たりに消費するガチャポイント量。
   */
  final val perGachaTicket: GachaPoint = GachaPoint.ofNonNegative(perGachaTicketPoint)

  /**
   * ガチャ券の枚数を指定して相当分のガチャポイントを返す
   */
  def gachaPointBy(gachaTicketAmount: Int): GachaPoint =
    ofNonNegative(perGachaTicketPoint * gachaTicketAmount)

}
