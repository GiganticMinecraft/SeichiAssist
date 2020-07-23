package com.github.unchama.seichiassist.mebius.controller.listeners

import java.util.Objects

import com.github.unchama.seichiassist.mebius.controller.codec.ItemStackMebiusCodec
import com.github.unchama.seichiassist.mebius.controller.listeners.MebiusListener._
import com.github.unchama.seichiassist.mebius.domain.resources.MebiusMessages
import com.github.unchama.seichiassist.mebius.service.MebiusLevellingService
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.seichiassist.{MaterialSets, SeichiAssist}
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor.{RED, RESET}
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.{Monster, Player}
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.{EntityDamageByEntityEvent, EntityDeathEvent}
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryDragEvent}
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}
import org.bukkit.inventory.{AnvilInventory, ItemFlag, ItemStack}
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

  // Mebiusドロップ率
  private val averageBlocksToBeBrokenPerMebiusDrop = 50000

  /** 見た目テーブル */
  private val APPEARANCE = Map(
    1 -> Material.LEATHER_HELMET,
    5 -> Material.GOLD_HELMET,
    10 -> Material.CHAINMAIL_HELMET,
    20 -> Material.IRON_HELMET,
    25 -> Material.DIAMOND_HELMET
  )

  /** エンチャント別レベル制限 */
  private val UNBREAK = s"$RESET${ChatColor.AQUA}耐久無限"
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

  // 新規Mebius発見処理(採掘時)
  private def discovery(player: Player): Unit = {
    val mebius = create(null, player)
    player.sendMessage(s"$RESET${ChatColor.YELLOW}${ChatColor.BOLD}おめでとうございます。採掘中にMEBIUSを発見しました。")
    player.sendMessage(s"$RESET${ChatColor.YELLOW}${ChatColor.BOLD}MEBIUSはプレイヤーと共に成長するヘルメットです。")
    player.sendMessage(s"$RESET${ChatColor.YELLOW}${ChatColor.BOLD}あなただけのMEBIUSを育てましょう！")
    Bukkit.getServer.getScheduler.runTaskLater(
      SeichiAssist.instance,
      () => getPlayerData(player).mebius
        .speakForce(s"こんにちは、${player.getName}$RESET。僕は${getName(mebius)}$RESET！これからよろしくね！"),
      10
    )

    player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
    if (!Util.isPlayerInventoryFull(player)) Util.addItem(player, mebius)
    else {
      player.sendMessage(s"$RESET$RED${ChatColor.BOLD}所持しきれないためMEBIUSをドロップしました。")
      Util.dropItem(player, mebius)
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

  // メッセージリストからランダムに取り出し、タグを置換する
  private def getMessage(messages: Set[String], str1: String, str2: String) = {
    var msg = messages.toList(Random.nextInt(messages.size))

    if (!str1.isEmpty) msg = msg.replace("[str1]", s"$str1$RESET")
    if (!str2.isEmpty) msg = msg.replace("[str2]", s"$str2$RESET")

    msg
  }
}

class MebiusListener() extends Listener {

  // ダメージを受けた時
  @EventHandler def onDamage(event: EntityDamageByEntityEvent): Unit = {
    // プレイヤーがダメージを受けた場合
    event.getEntity match {
      case player: Player =>
        // プレイヤーがMebiusを装備していない場合は除外
        if (!MebiusListener.isEquip(player)) return
        val mebius = player.getInventory.getHelmet
        // 耐久無限じゃない場合
        if (!mebius.getItemMeta.spigot.isUnbreakable) { // 耐久閾値を超えていたら破損警告
          val max = mebius.getType.getMaxDurability
          val dur = mebius.getDurability
          if (dur >= max - 10) {
            MebiusListener.getPlayerData(player).mebius
              .speak(MebiusListener.getMessage(MebiusMessages.onDamageBreaking, Objects.requireNonNull(MebiusListener.getNickname(player)), ""))
          }
        }
        // モンスターからダメージを受けた場合
        event.getDamager match {
          case monster: Monster =>
            // 対モンスターメッセージ
            MebiusListener.getPlayerData(player).mebius
              .speak(MebiusListener.getMessage(MebiusMessages.onDamageWarnEnemy, Objects.requireNonNull(MebiusListener.getNickname(player)), monster.getName))
          case _ =>
        }
      case _ =>
    }
  }

  // 壊れたとき
  @EventHandler def onBreak(event: PlayerItemBreakEvent): Unit = {
    val messages = MebiusMessages.onMebiusBreak
    val brokenItem = event.getBrokenItem

    // 壊れたアイテムがMEBIUSなら
    if (MebiusListener.isMebius(brokenItem)) {
      val player = event.getPlayer
      MebiusListener
        .getPlayerData(event.getPlayer).mebius
        .speak(MebiusListener.getMessage(messages, Objects.requireNonNull(MebiusListener.getNickname(player)), ""))
      player.sendMessage(s"${MebiusListener.getName(brokenItem)}${RESET}が旅立ちました。")
      // エンドラが叫ぶ
      player.playSound(player.getLocation, Sound.ENTITY_ENDERDRAGON_DEATH, 1.0f, 0.1f)
    }
  }

  // モンスターを倒した時
  @EventHandler def onKill(event: EntityDeathEvent): Unit = {
    val messages = MebiusMessages.onMonsterKill

    // プレイヤーがモンスターを倒した場合以外は除外
    val killedMonster = event.getEntity
    if (killedMonster == null) return

    val killerPlayer = killedMonster.getKiller
    if (killerPlayer == null) return

    if (!MebiusListener.isEquip(killerPlayer)) return

    //もしモンスター名が取れなければ除外
    val killedMonsterName = killedMonster.getName
    if (killedMonsterName == "") return

    val mebiusNickname = MebiusListener.getNickname(killerPlayer)

    Objects.requireNonNull(mebiusNickname)
    getPlayerData(killerPlayer).mebius.speak(MebiusListener.getMessage(messages, mebiusNickname, killedMonsterName))
  }

  // 金床配置時（クリック）
  @EventHandler def onRenameOnAnvil(event: InventoryClickEvent): Unit = {
    // 金床を開いていない場合return
    if (!event.getView.getTopInventory.isInstanceOf[AnvilInventory]) return

    val clickedInventory = event.getClickedInventory
    if (clickedInventory.isInstanceOf[AnvilInventory]) {
      // mebiusを選択中
      val item = event.getCursor
      if (MebiusListener.isMebius(item)) {
        // mebiusを左枠に置いた場合はcancel
        if (event.getView.convertSlot(0) == 0 && event.getRawSlot == 0) {
          event.setCancelled(true)
          event.getWhoClicked.sendMessage(s"${RED}MEBIUSへの命名は$RESET/mebius naming <name>${RED}で行ってください。")
        }
      }
    } else {
      // mebiusをShiftクリックした場合
      if (event.getClick.isShiftClick && MebiusListener.isMebius(event.getCurrentItem)) {
        // 左枠が空いている場合はcancel
        if (event.getView.getTopInventory.getItem(0) == null) {
          event.setCancelled(true)
          event.getWhoClicked.sendMessage(s"${RED}MEBIUSへの命名は$RESET/mebius naming <name>${RED}で行ってください。")
        }
      }
    }
  }

  // 金床配置時（ドラッグ）
  @EventHandler def onDragInAnvil(event: InventoryDragEvent): Unit = {
    // 金床じゃなければreturn
    if (!event.getInventory.isInstanceOf[AnvilInventory]) return

    // mebiusを選択中じゃなければreturn
    if (!MebiusListener.isMebius(event.getOldCursor)) return

    if (event.getRawSlots.contains(0) && event.getView.convertSlot(0) == 0) {
      event.setCancelled(true)
      event.getWhoClicked.sendMessage(s"${RED}MEBIUSへの命名は$RESET/mebius naming <name>${RED}で行ってください。")
    }
  }

  /**
   * ブロックを破壊した時
   * 保護と重力値に問題無く、ブロックタイプがmateriallistに登録されていたらメッセージを送る。
   */
  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  def sendMebiusMessageOn(event: BlockBreakEvent): Unit = {
    if (!MaterialSets.materials.contains(event.getBlock.getType)) return

    val player = event.getPlayer
    if (isEquip(player)) {
      val message = getMessage(MebiusMessages.onBlockBreak, Objects.requireNonNull(getNickname(player)), "")
      getPlayerData(player).mebius.speak(message)
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def tryMebiusLevelUpOn(event: BlockBreakEvent): Unit = {
    val player = event.getPlayer
    val playerInventory = player.getInventory

    val oldHelmet = {
      val helmet = playerInventory.getHelmet
      if (helmet == null || helmet.getType == Material.AIR) return else helmet
    }

    val oldMebiusProperty = ItemStackMebiusCodec.decodeMebiusProperty(oldHelmet).getOrElse(return)
    val newMebiusProperty = MebiusLevellingService.attemptLevelUp(oldMebiusProperty).unsafeRunSync()

    if (newMebiusProperty != oldMebiusProperty) {
      val materialized = ItemStackMebiusCodec.materialize(newMebiusProperty, damageValue = 0)
      val mebiusDisplayName = materialized.getItemMeta.getDisplayName

      // レベルアップ通知
      player.sendMessage(s"${newMebiusProperty.mebiusName}${RESET}がレベルアップしました。")

      // 進化通知
      if (materialized.getType != oldHelmet.getType) {
        player.sendMessage(s"$mebiusDisplayName${RESET}の見た目が進化しました。")
      }

      // エンチャント効果変更通知
      if (newMebiusProperty.level.isMaximum) {
        player.sendMessage {
          s"$RESET${ChatColor.GREEN}おめでとうございます。" +
            s"$mebiusDisplayName$RESET${ChatColor.GREEN}のレベルが最大になりました。"
        }
        player.sendMessage(s"$UNBREAK${RESET}が付与されました。")
      } else {
        val modifiedEnchantment = newMebiusProperty.enchantmentDifferentFrom(oldMebiusProperty).get

        // メッセージを生成
        player.sendMessage {
          val romanSuffix = List(
            "", "", " II", " III", " IV", " V",
            " VI", " VII", " VIII", " IX", " X",
            " XI", " XII", " XIII", " XIV", " XV",
            " XVI", " XVII", " XVIII", " XIX", " XX"
          )

          oldMebiusProperty.enchantmentLevel.get(modifiedEnchantment) match {
            case Some(previousLevel) =>
              s"${ChatColor.GRAY}${modifiedEnchantment.displayName}${romanSuffix(previousLevel)}${RESET}が" +
                s"${ChatColor.GRAY}${modifiedEnchantment.displayName}${romanSuffix(previousLevel + 1)}${RESET}に強化されました。"
            case None =>
              s"${ChatColor.GRAY}${modifiedEnchantment.displayName}${RESET}が付与されました。"
          }
        }
      }

      getPlayerData(player).mebius.speakForce(MebiusMessages.talkOnLevelUp(newMebiusProperty.level.value).mebiusMessage)

      playerInventory.setHelmet(materialized)
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def tryMebiusDropOn(event: BlockBreakEvent): Unit = {
    if (!MaterialSets.materials.contains(event.getBlock.getType)) return

    val player = event.getPlayer

    if (Random.nextInt(averageBlocksToBeBrokenPerMebiusDrop) == 0) {
      discovery(player)
    }
  }
}
