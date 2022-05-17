package com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaprizefactory.bukkit

import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.inventory.{ItemFlag, ItemStack}

object StaticGachaPrizeFactory {

  /**
   * がちゃりんごの名前
   */
  val gachaRingoName = s"$GOLD${BOLD}がちゃりんご"

  /**
   * がちゃりんごのロール
   */
  val gachaRingoLore = List(s"$RESET${GRAY}序盤に重宝します。", s"$RESET${AQUA}マナ回復（小）")

  import scala.jdk.CollectionConverters._
  import scala.util.chaining.scalaUtilChainingOps

  /**
   * がちゃりんごの[[ItemStack]]を返す
   */
  val gachaRingo: ItemStack = new ItemStack(Material.GOLDEN_APPLE, 1).tap { itemStack =>
    import itemStack._
    val meta = getItemMeta
    meta.setDisplayName(s"$GOLD${BOLD}がちゃりんご")
    meta.setLore(gachaRingoLore.asJava)
    setItemMeta(meta)
    clone()
  }

  /**
   * 所有者名を渡して椎名林檎の[[ItemStack]]を返す
   */
  val getMaxRingo: String => ItemStack = (name: String) =>
    new ItemStack(Material.GOLDEN_APPLE, 1).tap { itemStack =>
      import itemStack._
      setDurability(1.toShort)
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
      clone()
    }

  /**
   * 死神の鎌の[[ItemStack]]を返す
   * TODO: これはここに書かれるべきではなさそう？
   *  ガチャアイテムとして排出されていないので景品ではないため。
   */
  val mineHeadItem: ItemStack = new ItemStack(Material.CARROT_STICK, 1).tap { itemStack =>
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
