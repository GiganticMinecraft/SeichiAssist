package com.github.unchama.seichiassist.subsystems.gachaprize.bukkit.factories

import com.github.unchama.seichiassist.util.ItemMetaFactory
import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object BukkitGachaSkullData {

  import scala.jdk.CollectionConverters._
  import scala.util.chaining._

  /**
   * ノーマルガチャ券
   */
  val gachaSkull: ItemStack =
    new ItemStack(Material.PLAYER_HEAD, 1).tap { skull =>
      import skull._
      setDurability(3)
      setItemMeta {
        ItemMetaFactory.SKULL.getValue.tap { meta =>
          import meta._
          setDisplayName(s"$YELLOW${BOLD}ガチャ券")
          setLore {
            List(s"$RESET${GREEN}右クリックで使えます").asJava
          }

          // 参加したことのないプレーヤーはgetOfflinePlayerでデータが取れないのでこうするしか無い
          setOwner("unchama")
        }
      }
    }

  /**
   * 投票報酬のガチャ券
   */
  val gachaForVoting: ItemStack =
    new ItemStack(Material.PLAYER_HEAD, 1).tap { itemStack =>
      import itemStack._
      setDurability(3)
      setItemMeta {
        ItemMetaFactory.SKULL.getValue.tap { meta =>
          import meta._
          setDisplayName(s"$YELLOW${BOLD}ガチャ券")
          setLore {
            List(s"$RESET${GREEN}右クリックで使えます", s"$RESET${LIGHT_PURPLE}投票ありがとナス♡").asJava
          }

          // 参加したことのないプレーヤーはgetOfflinePlayerでデータが取れないのでこうするしか無い
          setOwner("unchama")
        }
      }
    }

  /**
   * ガチャ景品（当たり・大当たり）とガチャ券の交換システムで手に入るガチャ券
   */
  val gachaForExchanging: ItemStack = {
    new ItemStack(Material.PLAYER_HEAD, 1).tap { itemStack =>
      import itemStack._
      setDurability(3)
      setItemMeta {
        ItemMetaFactory.SKULL.getValue.tap { meta =>
          import meta._
          setDisplayName(s"$YELLOW${BOLD}ガチャ券")
          setLore {
            List(s"$RESET${GREEN}右クリックで使えます", s"$RESET${GRAY}ガチャ景品と交換しました。").asJava
          }

          // 参加したことのないプレーヤーはgetOfflinePlayerでデータが取れないのでこうするしか無い
          setOwner("unchama")
        }
      }
    }
  }
}
