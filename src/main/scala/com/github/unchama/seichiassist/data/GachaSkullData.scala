package com.github.unchama.seichiassist.data

import cats.effect.SyncIO
import com.github.unchama.seichiassist.subsystems.itemmigration.domain.minecraft.UuidRepository
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.minecraft.JdbcBackedUuidRepository
import com.github.unchama.seichiassist.util.ItemMetaFactory
import org.bukkit.ChatColor._
import org.bukkit.{Bukkit, Material}
import org.bukkit.inventory.ItemStack

object GachaSkullData {

  import scala.jdk.CollectionConverters._
  import scala.util.chaining._

  private implicit val syncIOUuidRepository: UuidRepository[SyncIO] =
    JdbcBackedUuidRepository.initializeStaticInstance[SyncIO].unsafeRunSync().apply[SyncIO]

  /**
   * ノーマルガチャ券
   */
  val gachaSkull: ItemStack =
    new ItemStack(Material.SKULL_ITEM, 1).tap { skull =>
      import skull._
      setDurability(3)
      setItemMeta {
        ItemMetaFactory.SKULL.getValue.tap { meta =>
          import meta._
          setDisplayName(s"$YELLOW${BOLD}ガチャ券")
          setLore {
            List(s"$RESET${GREEN}右クリックで使えます").asJava
          }
          setOwningPlayer(
            Bukkit.getOfflinePlayer(syncIOUuidRepository.getUuid("unchama").unsafeRunSync().get)
          )
        }
      }
    }

  /**
   * 投票報酬のガチャ券
   */
  val gachaForVoting: ItemStack =
    new ItemStack(Material.SKULL_ITEM, 1).tap { itemStack =>
      import itemStack._
      setDurability(3)
      setItemMeta {
        ItemMetaFactory.SKULL.getValue.tap { meta =>
          import meta._
          setDisplayName(s"$YELLOW${BOLD}ガチャ券")
          setLore {
            List(s"$RESET${GREEN}右クリックで使えます", s"$RESET${LIGHT_PURPLE}投票ありがとナス♡").asJava
          }
          setOwningPlayer(
            Bukkit.getOfflinePlayer(syncIOUuidRepository.getUuid("unchama").unsafeRunSync().get)
          )
        }
      }
    }

  /**
   * ガチャ景品（当たり・大当たり）とガチャ券の交換システムで手に入るガチャ券
   */
  val gachaForExchanging: ItemStack = {
    new ItemStack(Material.SKULL_ITEM, 1).tap { itemStack =>
      import itemStack._
      setDurability(3)
      setItemMeta {
        ItemMetaFactory.SKULL.getValue.tap { meta =>
          import meta._
          setDisplayName(s"$YELLOW${BOLD}ガチャ券")
          setLore {
            List(s"$RESET${GREEN}右クリックで使えます", s"$RESET${GRAY}ガチャ景品と交換しました。").asJava
          }
          setOwningPlayer(
            Bukkit.getOfflinePlayer(syncIOUuidRepository.getUuid("unchama").unsafeRunSync().get)
          )
        }
      }
    }
  }
}
