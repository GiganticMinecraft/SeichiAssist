package com.github.unchama.seichiassist.subsystems.gachaprize.bukkit.factories

import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.util.ItemMetaFactory
import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object BukkitGachaSkullData {

  import scala.util.chaining._

  /**
   * ノーマルガチャ券
   */
  val gachaSkull: ItemStack =
    new ItemStack(Material.PLAYER_HEAD, 1).tap { skull =>
      import skull._
      setDurability(3)
      setItemMeta {
        val meta = ItemMetaFactory.SKULL.getValue
        new SkullItemStackBuilder(SkullOwners.unchama)
          .title(s"$YELLOW${BOLD}ガチャ券")
          .lore(List(s"$RESET${GREEN}右クリックで使えます"))
          .transformItemMetaOnBuild(meta)
        meta
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
        val meta = ItemMetaFactory.SKULL.getValue
        new SkullItemStackBuilder(SkullOwners.unchama)
          .title(s"$YELLOW${BOLD}ガチャ券")
          .lore(List(s"$RESET${GREEN}右クリックで使えます", s"$RESET${LIGHT_PURPLE}投票ありがとナス♡"))
          .transformItemMetaOnBuild(meta)
        meta
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
        val meta = ItemMetaFactory.SKULL.getValue
        new SkullItemStackBuilder(SkullOwners.unchama)
          .title(s"$YELLOW${BOLD}ガチャ券")
          .lore(List(s"$RESET${GREEN}右クリックで使えます", s"$RESET${GRAY}ガチャ景品と交換しました。"))
          .transformItemMetaOnBuild(meta)
        meta
      }
    }
  }
}
