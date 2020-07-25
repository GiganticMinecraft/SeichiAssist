package com.github.unchama.seichiassist.mebius.bukkit

import com.github.unchama.seichiassist.mebius.bukkit.codec.{BukkitMebiusAppearanceMaterialCodec, BukkitMebiusItemStackCodec}
import com.github.unchama.seichiassist.mebius.domain.message.PropertyModificationMessages
import com.github.unchama.seichiassist.mebius.domain.property.MebiusProperty
import org.bukkit.ChatColor
import org.bukkit.ChatColor.RESET

object PropertyModificationBukkitMessages extends PropertyModificationMessages {
  override def onLevelUp(oldMebiusProperty: MebiusProperty, newMebiusProperty: MebiusProperty): List[String] = {
    val mebiusDisplayName = BukkitMebiusItemStackCodec.displayNameOfMaterializedItem(newMebiusProperty)

    // レベルアップ通知
    val levelUpMessage = List(s"${newMebiusProperty.mebiusName}${RESET}がレベルアップしました。")

    // 進化通知
    val materialChangeMessage =
      if (BukkitMebiusAppearanceMaterialCodec.appearanceMaterialAt(oldMebiusProperty.level) !=
        BukkitMebiusAppearanceMaterialCodec.appearanceMaterialAt(newMebiusProperty.level)) List {
        s"$mebiusDisplayName${RESET}の見た目が進化しました。"
      } else Nil

    // エンチャント効果変更通知
    val enchantmentChangeMessage =
      if (newMebiusProperty.level.isMaximum) List(
        s"$RESET${ChatColor.GREEN}おめでとうございます。$mebiusDisplayName$RESET${ChatColor.GREEN}のレベルが最大になりました。",
        s"$RESET${ChatColor.AQUA}耐久無限${RESET}が付与されました。"
      ) else List({
        val modifiedEnchantment = newMebiusProperty.enchantmentDifferentFrom(oldMebiusProperty).get

        val romanSuffix = List(
          "", "", " II", " III", " IV", " V",
          " VI", " VII", " VIII", " IX", " X",
          " XI", " XII", " XIII", " XIV", " XV",
          " XVI", " XVII", " XVIII", " XIX", " XX"
        )

        oldMebiusProperty.enchantmentLevel.get(modifiedEnchantment) match {
          case Some(previousLevel) =>
            s"${ChatColor.GRAY}${modifiedEnchantment.displayName}${romanSuffix(previousLevel)}${RESET}が" +
              s"${ChatColor.GRAY}${modifiedEnchantment.displayName}${romanSuffix(previousLevel + 1)}${RESET}に強化されました。"
          case None =>
            s"${ChatColor.GRAY}${modifiedEnchantment.displayName}${RESET}が付与されました。"
        }
      })

    levelUpMessage ++ materialChangeMessage ++ enchantmentChangeMessage
  }
}
