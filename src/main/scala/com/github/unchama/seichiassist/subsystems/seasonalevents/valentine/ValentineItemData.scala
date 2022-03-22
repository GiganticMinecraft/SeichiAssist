package com.github.unchama.seichiassist.subsystems.seasonalevents.valentine

import com.github.unchama.seichiassist.subsystems.seasonalevents.valentine.Valentine.{END_DATE_TIME, EVENT_DURATION, EVENT_YEAR}
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.{Bukkit, Material}

import java.time.LocalDateTime
import java.util.UUID
import scala.jdk.CollectionConverters._
import scala.util.chaining._

object ValentineItemData {
  private val baseLore = List(
    s"${GRAY}食べると一定時間ステータスが変化する。",
    s"${GRAY}消費期限を超えると効果が無くなる。",
    "",
    s"${DARK_GREEN}消費期限：$END_DATE_TIME",
    s"${AQUA}ステータス変化（10分）$GRAY （期限内）"
  ).map(str => s"$RESET$str")

  private val cookieName = s"$GOLD${BOLD}チョコチップクッキー"

  def isCookie(item: ItemStack): Boolean = item != null && item.getType == Material.COOKIE

  def isUsableCookie(item: ItemStack): Boolean = {
    val now = LocalDateTime.now()
    val exp = new NBTItem(item).getObject(NBTTagConstants.expiryDateTimeTag, classOf[LocalDateTime])
    now.isBefore(exp) || now.isEqual(exp)
  }

  // region DroppedCookie -> 爆発したmobからドロップするやつ

  private val DroppedCookieTypeId = 3

  val droppedCookie: ItemStack = {
    val loreList = {
      List("", s"$RESET${GRAY}リア充を爆発させて奪い取った。") ++ baseLore
    }.asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.COOKIE).tap { meta =>
      import meta._
      setDisplayName(cookieName)
      setLore(loreList)
    }

    val itemStack = new ItemStack(Material.COOKIE, 1)
    itemStack.setItemMeta(itemMeta)

    new NBTItem(itemStack)
      .tap { item =>
        import item._
        setByte(NBTTagConstants.typeIdTag, DroppedCookieTypeId.toByte)
        setObject(NBTTagConstants.expiryDateTimeTag, EVENT_DURATION.to)
      }
      .pipe(_.getItem)
  }

  def isDroppedCookie(item: ItemStack): Boolean =
    isCookie(item) && new NBTItem(item).getByte(NBTTagConstants.typeIdTag) == DroppedCookieTypeId

  // endregion

  // region GiftedCookie -> 棒メニューでもらえるやつ

  private val GiftedCookieTypeId = 4

  def cookieOf(playerName: String, playerUuid: UUID): ItemStack = {
    val loreList = {
      val header = List("", s"$RESET${GRAY}手作りのチョコチップクッキー。")
      val producer = List(s"$RESET${DARK_GREEN}製作者：$playerName")

      header ++ baseLore ++ producer
    }.asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.COOKIE).tap { meta =>
      import meta._
      setDisplayName(cookieName)
      setLore(loreList)
    }

    val itemStack = new ItemStack(Material.COOKIE, 64)
    itemStack.setItemMeta(itemMeta)

    new NBTItem(itemStack)
      .tap { item =>
        import item._
        setByte(NBTTagConstants.typeIdTag, GiftedCookieTypeId.toByte)
        setObject(NBTTagConstants.expiryDateTimeTag, EVENT_DURATION.to)
        setObject(NBTTagConstants.producerUuidTag, playerUuid)
        setString(NBTTagConstants.producerNameTag, playerName)
      }
      .pipe(_.getItem)
  }

  def isGiftedCookie(item: ItemStack): Boolean =
    isCookie(item) && new NBTItem(item).getByte(NBTTagConstants.typeIdTag) == GiftedCookieTypeId

  def ownerOf(item: ItemStack): Option[UUID] =
    Option(new NBTItem(item).getObject(NBTTagConstants.producerUuidTag, classOf[UUID]))

  def deathMessages(playerName: String, cookieProducerName: String): Seq[String] = Seq(
    s"${playerName}は${cookieProducerName}のチョコチップクッキーを食べた！猟奇的な味だった。",
    s"$playerName！${cookieProducerName}からのチョコチップクッキーだと思ったかい？ざぁんねんっ！",
    s"${playerName}は${cookieProducerName}のプレゼントで鼻血が止まらない！（計画通り）",
    s"${playerName}は${cookieProducerName}のチョコチップクッキーを頬張ったまま息絶えた！",
    s"${playerName}は${cookieProducerName}のチョコチップクッキーにアレが入っているとはを知らずに食べた…",
    s"${playerName}は${cookieProducerName}のチョコチップクッキーなんか食ってないであくしろはたらけ",
    s"${cookieProducerName}は${playerName}に日頃の恨みを晴らした！スッキリ！",
    s"${cookieProducerName}による${playerName}への痛恨の一撃！ハッピーバレンタインッ！",
    s"${cookieProducerName}は${playerName}が食べる姿を、満面の笑みで見つめている！",
    s"${cookieProducerName}は悪くない！${playerName}が悪いんだっ！",
    s"${cookieProducerName}は${playerName}を討伐した！",
    s"こうして${cookieProducerName}のイタズラでまた1人${playerName}が社畜となった。",
    s"おい聞いたか！${cookieProducerName}が${playerName}にチョコチップクッキー送ったらしいぞー！"
  )

  // endregion

  // SeichiAssistで呼ばれてるだけ
  def valentinePlayerHead(head: SkullMeta): SkullMeta = {
    val lore = List(
      "",
      s"$GREEN${ITALIC}大切なあなたへ。",
      s"$YELLOW$UNDERLINE${ITALIC}Happy Valentine $EVENT_YEAR"
    ).map(str => s"$RESET$str").asJava
    head.setLore(lore)
    head
  }

  object NBTTagConstants {
    val typeIdTag = "valentineCookieTypeId"
    val expiryDateTimeTag = "valentineCookieExpiryDateTime"
    val producerNameTag = "valentineCookieProducerName"
    val producerUuidTag = "valentineCookieProducerUuid"
  }

}
