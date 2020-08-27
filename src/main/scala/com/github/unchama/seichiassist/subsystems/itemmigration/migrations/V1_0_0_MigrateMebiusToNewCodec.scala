package com.github.unchama.seichiassist.subsystems.itemmigration.migrations

import cats.effect.SyncIO
import com.github.unchama.itemmigration.domain.{ItemMigration, ItemMigrationVersionNumber}
import com.github.unchama.itemmigration.util.MigrationHelper
import com.github.unchama.seichiassist.domain.minecraft.UuidRepository
import com.github.unchama.seichiassist.subsystems.itemmigration.migrations.V1_0_0_MigrateMebiusToNewCodec.OldBukkitMebiusItemStackCodec.OldMebiusRawProperty
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.inventory.ItemStack
import org.slf4j.Logger

object V1_0_0_MigrateMebiusToNewCodec {

  import scala.jdk.CollectionConverters._

  //noinspection DuplicatedCode
  object OldBukkitMebiusItemStackCodec {

    import de.tr7zw.itemnbtapi.NBTItem
    import org.bukkit.inventory.ItemStack

    private val mebiusLoreHead = List(
      s"$RESET",
      s"$RESET${AQUA}初心者をサポートする不思議なヘルメット。",
      s"$RESET${AQUA}整地により成長する。",
      ""
    )
    private val ownerLoreRowPrefix = s"$RESET${DARK_GREEN}所有者："
    private val levelLoreRowPrefix = s"$RESET$RED${BOLD}アイテムLv. "

    /**
     * (必ずしも有効な`MebiusProperty`を持つとは限らない)実体から `ItemStack` をデコードする。
     */
    def decodeOldMebiusProperty(itemStack: ItemStack): Option[OldMebiusRawProperty] = {
      val mebius = if (isMebius(itemStack)) itemStack else return None

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

    def isMebius(itemStack: ItemStack): Boolean = {
      val meta = if (itemStack != null) itemStack.getItemMeta else return false

      meta != null && meta.hasLore && {
        val lore = meta.getLore.asScala
        mebiusLoreHead.forall(lore.contains)
      }
    }

    case class OldMebiusRawProperty(ownerPlayerId: String,
                                    level: Int,
                                    ownerNicknameOverride: Option[String] = None,
                                    mebiusName: String)

  }

  import eu.timepit.refined.auto._

  def migrationFunction(itemStack: ItemStack)(implicit
                                              repository: UuidRepository[SyncIO],
                                              logger: Logger): ItemStack = {
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
            meta.setLore {
              meta.getLore.asScala
                .append(s"$RESET${DARK_RED}エラー：所有者が見つかりません。")
                .asJava
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

  def migration(implicit uuidRepository: UuidRepository[SyncIO], logger: Logger): ItemMigration = ItemMigration(
    ItemMigrationVersionNumber(1, 0, 0),
    MigrationHelper.delegateConversionForContainers(migrationFunction)
  )
}
