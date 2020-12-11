package com.github.unchama.seichiassist.data

import com.github.unchama.seichiassist.util.ItemMetaFactory
import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object GachaSkullData {

  import scala.jdk.CollectionConverters._
  import scala.util.chaining._

  /**
   * ノーマルガチャ券
   */
  val gachaSkull: ItemStack =
    new ItemStack(Material.SKULL_ITEM, 1).tap { skull =>
      import skull._
      setDurability(3.toShort)
      setItemMeta {
        ItemMetaFactory.SKULL.getValue.tap { skullMeta =>
          import skullMeta._
          setDisplayName(s"$YELLOW${BOLD}ガチャ券")
          setLore {
            List(
              s"$RESET${GREEN}右クリックで使えます"
            ).asJava
          }
          setOwner("unchama")
        }
      }
    }

  /**
   * お詫びガチャ券
   */
  val gachaFromAdministrator: ItemStack =
    new ItemStack(Material.SKULL_ITEM, 1).tap { itemStack =>
      import itemStack._
      setDurability(3)
      setItemMeta {
        ItemMetaFactory.SKULL.getValue.tap { meta =>
          import meta._
          setDisplayName(s"$YELLOW${BOLD}ガチャ券")
          setLore {
            List(
              s"$RESET${GREEN}右クリックで使えます",
              s"$RESET${DARK_RED}運営から不具合のお詫びです"
            ).asJava
          }
          setOwner("unchama")
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
            List(
              s"$RESET${GREEN}右クリックで使えます",
              s"$RESET${LIGHT_PURPLE}投票ありがとナス♡"
            ).asJava
          }
          setOwner("unchama")
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
            List(
              s"$RESET${GREEN}右クリックで使えます",
              s"$RESET${GRAY}ガチャ景品と交換しました。"
            ).asJava
          }
          setOwner("unchama")
        }
      }
    }
  }

  /**
   * 整地レベルアップ時に配布されるガチャ券
   */
  val gachaForSeichiLevelUp: ItemStack = {
    new ItemStack(Material.SKULL_ITEM, 1).tap { itemStack =>
      import itemStack._
      setDurability(3)
      setItemMeta {
        ItemMetaFactory.SKULL.getValue.tap { meta =>
          import meta._
          setDisplayName(s"$YELLOW${BOLD}ガチャ券")
          setLore {
            List(
              s"$RESET${GREEN}右クリックで使えます",
              s"$RESET${GRAY}レベルアップ記念です"
            ).asJava
          }
          setOwner("unchama")
        }
      }
    }
  }
}
