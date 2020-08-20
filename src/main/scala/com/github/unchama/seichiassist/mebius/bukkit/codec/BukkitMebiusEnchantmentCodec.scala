package com.github.unchama.seichiassist.mebius.bukkit.codec

import com.github.unchama.seichiassist.mebius.domain.property.MebiusEnchantment
import com.github.unchama.seichiassist.mebius.domain.property.MebiusEnchantment._
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

object BukkitMebiusEnchantmentCodec {

  /**
   * アイテムスタックに対して破壊的にエンチャントを付与する
   */
  def applyEnchantment(enchantment: MebiusEnchantment, level: Int)(itemStack: ItemStack): ItemStack = {
    import scala.util.chaining._

    def unsafeEnchantmentAdded(bukkitEnchantment: Enchantment): ItemStack =
      itemStack.tap {
        _.addUnsafeEnchantment(bukkitEnchantment, level)
      }

    enchantment match {
      case Protection => unsafeEnchantmentAdded(Enchantment.PROTECTION_ENVIRONMENTAL)
      case Durability => unsafeEnchantmentAdded(Enchantment.DURABILITY)
      case Mending => unsafeEnchantmentAdded(Enchantment.MENDING)
      case FireProtection => unsafeEnchantmentAdded(Enchantment.PROTECTION_FIRE)
      case ProjectileProtection => unsafeEnchantmentAdded(Enchantment.PROTECTION_PROJECTILE)
      case ExplosionProtection => unsafeEnchantmentAdded(Enchantment.PROTECTION_EXPLOSIONS)
      case Respiration => unsafeEnchantmentAdded(Enchantment.OXYGEN)
      case WaterAffinity => unsafeEnchantmentAdded(Enchantment.WATER_WORKER)
    }
  }

  def getLevelOf(enchantment: MebiusEnchantment)(itemStack: ItemStack): Int = {
    enchantment match {
      case Protection => itemStack.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL)
      case Durability => itemStack.getEnchantmentLevel(Enchantment.DURABILITY)
      case Mending => itemStack.getEnchantmentLevel(Enchantment.MENDING)
      case FireProtection => itemStack.getEnchantmentLevel(Enchantment.PROTECTION_FIRE)
      case ProjectileProtection => itemStack.getEnchantmentLevel(Enchantment.PROTECTION_PROJECTILE)
      case ExplosionProtection => itemStack.getEnchantmentLevel(Enchantment.PROTECTION_EXPLOSIONS)
      case Respiration => itemStack.getEnchantmentLevel(Enchantment.OXYGEN)
      case WaterAffinity => itemStack.getEnchantmentLevel(Enchantment.WATER_WORKER)
    }
  }

}
