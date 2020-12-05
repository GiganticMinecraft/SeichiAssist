package com.github.unchama.seasonalevents.valentine

sealed trait ValentineCookieEffects extends enumeratum.EnumEntry

object ValentineCookieEffects extends enumeratum.Enum[ValentineCookieEffects] {
  override val values: IndexedSeq[ValentineCookieEffects] = findValues

  //region プラス効果

  case object Jump extends ValentineCookieEffects

  case object Speed extends ValentineCookieEffects

  case object Absorption extends ValentineCookieEffects

  case object NightVision extends ValentineCookieEffects

  case object Regeneration extends ValentineCookieEffects

  case object FireResistance extends ValentineCookieEffects

  case object WaterBreathing extends ValentineCookieEffects

  case object IncreaseDamage extends ValentineCookieEffects

  case object DamageResistance extends ValentineCookieEffects

  //endregion

  //region マイナス効果

  case object Unluck extends ValentineCookieEffects

  //endregion
}