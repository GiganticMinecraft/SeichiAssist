package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina.bukkit

import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina.domain.StaticTradeItemFactory
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.ChatColor._

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

object BukkitStaticTradeItemFactory extends StaticTradeItemFactory[ItemStack] {

  override val getMaxRingo: String => ItemStack = (name: String) =>
    new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1).tap { itemStack =>
      import itemStack._
      val meta = getItemMeta
      meta.setDisplayName(s"$YELLOW$BOLD${ITALIC}椎名林檎")
      meta.setLore(
        List(
          s"$RESET${GRAY}使用するとマナが全回復します",
          s"$RESET${AQUA}マナ完全回復",
          s"$RESET${DARK_GREEN}所有者:$name",
          s"$RESET${GRAY}ガチャ景品と交換しました。"
        ).asJava
      )
      setItemMeta(meta)
    }

}
