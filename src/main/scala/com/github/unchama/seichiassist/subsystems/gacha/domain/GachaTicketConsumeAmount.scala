package com.github.unchama.seichiassist.subsystems.gacha.domain

/**
 * ガチャ券を消費する量を定義する。
 * @param value 消費数
 */
abstract class GachaTicketConsumeAmount(val value: Int)

object GachaTicketConsumeAmount {

  case object oneThousand extends GachaTicketConsumeAmount(1000)

  case object fiveThousands extends GachaTicketConsumeAmount(5000)

  case object tenThousands extends GachaTicketConsumeAmount(10000)

}
