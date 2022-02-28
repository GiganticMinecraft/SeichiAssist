package com.github.unchama.seichiassist.subsystems.mebius.domain.property

/**
 * メビウスの種類。
 *
 * 現在、通常メビウスとクリスマスバージョンのメビウスのみが定義されている。
 */
sealed trait MebiusType

case object NormalMebius extends MebiusType

case object ChristmasMebius extends MebiusType
