package com.github.unchama.seichiassist.mebius.controller.codec

import com.github.unchama.seichiassist.mebius.domain.resources.{MebiusEnchantments, MebiusMessages}
import com.github.unchama.seichiassist.mebius.domain.{MebiusEnchantment, MebiusLevel, MebiusProperty}
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor
import org.bukkit.ChatColor.{RED, RESET}
import org.bukkit.inventory.{ItemFlag, ItemStack}

object ItemStackMebiusCodec {

  import scala.jdk.CollectionConverters._

  private val mebiusLoreHead = List(
    s"$RESET",
    s"$RESET${ChatColor.AQUA}初心者をサポートする不思議なヘルメット。",
    s"$RESET${ChatColor.AQUA}整地により成長する。",
    ""
  )

  private val unbreakableLoreRow = s"$RESET${ChatColor.AQUA}耐久無限"

  val mebiusNameDisplayPrefix = s"$RESET${ChatColor.GOLD}${ChatColor.BOLD}"

  private val ownerLoreRowPrefix = s"$RESET${ChatColor.DARK_GREEN}所有者："
  private val levelLoreRowPrefix = s"$RESET$RED${ChatColor.BOLD}アイテムLv. "

  private val levelUpMebiusMessageLoreRowPrefix = s"$RESET${ChatColor.GOLD}${ChatColor.ITALIC}"
  private val levelUpPlayerMessageLoreRowPrefix = s"$RESET${ChatColor.GRAY}${ChatColor.ITALIC}"

  def isMebius(itemStack: ItemStack): Boolean = {
    val meta = if (itemStack != null) itemStack.getItemMeta else return false

    meta.hasLore && {
      val lore = meta.getLore.asScala
      mebiusLoreHead.forall(lore.contains)
    }
  }

  /**
   * (必ずしも有効な`MebiusProperty`を持つとは限らない)実体から `ItemStack` をデコードする。
   */
  def decodeMebiusProperty(itemStack: ItemStack): Option[MebiusProperty] = {
    val mebius = if (isMebius(itemStack)) itemStack else return None

    val nickname = {
      val nbtItem = new NBTItem(mebius)
      val nicknameField = nbtItem.getString("nickname")

      if (nicknameField.isEmpty) None else Some(nicknameField)
    }

    val mebiusLevel = MebiusLevel {
      mebius.getItemMeta.getLore.get(4).replace(levelLoreRowPrefix, "").toInt
    }

    val ownerName = {
      mebius.getItemMeta.getLore.get(8).replaceFirst(ownerLoreRowPrefix, "")
    }

    val enchantments = {
      MebiusEnchantments.list
        .map { case mebiusEnchantment@MebiusEnchantment(enchantment, _, _, _) =>
          mebiusEnchantment -> mebius.getEnchantmentLevel(enchantment)
        }
        .filter { case (e, l) => 1 <= l && l <= e.maxLevel }
        .toMap
    }

    val mebiusName = mebius.getItemMeta.getDisplayName

    Some(MebiusProperty(ownerName, enchantments, mebiusLevel, nickname, mebiusName))
  }

  /**
   * 与えられた `MebiusProperty` を持つような実体を得る。
   *
   * `Some(property) == decodeMebiusProperty(materialize(property))`
   *
   * を満足する。
   */
  def materialize(property: MebiusProperty, damageValue: Short = 0.toShort): ItemStack = {
    val material = AppearanceMaterialCodec.appearanceMaterialAt(property.level)

    import scala.util.chaining._

    val item = new ItemStack(material, 1, damageValue)

    item.setItemMeta {
      item.getItemMeta.tap { meta =>
        meta.setDisplayName(s"$mebiusNameDisplayPrefix${property.mebiusName}")

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)

        if (property.level.isMaximum) meta.setUnbreakable(true)

        meta.setLore {
          val previousLevelUpTalk = MebiusMessages.talkOnLevelUp(property.level.value)

          mebiusLoreHead
            .concat(List(
              s"$levelLoreRowPrefix${property.level.value}",
              s"$levelUpMebiusMessageLoreRowPrefix${previousLevelUpTalk.mebiusMessage}",
              s"$levelUpPlayerMessageLoreRowPrefix${previousLevelUpTalk.playerMessage}",
              s"",
              s"$ownerLoreRowPrefix${property.ownerName}"
            ))
            .concat {
              if (property.level.isMaximum) List(unbreakableLoreRow) else Nil
            }
            .asJava
        }
      }
    }

    property.enchantmentLevel.foreach { case (enchantment, level) =>
      item.addUnsafeEnchantment(enchantment.enchantment, level)
    }

    {
      val nbtItem = new NBTItem(item)

      property.ownerNickname match {
        case Some(ownerNickname) => nbtItem.setString("nickname", ownerNickname)
        case None =>
      }

      nbtItem.getItem
    }
  }

}
