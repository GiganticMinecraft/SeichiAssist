package com.github.unchama.seasonalevents.valentine

import java.util.Random

import com.github.unchama.seasonalevents.valentine.ValentineCookieEffects._
import org.bukkit.potion.{PotionEffect, PotionEffectType}

object ValentineCookieEffectsHandler {
  def randomlySelectEffect: ValentineCookieEffects =
    ValentineCookieEffects.values(new Random().nextInt(ValentineCookieEffects.values.size))

  def getEffect(effect: ValentineCookieEffects): (String, PotionEffect) = effect match {
    case Jump => ("跳躍力上昇", new PotionEffect(PotionEffectType.JUMP, 20 * 60 * 10, 1))
    case Speed => ("移動速度上昇", new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 10, 1))
    case Absorption => ("衝撃吸収", new PotionEffect(PotionEffectType.ABSORPTION, 20 * 60 * 10, 1))
    case NightVision => ("暗視", new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 60 * 10, 1))
    case Regeneration => ("再生能力", new PotionEffect(PotionEffectType.REGENERATION, 20 * 60 * 10, 1))
    case FireResistance => ("火炎耐性", new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 60 * 10, 1))
    case WaterBreathing => ("水中呼吸", new PotionEffect(PotionEffectType.WATER_BREATHING, 20 * 60 * 10, 1))
    case IncreaseDamage => ("攻撃力上昇", new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 60 * 10, 1))
    case DamageResistance => ("耐性", new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 60 * 10, 1))

    case Unluck => ("不運", new PotionEffect(PotionEffectType.UNLUCK, 20 * 60, 1))
  }

  def getMessage(effect: ValentineCookieEffects): String = effect match {
    case Unluck => "不運IIを感じてしまった…はぁ…むなしいなぁ…"
    case _ => s"${getEffect(effect)._1}IIを奪い取った！あぁ、おいしいなぁ！"
  }
}