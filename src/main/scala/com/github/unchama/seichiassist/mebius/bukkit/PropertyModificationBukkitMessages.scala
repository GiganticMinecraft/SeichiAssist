package com.github.unchama.seichiassist.mebius.bukkit

import com.github.unchama.seichiassist.mebius.bukkit.codec.{BukkitMebiusAppearanceMaterialCodec, BukkitMebiusItemStackCodec}
import com.github.unchama.seichiassist.mebius.domain.message.PropertyModificationMessages
import com.github.unchama.seichiassist.mebius.domain.property.{MebiusEnchantment, MebiusProperty}
import org.bukkit.ChatColor._

object PropertyModificationBukkitMessages extends PropertyModificationMessages {
  override def onLevelUp(oldMebiusProperty: MebiusProperty, newMebiusProperty: MebiusProperty): List[String] = {
    val mebiusDisplayName = BukkitMebiusItemStackCodec.displayNameOfMaterializedItem(newMebiusProperty)

    // レベルアップ通知
    val levelUpMessage =
      if (newMebiusProperty.level.isMaximum) {
        List(s"$RESET${GREEN}おめでとうございます。$mebiusDisplayName$RESET${GREEN}のレベルが最大になりました。")
      } else {
        List(s"${newMebiusProperty.mebiusName}${RESET}がレベルアップしました。")
      }

    // 進化通知
    val materialChangeMessage =
      if (BukkitMebiusAppearanceMaterialCodec.appearanceMaterialAt(oldMebiusProperty.level) !=
        BukkitMebiusAppearanceMaterialCodec.appearanceMaterialAt(newMebiusProperty.level)) List {
        s"$mebiusDisplayName${RESET}の見た目が進化しました。"
      } else Nil

    // エンチャント効果変更通知
    val givenEnchantment = newMebiusProperty.enchantmentLevels
      .differenceFrom(oldMebiusProperty.enchantmentLevels)
      .head

    val enchantmentChangeMessage =
      if (givenEnchantment == MebiusEnchantment.Unbreakable) {
        List(s"$RESET${AQUA}耐久無限${RESET}が付与されました。")
      } else List({
        val romanSuffix = List(
          "", "", " II", " III", " IV", " V",
          " VI", " VII", " VIII", " IX", " X",
          " XI", " XII", " XIII", " XIV", " XV",
          " XVI", " XVII", " XVIII", " XIX", " XX"
        )

        oldMebiusProperty.enchantmentLevels.of(givenEnchantment) match {
          case 1 =>
            s"$GRAY${givenEnchantment.displayName}${RESET}が付与されました。"
          case previousLevel =>
            s"$GRAY${givenEnchantment.displayName}${romanSuffix(previousLevel)}${RESET}が" +
              s"$GRAY${givenEnchantment.displayName}${romanSuffix(previousLevel + 1)}${RESET}に強化されました。"
        }
      })

    levelUpMessage ++ materialChangeMessage ++ enchantmentChangeMessage
  }
}
