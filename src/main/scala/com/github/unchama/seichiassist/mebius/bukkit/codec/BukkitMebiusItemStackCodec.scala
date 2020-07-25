package com.github.unchama.seichiassist.mebius.bukkit.codec

import com.github.unchama.seichiassist.mebius.domain.property
import com.github.unchama.seichiassist.mebius.domain.property.{MebiusEnchantment, MebiusLevel, MebiusProperty}
import com.github.unchama.seichiassist.mebius.domain.resources.{MebiusEnchantments, MebiusTalks}
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.{ItemFlag, ItemStack}

object BukkitMebiusItemStackCodec {

  import scala.jdk.CollectionConverters._

  private val mebiusLoreHead = List(
    s"$RESET",
    s"$RESET${AQUA}初心者をサポートする不思議なヘルメット。",
    s"$RESET${AQUA}整地により成長する。",
    ""
  )

  private val unbreakableLoreRow = s"$RESET${AQUA}耐久無限"

  private val mebiusNameDisplayPrefix = s"$RESET$GOLD$BOLD"

  private val ownerLoreRowPrefix = s"$RESET${DARK_GREEN}所有者："
  private val levelLoreRowPrefix = s"$RESET$RED${BOLD}アイテムLv. "

  private val levelUpMebiusMessageLoreRowPrefix = s"$RESET$GOLD$ITALIC"
  private val levelUpPlayerMessageLoreRowPrefix = s"$RESET$GRAY$ITALIC"

  def isMebius(itemStack: ItemStack): Boolean = new NBTItem(itemStack).getByte("mebiusTypeId") != 0

  /**
   * (必ずしも有効な`MebiusProperty`を持つとは限らない)実体から `ItemStack` をデコードする。
   */
  def decodeMebiusProperty(itemStack: ItemStack): Option[MebiusProperty] = {
    val mebius = if (isMebius(itemStack)) itemStack else return None

    val nbtItem = new NBTItem(mebius)

    val ownerName = nbtItem.getString("mebiusOwnerName")
    val ownerUuid = nbtItem.getString("mebiusOwnerUUID")
    val enchantments = {
      MebiusEnchantments.list
        .map { case mebiusEnchantment@MebiusEnchantment(enchantment, _, _, _) =>
          mebiusEnchantment -> mebius.getEnchantmentLevel(enchantment)
        }
        .filter { case (e, l) => 1 <= l && l <= e.maxLevel }
        .toMap
    }
    val mebiusLevel = MebiusLevel(nbtItem.getInteger("mebiusLevel"))
    val ownerNickname = Some(nbtItem.getString("mebiusOwnerNickname")).filter(_.nonEmpty)
    val mebiusName = nbtItem.getString("mebiusName")

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
      item.addUnsafeEnchantment(enchantment.enchantment, level)
    }

    {
      val nbtItem = new NBTItem(item)

      nbtItem.setByte("mebiusTypeId", 1.toByte)
      nbtItem.setString("mebiusOwnerName", property.ownerPlayerId)
      nbtItem.setString("mebiusOwnerUUID", property.ownerUuid)
      nbtItem.setInteger("mebiusLevel", property.level.value)
      property.ownerNicknameOverride.foreach(nbtItem.setString("mebiusOwnerNickname", _))
      nbtItem.setString("mebiusName", property.mebiusName)

      nbtItem.getItem
    }
  }

  def displayNameOfMaterializedItem(property: MebiusProperty): String =
    mebiusNameDisplayPrefix + property.mebiusName

  def ownershipMatches(player: Player)(property: MebiusProperty): Boolean =
    property.ownerUuid == player.getUniqueId.toString

  def decodePropertyOfOwnedMebius(player: Player)(itemStack: ItemStack): Option[MebiusProperty] =
    decodeMebiusProperty(itemStack).filter(ownershipMatches(player))

}
