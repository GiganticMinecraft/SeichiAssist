package com.github.unchama.seichiassist.subsystems.gachaprize.bukkit.factories

import com.github.unchama.seichiassist.subsystems.gachaprize.domain.StaticGachaPrizeFactory
import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.inventory.{ItemFlag, ItemStack}

import java.util

object BukkitStaticGachaPrizeFactory extends StaticGachaPrizeFactory[ItemStack] {

  /**
   * がちゃりんごのロール
   * TODO: `ItemData.java`がScalaに置き換えられたら[[ List[String] ]]にしてしまって良さそう
   */
  val gachaRingoLore: util.List[String] =
    util.Arrays.asList(s"$RESET${GRAY}序盤に重宝します。", s"$RESET${AQUA}マナ回復（小）")

  import scala.jdk.CollectionConverters._
  import scala.util.chaining.scalaUtilChainingOps

  override val gachaRingo: ItemStack = new ItemStack(Material.GOLDEN_APPLE, 1).tap {
    itemStack =>
      import itemStack._
      val meta = getItemMeta
      meta.setDisplayName(s"$GOLD${BOLD}がちゃりんご")
      meta.setLore(gachaRingoLore)
      setItemMeta(meta)
  }

  override val mineHeadItem: ItemStack =
    new ItemStack(Material.CARROT_ON_A_STICK, 1, 1.toShort).tap { itemStack =>
      import itemStack._
      val meta = getItemMeta
      meta.setDisplayName(s"${DARK_RED}死神の鎌")
      meta.setLore(
        List(
          s"${RED}頭を狩り取る形をしている...",
          "",
          s"${GRAY}設置してある頭が",
          s"${GRAY}左クリックで即時に回収できます",
          s"${DARK_GRAY}インベントリに空きを作って使いましょう"
        ).asJava
      )
      meta.setUnbreakable(true)
      meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
      setItemMeta(meta)
    }

}
