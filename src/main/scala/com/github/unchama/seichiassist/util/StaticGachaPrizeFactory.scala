package com.github.unchama.seichiassist.util

import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object StaticGachaPrizeFactory {
  private val gachaRingo = new IconItemStackBuilder(Material.GOLDEN_APPLE)
    .amount(1)
    .title(getGachaRingoName)
    .lore(getGachaRingoLore)
    .build()
  private val sickleOfDeathGod = new IconItemStackBuilder(Material.CARROT_STICK, 1)
    .amount(1)
    .title(getMineHeadItemName)
    .lore(getMineHeadItemLore)
    .unbreakable()
    .build()

  /**
   * @return ガチャりんごを表すItemStackを返す。
   */
  def getGachaRingo: ItemStack = {
    gachaRingo.clone()
  }

  //がちゃりんごの名前を取得
  def getGachaRingoName: String = s"$GOLD${BOLD}がちゃりんご"

  //がちゃりんごの説明を取得
  def getGachaRingoLore: List[String] = List(
    s"$RESET${GRAY}序盤に重宝します。",
    s"$RESET${AQUA}マナ回復（小）"
  )

  /**
   * @return 椎名林檎を表すItemStackを返す。
   */
  def getMaxRingo(name: String): ItemStack = {
    new IconItemStackBuilder(Material.GOLDEN_APPLE, /* 上位リンゴ */ 1)
      .amount(1)
      .lore(getMaxRingoLore(name))
      .title(s"$YELLOW$BOLD${ITALIC}椎名林檎")
      .build()
  }

  //椎名林檎の説明を取得
  private def getMaxRingoLore(name: String): List[String] = List(
    s"$RESET${GRAY}使用するとマナが全回復します",
    s"$RESET${AQUA}マナ完全回復",
    s"$RESET${DARK_GREEN}所有者:$name",
    s"$RESET${GRAY}ガチャ景品と交換しました。"
  )

  /**
   * @return 死神の鎌を表すItemStackを返す。
   */
  def getMineHeadItem: ItemStack = {
    sickleOfDeathGod.clone()
  }

  private def getMineHeadItemName: String = s"${DARK_RED}死神の鎌"

  private def getMineHeadItemLore: List[String] = List(
    s"${RED}頭を狩り取る形をしている...",
    "",
    s"${GRAY}設置してある頭が",
    s"${GRAY}左クリックで即時に回収できます",
    s"${DARK_GRAY}インベントリに空きを作って使いましょう"
  )
}

