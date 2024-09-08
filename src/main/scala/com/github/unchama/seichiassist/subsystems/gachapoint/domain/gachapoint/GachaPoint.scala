package com.github.unchama.seichiassist.subsystems.gachapoint.domain.gachapoint

import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount

/**
 * ガチャポイントとはプレーヤーが持つ「消費可能な整地経験量」である。
 *
 * プレーヤーは576個(= 64 * 9スタック)のバッチもしくは、64個(1スタック)のバッチにてガチャポイントをガチャ券に交換できる。
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
  private def useInBatch(batchSize: BatchSize): GachaPoint.Usage = {
    val ticketCount = availableTickets.min(batchSize.value).toInt

    val expToUse = GachaPoint.perGachaTicket.exp.amount * ticketCount
    val remaining = GachaPoint.ofNonNegative(exp.amount - expToUse)

    GachaPoint.Usage(remaining, ticketCount)
  }

  /**
   * ガチャポイントを576個(= 64 * 9スタック)のバッチでガチャ券に変換した際のポイントの変化を計算する。
   */
  lazy val useInLargeBatch: GachaPoint.Usage = useInBatch(GachaPoint.largeBatchSize)

  /**
   * ガチャポイントを64個(= 64 * 1スタック)のバッチでガチャ券に変換した際のポイントの変化を計算する。
   */
  lazy val useInSmallBatch: GachaPoint.Usage = useInBatch(GachaPoint.smallBatchSize)

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

case class BatchSize(value: Int) {
  require(value > 0, "batch size must be positive")
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
    require(
      gachaTicketCount <= GachaPoint.maxBatchSize.value,
      "usage must not exceed max batch size"
    )

    def asTuple: (GachaPoint, Int) = (remainingGachaPoint, gachaTicketCount)
  }

  /**
   * ガチャ券を576個(= 64 * 9スタック)のバッチでガチャポイントに変換する際のバッチサイズ
   */
  final val largeBatchSize = BatchSize(9 * 64)

  /**
   * ガチャ券を64個(= 64 * 1スタック)のバッチでガチャポイントに変換する際のバッチサイズ
   */
  final val smallBatchSize = BatchSize(64)

  /**
   * ガチャ券へのポイント交換にて一度に得られるガチャ券の上限
   */
  final val maxBatchSize = largeBatchSize

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
