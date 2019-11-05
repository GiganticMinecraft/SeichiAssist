package com.github.unchama.seichiassist.util.ops

import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object ItemStackOps {

  implicit class ItemStackOps(val itemStack: ItemStack) {

    import scala.jdk.CollectionConverters._

    def appendOwnerInformation(owner: Player): Unit = {
      lore =
        (if (itemStack.getItemMeta.hasLore) this.lore else Nil) ++
          List(s"$RESET${DARK_GREEN}所有者：${owner.getName}")
    }

    /**
     * [ItemStack] の説明文を表すプロパティ.
     *
     * getter は説明文のコピーを返し,
     * setterはこの[ItemStack]の説明文を与えられた[String]の[List]に書き換える.
     */
    def lore: List[String] = {
      itemStack.getItemMeta.getLore
      }.asScala.toList

    def lore_=(value: List[String]): Unit = {
      import scala.util.chaining._

      itemStack.tap { i => import i._
        setItemMeta {
          getItemMeta.tap(_.setLore(value.asJava))
        }
      }
    }

  }

}
