package com.github.unchama.seichiassist.mebius.bukkit.codec

import com.github.unchama.seichiassist.mebius.domain.property
import com.github.unchama.seichiassist.mebius.domain.property.{MebiusEnchantment, MebiusLevel, MebiusProperty}
import com.github.unchama.seichiassist.mebius.domain.resources.MebiusTalks
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.{ItemFlag, ItemStack}

object BukkitMebiusItemStackCodec {

  import scala.jdk.CollectionConverters._

  //noinspection DuplicatedCode
  object LoreConstants {
    val mebiusLoreHead = List(
      s"$RESET",
      s"$RESET${AQUA}初心者をサポートする不思議なヘルメット。",
      s"$RESET${AQUA}整地により成長する。",
      ""
    )

    val unbreakableLoreRow = s"$RESET${AQUA}耐久無限"

    val mebiusNameDisplayPrefix = s"$RESET$GOLD$BOLD"

    val ownerLoreRowPrefix = s"$RESET${DARK_GREEN}所有者："
    val levelLoreRowPrefix = s"$RESET$RED${BOLD}アイテムLv. "

    val levelUpMebiusMessageLoreRowPrefix = s"$RESET$GOLD$ITALIC"
    val levelUpPlayerMessageLoreRowPrefix = s"$RESET$GRAY$ITALIC"
  }

  object NBTTagConstants {
    val typeIdTag = "mebiusTypeId"
    val ownerNameTag = "mebiusOwnerName"
    val ownerUuidTag = "mebiusOwnerUUID"
    val levelTag = "mebiusLevel"
    val ownerNicknameTag = "mebiusOwnerNickname"
    val nameTag = "mebiusName"
  }

  def isMebius(itemStack: ItemStack): Boolean = {
    if (itemStack != null && itemStack.getType != Material.AIR) {
      new NBTItem(itemStack).getByte(NBTTagConstants.typeIdTag) != 0
    } else {
      false
    }
  }

  /**
   * (必ずしも有効な`MebiusProperty`を持つとは限らない)実体から `ItemStack` をデコードする。
   */
  def decodeMebiusProperty(itemStack: ItemStack): Option[MebiusProperty] = {
    val mebius = if (isMebius(itemStack)) itemStack else return None

    val nbtItem = new NBTItem(mebius)

    import NBTTagConstants._

    val ownerName = nbtItem.getString(ownerNameTag)
    val ownerUuid = nbtItem.getString(ownerUuidTag)
    val enchantments = {
      MebiusEnchantment.values
        .map { mebiusEnchantment =>
          mebiusEnchantment -> BukkitMebiusEnchantmentCodec.getLevelOf(mebiusEnchantment)(itemStack)
        }
        .filter { case (e, l) => 1 <= l && l <= e.maxLevel }
        .toMap
    }
    val mebiusLevel = MebiusLevel(nbtItem.getInteger(levelTag))
    val ownerNickname = Some(nbtItem.getString(ownerNicknameTag)).filter(_.nonEmpty)
    val mebiusName = nbtItem.getString(nameTag)

    Some(property.MebiusProperty(ownerName, ownerUuid, enchantments, mebiusLevel, ownerNickname, mebiusName))
  }

  /**
   * 与えられた `MebiusProperty` を持つような実体を得る。
   *
   * `Some(property) == decodeMebiusProperty(materialize(property))`
   *
   * を満足する。
   */
  def materialize(property: MebiusProperty, damageValue: Short): ItemStack = {
    val material = BukkitMebiusAppearanceMaterialCodec.appearanceMaterialAt(property.level)

    import scala.util.chaining._

    val item = new ItemStack(material, 1, damageValue)

    item.setItemMeta {
      item.getItemMeta.tap { meta =>
        meta.setDisplayName(displayNameOfMaterializedItem(property))

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)

        if (property.level.isMaximum) meta.setUnbreakable(true)

        meta.setLore {
          val talk = MebiusTalks.at(property.level)

          import LoreConstants._

          mebiusLoreHead
            .concat(List(
              s"$levelLoreRowPrefix${property.level.value}",
              s"$levelUpMebiusMessageLoreRowPrefix${talk.mebiusMessage}",
              s"$levelUpPlayerMessageLoreRowPrefix${talk.playerMessage}",
              s"",
              s"$ownerLoreRowPrefix${property.ownerPlayerId}"
            ))
            .concat {
              if (property.level.isMaximum) List(unbreakableLoreRow) else Nil
            }
            .asJava
        }
      }
    }

    property.enchantmentLevel.foreach { case (enchantment, level) =>
      BukkitMebiusEnchantmentCodec.applyEnchantment(enchantment, level)(item)
    }

    {
      val nbtItem = new NBTItem(item)

      import NBTTagConstants._

      nbtItem.setByte(typeIdTag, 1.toByte)
      nbtItem.setString(ownerNameTag, property.ownerPlayerId)
      nbtItem.setString(ownerUuidTag, property.ownerUuid)
      nbtItem.setInteger(levelTag, property.level.value)
      property.ownerNicknameOverride.foreach(nbtItem.setString(ownerNicknameTag, _))
      nbtItem.setString(nameTag, property.mebiusName)

      nbtItem.getItem
    }
  }

  def displayNameOfMaterializedItem(property: MebiusProperty): String = {
    import LoreConstants._

    mebiusNameDisplayPrefix + property.mebiusName
  }

  def ownershipMatches(player: Player)(property: MebiusProperty): Boolean =
    property.ownerUuid == player.getUniqueId.toString

  def decodePropertyOfOwnedMebius(player: Player)(itemStack: ItemStack): Option[MebiusProperty] =
    decodeMebiusProperty(itemStack).filter(ownershipMatches(player))

}
