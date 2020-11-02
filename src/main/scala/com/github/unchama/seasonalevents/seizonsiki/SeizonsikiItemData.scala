package com.github.unchama.seasonalevents.seizonsiki

import com.github.unchama.seasonalevents.seizonsiki.Seizonsiki.{DISPLAYED_END_DATE, END_DATE}
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.inventory.ItemStack
import org.bukkit.{Bukkit, Material}

import scala.jdk.CollectionConverters._
import scala.util.chaining._

object SeizonsikiItemData {
  val seizonsikiZongo: ItemStack = {
    val loreList = List(
      "",
      s"${GRAY}成ゾン式で暴走していたチャラゾンビから没収した。",
      s"${GRAY}ゾンビたちが栽培しているりんご。",
      s"${GRAY}良質な腐葉土で1つずつ大切に育てられた。",
      s"${GRAY}栄養豊富で、食べるとマナが10%回復する。",
      s"${GRAY}腐りやすいため消費期限を超えると効果が無くなる。",
      "",
      s"${DARK_GREEN}消費期限：$DISPLAYED_END_DATE",
      s"${AQUA}マナ回復（10％）$GRAY （期限内）"
    ).map(str => s"$RESET$str")
      .asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.GOLDEN_APPLE)
      .tap(_.setDisplayName(s"$GOLD${BOLD}ゾんご"))
      .tap(_.setLore(loreList))

    val itemStack = new ItemStack(Material.GOLDEN_APPLE, 1)
    itemStack.setItemMeta(itemMeta)

    new NBTItem(itemStack)
      .tap(_.setByte(NBTTagConstants.typeIdTag, 1.toByte))
      .tap(_.setObject(NBTTagConstants.expirationDateTag, END_DATE))
      .pipe(_.getItem)
  }

  def isZongo(item: ItemStack): Boolean = {
    item != null && item.getType != Material.AIR && {
      new NBTItem(item).getByte(NBTTagConstants.typeIdTag) == 1
    }
  }

  def deathMessages(playerName: String): Seq[String] = Seq(
    "やっぱりゾンビが持ってたりんごなんだから体に悪い可能性だってあるよね",
    "毒！？毒りんごだったの！？あのチャラゾンビめ...",
    s"${playerName}は消費期限切れのゾンごを食べて死亡した",
    "賞味期限は少しくらい無視してもいいけど、消費期限は守ろうね！unchamaとのお約束だぞ！",
    "腐った食べ物には腐敗菌が作り出した毒素や毒物がたくさん含まれているらしいよ",
    "マナ妖精「消費期限切れのゾンごなんて僕でも食べないよ！」",
    s"$playerName「色も匂いも全然変わってないので消費期限切れだけど食べてもヨシ！」",
    "？？？「どうして期限切れのゾンごを食べようとしたんですか？」",
    "[Tips] りんごは他の野菜や果物と冷蔵庫に入れると熟成を早めちゃうから別々にしよう！"
  )

  object NBTTagConstants {
    val typeIdTag = "seizonsikiZongoTypeId"
    val expirationDateTag = "seizonsikiZongoExpirationDate"
  }
}