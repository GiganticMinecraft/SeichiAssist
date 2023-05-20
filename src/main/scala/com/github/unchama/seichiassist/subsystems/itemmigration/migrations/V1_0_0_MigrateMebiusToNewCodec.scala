package com.github.unchama.seichiassist.subsystems.itemmigration.migrations

import cats.effect.SyncIO
import com.github.unchama.itemmigration.bukkit.util.MigrationHelper
import com.github.unchama.itemmigration.domain.{ItemMigration, ItemMigrationVersionNumber}
import com.github.unchama.seichiassist.subsystems.itemmigration.domain.minecraft.UuidRepository
import com.github.unchama.seichiassist.subsystems.itemmigration.migrations.V1_0_0_MigrateMebiusToNewCodec.OldBukkitMebiusItemStackCodec.OldMebiusRawProperty
import com.github.unchama.seichiassist.subsystems.mebius.bukkit.codec.BukkitMebiusItemStackCodec.NBTTagConstants
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.slf4j.Logger

/**
 * メビウスを古い、Loreによって情報を管理するフォーマットからNBTによって情報を管理するフォーマットに切り替える変換。
 */
object V1_0_0_MigrateMebiusToNewCodec {

  import scala.jdk.CollectionConverters._

  val ownerResolutionError = s"$RESET${DARK_RED}エラー：所有者が見つかりません。"

  // noinspection DuplicatedCode
  object OldBukkitMebiusItemStackCodec {

    import de.tr7zw.itemnbtapi.NBTItem
    import org.bukkit.inventory.ItemStack

    private val mebiusLoreHead =
      List(s"$RESET", s"$RESET${AQUA}初心者をサポートする不思議なヘルメット。", s"$RESET${AQUA}整地により成長する。", "")
    private val ownerLoreRowPrefix = s"$RESET${DARK_GREEN}所有者："
    private val levelLoreRowPrefix = s"$RESET$RED${BOLD}アイテムLv. "
    def isNewMebius(itemStack: ItemStack): Boolean = {
      itemStack != null && itemStack.getType != Material.AIR && {
        new NBTItem(itemStack).getByte(NBTTagConstants.typeIdTag) == 1
      }
    }

    def isMebius(itemStack: ItemStack): Boolean = {
      val meta = if (itemStack != null) itemStack.getItemMeta else return false

      meta != null && meta.hasLore && {
        val lore = meta.getLore.asScala
        mebiusLoreHead.forall(lore.contains)
      }
    }

    /**
     * `"mebiusTypeId"`をNBTに持たないアイテムから古いMebiusのプロパティをデコードする。
     */
    def decodeOldMebiusProperty(itemStack: ItemStack): Option[OldMebiusRawProperty] = {
      val mebius = {
        // 変換を冪等にするために新しいMebiusは弾くようにする
        if (isMebius(itemStack) && !isNewMebius(itemStack))
          itemStack
        else return None
      }

      val nickname = {
        val nbtItem = new NBTItem(mebius)
        val nicknameField = nbtItem.getString("nickname")

        if (nicknameField.isEmpty) None else Some(nicknameField)
      }

      val mebiusLevel = mebius.getItemMeta.getLore.get(4).replace(levelLoreRowPrefix, "").toInt
      val ownerName = mebius.getItemMeta.getLore.get(8).replaceFirst(ownerLoreRowPrefix, "")
      val mebiusName = mebius.getItemMeta.getDisplayName

      Some(OldMebiusRawProperty(ownerName, mebiusLevel, nickname, mebiusName))
    }

    case class OldMebiusRawProperty(
      ownerPlayerId: String,
      level: Int,
      ownerNicknameOverride: Option[String] = None,
      mebiusName: String
    )

  }

  import eu.timepit.refined.auto._

  def migrationFunction(
    itemStack: ItemStack
  )(implicit repository: UuidRepository[SyncIO], logger: Logger): ItemStack = {
    val OldMebiusRawProperty(ownerPlayerId, level, ownerNicknameOverride, mebiusName) =
      OldBukkitMebiusItemStackCodec
        .decodeOldMebiusProperty(itemStack)
        .getOrElse(return itemStack)

    val clone = itemStack.clone()

    val ownerUuid =
      repository
        .getUuid(ownerPlayerId)
        // プレーヤーUUID解決は同期的な実行を待つ以外選択肢が無い
        .unsafeRunSync()
        .getOrElse {
          logger.error(s"メビウス変換にて、${ownerPlayerId}のプレーヤーUUIDが解決できませんでした。")

          // 解決できなかった場合ItemStackにエラーを書き込む。
          clone.setItemMeta {
            val meta = clone.getItemMeta
            val lore = meta.getLore.asScala

            // 冪等性のため、すでにエラーが書かれているケースを除外する
            if (!lore.contains(ownerResolutionError)) {
              meta.setLore {
                lore.append(ownerResolutionError).asJava
              }
            }
            meta
          }
          return clone
        }

    val nbtItem = new NBTItem(clone)

    // 今後、Mebiusかどうかの判別はこのタグをチェックすればよい
    nbtItem.setByte("mebiusTypeId", 1.toByte)

    nbtItem.setString("mebiusOwnerName", ownerPlayerId)
    nbtItem.setInteger("mebiusLevel", level)
    // "nickname" -> "mebiusOwnerNickname"
    ownerNicknameOverride.foreach(nbtItem.setString("mebiusOwnerNickname", _))
    nbtItem.setString("mebiusName", mebiusName)
    nbtItem.setString("mebiusOwnerUUID", ownerUuid.toString)

    nbtItem.getItem
  }

  def migration(
    implicit uuidRepository: UuidRepository[SyncIO],
    logger: Logger
  ): ItemMigration = ItemMigration(
    ItemMigrationVersionNumber(1, 0, 0),
    MigrationHelper.delegateConversionForContainers(migrationFunction)
  )
}
