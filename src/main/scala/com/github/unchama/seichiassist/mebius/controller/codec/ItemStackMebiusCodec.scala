package com.github.unchama.seichiassist.mebius.controller.codec

import com.github.unchama.seichiassist.mebius.domain.resources.{MebiusEnchantments, MebiusMessages}
import com.github.unchama.seichiassist.mebius.domain.{MebiusEnchantment, MebiusLevel, MebiusProperty}
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor
import org.bukkit.ChatColor.{RED, RESET}
import org.bukkit.inventory.{ItemFlag, ItemStack}

object ItemStackMebiusCodec extends MebiusCodec[ItemStack] {

  import scala.jdk.CollectionConverters._

  private val mebiusLoreHead = List(
    s"$RESET",
    s"$RESET${ChatColor.AQUA}初心者をサポートする不思議なヘルメット。",
    s"$RESET${ChatColor.AQUA}整地により成長する。",
    ""
  )

  private val mebiusNameDisplayPrefix = s"$RESET${ChatColor.GOLD}${ChatColor.BOLD}"

  private val ownerLoreRowPrefix = s"$RESET${ChatColor.DARK_GREEN}所有者："
  private val levelLoreRowPrefix = s"$RESET$RED${ChatColor.BOLD}アイテムLv. "

  private val levelUpMebiusMessageLoreRowPrefix = s"$RESET${ChatColor.GOLD}${ChatColor.ITALIC}"
  private val levelUpPlayerMessageLoreRowPrefix = s"$RESET${ChatColor.GRAY}${ChatColor.ITALIC}"

  override def decodeMebiusProperty(itemStack: ItemStack): Option[MebiusProperty] = {
    val isMebius: Boolean = {
      val meta = itemStack.getItemMeta

      meta.hasLore && {
        val lore = meta.getLore.asScala
        mebiusLoreHead.forall(lore.contains)
      }
    }

    val mebius =
      if (!isMebius) {
        return None
      } else {
        itemStack
      }

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

  override def materialize(property: MebiusProperty): ItemStack = {
    val material = AppearanceMaterialCodec.appearanceMaterialAt(property.level)

    import scala.util.chaining._

    val item = new ItemStack(material)

    item.setItemMeta {
      item.getItemMeta.tap { meta =>
        meta.setDisplayName(s"$mebiusNameDisplayPrefix${property.mebiusName}")

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)

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
            .asJava
        }
      }
    }

    property.enchantments.foreach { case (enchantment, level) =>
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
