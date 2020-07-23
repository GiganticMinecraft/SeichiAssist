package com.github.unchama.seichiassist.mebius.controller.listeners

import java.util.Objects

import com.github.unchama.seichiassist.mebius.controller.listeners.MebiusListener._
import com.github.unchama.seichiassist.mebius.domain.PropertyModificationMessages
import com.github.unchama.seichiassist.mebius.domain.resources.MebiusMessages
import com.github.unchama.seichiassist.{MaterialSets, SeichiAssist}
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor.{RED, RESET}
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.{Monster, Player}
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.{EntityDamageByEntityEvent, EntityDeathEvent}
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}
import org.bukkit.inventory.{ItemFlag, ItemStack}
import org.bukkit.{Bukkit, ChatColor, Material, Sound}

import scala.collection.mutable
import scala.util.Random

// TODO cleanup
object MebiusListener {

  import scala.jdk.CollectionConverters._

  /** 初期の名前 */
  private val DEFNAME = "MEBIUS"

  private val LOREFIRST2 = List(
    s"$RESET",
    s"$RESET${ChatColor.AQUA}初心者をサポートする不思議なヘルメット。",
    s"$RESET${ChatColor.AQUA}整地により成長する。",
    ""
  )
  private val LV = 4
  private val TALK = 5
  private val DEST = 6
  private val NAMEHEAD = s"$RESET${ChatColor.GOLD}${ChatColor.BOLD}"
  private val ILHEAD = s"$RESET$RED${ChatColor.BOLD}アイテムLv. "
  private val TALKHEAD = s"$RESET${ChatColor.GOLD}${ChatColor.ITALIC}"
  private val DESTHEAD = s"$RESET${ChatColor.GRAY}${ChatColor.ITALIC}"
  private val OWNERHEAD = s"$RESET${ChatColor.DARK_GREEN}所有者："

  /** 見た目テーブル */
  private val APPEARANCE = Map(
    1 -> Material.LEATHER_HELMET,
    5 -> Material.GOLD_HELMET,
    10 -> Material.CHAINMAIL_HELMET,
    20 -> Material.IRON_HELMET,
    25 -> Material.DIAMOND_HELMET
  )

  /** Mebiusを装備しているか */
  def isEquip(player: Player): Boolean =
    try isMebius(player.getInventory.getHelmet)
    catch {
      case _: NullPointerException => false
    }

  /** MebiusのDisplayNameを設定 */
  def setName(player: Player, name: String): Boolean = {
    if (isEquip(player)) {
      val mebius = player.getInventory.getHelmet
      val meta = mebius.getItemMeta
      meta.setDisplayName(s"$NAMEHEAD$name")
      player.sendMessage(s"${getName(mebius)}${RESET}に命名しました。")
      mebius.setItemMeta(meta)
      player.getInventory.setHelmet(mebius)
      getPlayerData(player).mebius.speakForce(s"わーい、ありがとう！今日から僕は$NAMEHEAD$name${RESET}だ！")
      return true
    }
    false
  }

  /** MebiusのDisplayNameを取得 */
  def getName(mebius: ItemStack): String = {
    try if (isMebius(mebius)) return mebius.getItemMeta.getDisplayName
    catch {
      case _: NullPointerException =>
    }
    s"$NAMEHEAD$DEFNAME"
  }

  def setNickname(player: Player, name: String): Boolean = if (!isEquip(player)) false
  else {
    val mebius = player.getInventory.getHelmet
    val nbtItem = new NBTItem(mebius)
    nbtItem.setString("nickname", name)
    player.getInventory.setHelmet(nbtItem.getItem)
    getPlayerData(player).mebius.speakForce("わーい、ありがとう！今日から君のこと" + ChatColor.GREEN + name + RESET + "って呼ぶね！")
    true
  }

  // FIXME あの！ここはListenerクラスですよ！！
  def getNickname(player: Player): String =
    if (!isEquip(player)) null
    else {
      val mebius = player.getInventory.getHelmet
      val nbtItem = new NBTItem(mebius)
      if (nbtItem.getString("nickname").isEmpty) {
        nbtItem.setString("nickname", player.getName)
        player.getName
      }
      else nbtItem.getString("nickname")
    }

  // PlayerData取得
  private def getPlayerData(player: Player) = SeichiAssist.playermap.apply(player.getUniqueId)

  // ItemStackがMebiusか
  private def isMebius(item: ItemStack): Boolean = {
    val meta = item.getItemMeta

    meta.hasLore && {
      val lore = meta.getLore.asScala
      LOREFIRST2.forall(lore.contains)
    }
  }

  // MebiusのLvを取得
  def getMebiusLevel(mebius: ItemStack): Int = {
    mebius.getItemMeta.getLore.get(LV).replace(ILHEAD, "").toInt
  }

  // 新しいMebiusのひな形を作る
  def create(mebius: ItemStack, player: Player): ItemStack = {
    val (name, nickname, level, enchantments) =
      if (mebius != null) {
        val level = getMebiusLevel(mebius) + 1
        val name = mebius.getItemMeta.getDisplayName
        val enchantments = mebius.getItemMeta.getEnchants.asScala.view.mapValues(_.toInt).toMap
        val nickname = new NBTItem(mebius).getString("nickname")

        // Mebiusの進化を通知する
        // FIXME createなのに通知ロジックがある
        player.sendMessage(s"$name${RESET}の見た目が進化しました。")

        (name, nickname, level, enchantments)
      } else {
        (NAMEHEAD + DEFNAME, "", 1, Map[Enchantment, Int]())
      }

    val newMebius = new ItemStack(APPEARANCE(level))

    val meta = Bukkit.getItemFactory.getItemMeta(APPEARANCE(level))
    meta.setDisplayName(name)

    // Lore生成
    val lore = LOREFIRST2.concat(List(ILHEAD + level, "", "", "", OWNERHEAD + player.getName.toLowerCase))

    meta.setLore(updateTalkDest(lore, level).asJava)

    // エンチャントを付与する
    enchantments.foreachEntry { case (enchantment, level) =>
      meta.removeEnchant(enchantment)
      meta.addEnchant(enchantment, level, true)
    }

    // フラグ設定
    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
    meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
    newMebius.setItemMeta(meta)

    val nbtItem = new NBTItem(newMebius)
    nbtItem.setString("nickname", nickname)

    nbtItem.getItem
  }

  // Talk更新
  private def updateTalkDest(currentLore: Iterable[String], level: Int): List[String] = {
    val currentLoreView = mutable.ListBuffer.from(currentLore)

    LOREFIRST2.zipWithIndex.foreach { case (row, index) => currentLoreView(index) = row }

    val talk = MebiusMessages.talkOnLevelUp(level)

    currentLoreView(LV) = ILHEAD + level
    currentLoreView(TALK) = s"$TALKHEAD「${talk.mebiusMessage}」"
    currentLoreView(DEST) = s"$DESTHEAD${talk.playerMessage}"

    currentLoreView.toList
  }

}
