package com.github.unchama.seichiassist.subsystems.gacha.domain

/**
 * ガチャを引いた結果。
 */
sealed trait GachaResult[+IS]

object GachaResult {

  case object Blank extends GachaResult[Nothing]
  case class Won[IS](template: GachaPrizeTemplate[IS]) extends GachaResult[IS]

}
