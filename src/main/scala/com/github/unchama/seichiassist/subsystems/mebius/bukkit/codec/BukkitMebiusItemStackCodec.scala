package com.github.unchama.seichiassist.subsystems.mebius.bukkit.codec

import com.github.unchama.seichiassist.subsystems.mebius.domain.property._
import com.github.unchama.seichiassist.subsystems.mebius.domain.resources.MebiusTalks
import com.github.unchama.seichiassist.subsystems.seasonalevents.christmas.ChristmasItemData
import de.tr7zw.nbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.{ItemFlag, ItemStack}

object BukkitMebiusItemStackCodec {

  import scala.jdk.CollectionConverters._

  // noinspection DuplicatedCode
  object LoreConstants {
    val mebiusLoreHead =
      List(s"$RESET", s"$RESET${AQUA}初心者をサポートする不思議なヘルメット。", s"$RESET${AQUA}整地により成長する。", "")

    val unbreakableLoreRow = s"$RESET${AQUA}耐久無限"

    val mebiusNameStyle = s"$RESET$GOLD$BOLD"
    val christmasMebiusNameStyle = s"$RESET$WHITE$BOLD"

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
    val forcedMaterialTag = "mebiusForcedMaterial"
  }

  def encodeTypeId(mebiusType: MebiusType): Int =
    mebiusType match {
      case NormalMebius    => 1
      case ChristmasMebius => 2
    }

  def decodeTypeId(typeIdByte: Byte): Option[MebiusType] =
    typeIdByte match {
      case 1 => Some(NormalMebius)
      case 2 => Some(ChristmasMebius)
      case _ => None
    }

  def encodeForcedMaterial(forcedMaterial: MebiusForcedMaterial): Byte = forcedMaterial match {
    case MebiusForcedMaterial.None    => 0
    case MebiusForcedMaterial.Leather => 1
    case MebiusForcedMaterial.Iron    => 2
    case MebiusForcedMaterial.Chain   => 3
    case MebiusForcedMaterial.Gold    => 4
  }

  def decodeForcedMaterial(forcedMaterialByte: Byte): MebiusForcedMaterial =
    forcedMaterialByte match {
      case 0 => MebiusForcedMaterial.None
      case 1 => MebiusForcedMaterial.Leather
      case 2 => MebiusForcedMaterial.Iron
      case 3 => MebiusForcedMaterial.Chain
      case 4 => MebiusForcedMaterial.Gold
      case _ => MebiusForcedMaterial.None
    }

  def isMebius(itemStack: ItemStack): Boolean =
    itemStack != null && itemStack.getType != Material.AIR && {
      val typeIdByte = new NBTItem(itemStack).getByte(NBTTagConstants.typeIdTag)
      typeIdByte != null && decodeTypeId(typeIdByte).nonEmpty
    }

  /**
   * (必ずしも有効な`MebiusProperty`を持つとは限らない)実体から `ItemStack` をデコードする。
   */
  def decodeMebiusProperty(itemStack: ItemStack): Option[MebiusProperty] = {
    val mebius = if (isMebius(itemStack)) itemStack else return None

    val nbtItem = new NBTItem(mebius)

    import NBTTagConstants._

    val mebiusType = decodeTypeId(nbtItem.getByte(NBTTagConstants.typeIdTag)).get
    val ownerName = nbtItem.getString(ownerNameTag)
    val ownerUuid = nbtItem.getString(ownerUuidTag)
    val enchantments = MebiusEnchantmentLevels.fromUnsafeCounts(
      BukkitMebiusEnchantmentCodec.getLevelOf(_)(itemStack)
    )
    val mebiusForcedMaterial = decodeForcedMaterial(nbtItem.getByte(forcedMaterialTag))
    val mebiusLevel = MebiusLevel(nbtItem.getInteger(levelTag))
    val ownerNickname = Some(nbtItem.getString(ownerNicknameTag)).filter(_.nonEmpty)
    val mebiusName = nbtItem.getString(nameTag)

    Some(
      MebiusProperty(
        mebiusType,
        ownerName,
        ownerUuid,
        enchantments,
        mebiusForcedMaterial,
        mebiusLevel,
        ownerNickname,
        mebiusName
      )
    )
  }

  /**
   * 与えられた `MebiusProperty` を持つような実体を得る。
   *
   * `Some(property) == decodeMebiusProperty(materialize(property))`
   *
   * を満足する。
   */
  def materialize(property: MebiusProperty): ItemStack = {
    val material = property.forcedMaterial match {
      case MebiusForcedMaterial.None =>
        BukkitMebiusAppearanceMaterialCodec.appearanceMaterialAt(property.level)
      case MebiusForcedMaterial.Leather => Material.LEATHER_HELMET // 革のヘルメット
      case MebiusForcedMaterial.Iron    => Material.IRON_HELMET // 鉄のヘルメット
      case MebiusForcedMaterial.Chain   => Material.CHAINMAIL_HELMET // チェーンのヘルメット
      case MebiusForcedMaterial.Gold    => Material.GOLDEN_HELMET // 金のヘルメット
    }

    import scala.util.chaining._

    val item = new ItemStack(material, 1)

    item.setItemMeta {
      item.getItemMeta.tap { meta =>
        meta.setDisplayName(displayNameOfMaterializedItem(property))

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)

        meta.setLore {
          val talk = MebiusTalks.at(property.level)

          import LoreConstants._

          mebiusLoreHead
            .concat(
              List(
                s"$levelLoreRowPrefix${property.level.value}",
                s"$levelUpMebiusMessageLoreRowPrefix「${talk.mebiusMessage}」",
                s"$levelUpPlayerMessageLoreRowPrefix${talk.playerMessage}",
                s"",
                s"$ownerLoreRowPrefix${property.ownerPlayerId}"
              )
            )
            .concat {
              if (property.level.isMaximum) List(unbreakableLoreRow) else Nil
            }
            .concat {
              property.mebiusType match {
                case ChristmasMebius => ChristmasItemData.christmasMebiusLore
                case _               => Nil
              }
            }
            .asJava
        }
      }
    }

    property.enchantmentLevels.mapping.foreach {
      case (enchantment, level) =>
        BukkitMebiusEnchantmentCodec.applyEnchantment(enchantment, level)(item)
    }

    {
      val nbtItem = new NBTItem(item)

      import NBTTagConstants._

      nbtItem.setByte(typeIdTag, encodeTypeId(property.mebiusType).toByte)
      nbtItem.setString(ownerNameTag, property.ownerPlayerId)
      nbtItem.setString(ownerUuidTag, property.ownerUuid)
      nbtItem.setByte(forcedMaterialTag, encodeForcedMaterial(property.forcedMaterial))
      nbtItem.setInteger(levelTag, property.level.value)
      property.ownerNicknameOverride.foreach(nbtItem.setString(ownerNicknameTag, _))
      nbtItem.setString(nameTag, property.mebiusName)

      nbtItem.getItem
    }
  }

  def displayNameOfMaterializedItem(property: MebiusProperty): String = {
    import LoreConstants._

    property.mebiusType match {
      case ChristmasMebius => s"$christmasMebiusNameStyle${property.mebiusName} Christmas Ver."
      case _               => s"$mebiusNameStyle${property.mebiusName}"
    }
  }

  def ownershipMatches(player: Player)(property: MebiusProperty): Boolean =
    property.ownerUuid == player.getUniqueId.toString

  def decodePropertyOfOwnedMebius(player: Player)(
    itemStack: ItemStack
  ): Option[MebiusProperty] =
    decodeMebiusProperty(itemStack).filter(ownershipMatches(player))

}
