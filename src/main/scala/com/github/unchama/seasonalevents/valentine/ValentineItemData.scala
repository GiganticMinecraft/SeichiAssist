package com.github.unchama.seasonalevents.valentine

import java.time.LocalDate
import java.util.UUID

import com.github.unchama.seasonalevents.valentine.Valentine.{END_DATE, isInEvent}
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.{Bukkit, Material}

import scala.jdk.CollectionConverters._
import scala.util.chaining._

object ValentineItemData {
  private val baseLore = List(
    s"${GRAY}食べると一定時間ステータスが変化する。",
    s"${GRAY}消費期限を超えると効果が無くなる。",
    "",
    s"${DARK_GREEN}消費期限：$END_DATE",
    s"${AQUA}ステータス変化（10分）$GRAY （期限内）"
  ).map(str => s"$RESET$str")

  private val cookieName = s"$GOLD${BOLD}チョコチップクッキー"

  def isUsableCookie(item: ItemStack): Boolean = {
    val today = LocalDate.now()
    val exp = new NBTItem(item).getObject(NBTTagConstants.expirationDateTag, classOf[LocalDate])
    today.isBefore(exp)
  }

  //region DroppedCookie -> 爆発したmobからドロップするやつ

  val droppedCookie: ItemStack = {
    val loreList = {
      List(
        "",
        s"$RESET${GRAY}リア充を爆発させて奪い取った。"
      ) ++ baseLore
    }.asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.COOKIE)
      .tap(_.setDisplayName(cookieName))
      .tap(_.setLore(loreList))

    val itemStack = new ItemStack(Material.COOKIE, 1)
    itemStack.setItemMeta(itemMeta)

    new NBTItem(itemStack)
      .tap(_.setByte(NBTTagConstants.typeIdTag, 1.toByte))
      .tap(_.setObject(NBTTagConstants.expirationDateTag, END_DATE))
      .pipe(_.getItem)
  }

  def isDroppedCookie(item: ItemStack): Boolean =
    item != null && item.getType != Material.AIR && {
      new NBTItem(item).getByte(NBTTagConstants.typeIdTag) == 1
    }

  //endregion

  //region GiftedCookie -> 棒メニューでもらえるやつ

  def cookieOf(player: Player): ItemStack = {
    val playerName = player.getName
    val loreList = {
      val header = List(
        "",
        s"$RESET${GRAY}手作りのチョコチップクッキー。")
      val producer = List(s"$RESET${DARK_GREEN}製作者：$playerName")

      header ++ baseLore ++ producer
    }.asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.COOKIE)
      .tap(_.setDisplayName(cookieName))
      .tap(_.setLore(loreList))

    val itemStack = new ItemStack(Material.COOKIE, 64)
    itemStack.setItemMeta(itemMeta)

    new NBTItem(itemStack)
      .tap(_.setByte(NBTTagConstants.typeIdTag, 2.toByte))
      .tap(_.setObject(NBTTagConstants.expirationDateTag, END_DATE))
      .tap(_.setObject(NBTTagConstants.producerUuidTag, player.getUniqueId))
      .tap(_.setString(NBTTagConstants.producerNameTag, playerName))
      .pipe(_.getItem)
  }

  def isGiftedCookie(item: ItemStack): Boolean =
    item != null && item.getType != Material.AIR && {
      new NBTItem(item).getByte(NBTTagConstants.typeIdTag) == 2
    }

  def ownerOf(item: ItemStack): Option[UUID] =
    Option(new NBTItem(item).getObject(NBTTagConstants.producerUuidTag, classOf[UUID]))

  def deathMessages(playerName: String, cookieProducerName: String): Seq[String] = Seq(
    s"${playerName}は${cookieProducerName}のチョコレートを食べた！猟奇的な味だった。",
    s"$playerName！${cookieProducerName}からのチョコだと思ったかい？ざぁんねんっ！",
    s"${playerName}は${cookieProducerName}のプレゼントで鼻血が止まらない！（計画通り）",
    s"${playerName}は${cookieProducerName}のチョコレートを頬張ったまま息絶えた！",
    s"${playerName}は${cookieProducerName}のチョコにアレが入っているとはを知らずに食べた…",
    s"${playerName}は${cookieProducerName}のチョコなんか食ってないであくしろはたらけ",
    s"${cookieProducerName}は${playerName}に日頃の恨みを晴らした！スッキリ！",
    s"${cookieProducerName}による${playerName}への痛恨の一撃！ハッピーバレンタインッ！",
    s"${cookieProducerName}は${playerName}が食べる姿を、満面の笑みで見つめている！",
    s"${cookieProducerName}は悪くない！${playerName}が悪いんだっ！",
    s"${cookieProducerName}は${playerName}を討伐した！",
    s"こうして${cookieProducerName}のイタズラでまた1人${playerName}が社畜となった。",
    s"おい聞いたか！${cookieProducerName}が${playerName}にチョコ送ったらしいぞー！"
  )

  //endregion

  // SeichiAssistで呼ばれてるだけ
  def valentinePlayerHead(head: SkullMeta): SkullMeta = {
    if (isInEvent) {
      val year: String = END_DATE.getYear.toString
      val lore = List(
        "",
        s"$GREEN${ITALIC}大切なあなたへ。",
        s"$UNDERLINE$YELLOW${ITALIC}Happy Valentine $year"
      ).map(str => s"$RESET$str")
        .asJava
      head.setLore(lore)
    }
    head
  }

  object NBTTagConstants {
    val typeIdTag = "valentineCookieTypeId"
    val expirationDateTag = "valentineCookieExpirationDate"
    val producerNameTag = "valentineCookieProducerName"
    val producerUuidTag = "valentineCookieProducerUuid"
  }
}