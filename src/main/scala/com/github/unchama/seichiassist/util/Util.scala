package com.github.unchama.seichiassist.util

import cats.data
import cats.effect.IO
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.util.typeclass.Sendable
import com.github.unchama.seichiassist.{ManagedWorld, SeichiAssist}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffect}
import org.bukkit.ChatColor._
import org.bukkit._
import org.bukkit.block.{Block, Skull}
import org.bukkit.entity.{EntityType, Firework, Player}
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.inventory.{ItemStack, PlayerInventory}

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.{Calendar, Random}

object Util {

  import scala.jdk.CollectionConverters._
  import scala.util.chaining._

  private val types = List(
    FireworkEffect.Type.BALL,
    FireworkEffect.Type.BALL_LARGE,
    FireworkEffect.Type.BURST,
    FireworkEffect.Type.CREEPER,
    FireworkEffect.Type.STAR
  )

  def playerDataErrorEffect: TargetedEffect[Player] = SequentialEffect(
    MessageEffect(s"${RED}初回ログイン時の読み込み中か、読み込みに失敗しています"),
    MessageEffect(s"${RED}再接続しても改善されない場合はお問い合わせフォームまたは整地鯖公式Discordサーバーからお知らせ下さい")
  )

  // TODO: ManagedWorld
  def seichiSkillsAllowedIn(world: World): Boolean = {
    val seichiWorldPrefix = if (SeichiAssist.DEBUG) SeichiAssist.DEBUGWORLDNAME else SeichiAssist.SEICHIWORLDNAME
    val worldNameLowerCase = world.getName.toLowerCase()

    worldNameLowerCase match {
      case "world_sw_zero" => false // 整地ワールドzeroではスキル発動不可
      case "world" |
           "world_2" |
           "world_nether" |
           "world_the_end" |
           "world_TT" |
           "world_nether_TT" |
           "world_the_end_TT" => true
      case _ => worldNameLowerCase.startsWith(seichiWorldPrefix)
    }
  }

  def sendEveryMessageWithoutIgnore[T : Sendable](message: T): Unit = {
    Bukkit.getOnlinePlayers.forEach(implicitly[Sendable[T]].sendMessage(_, message))
  }

  def sendEveryMessage[T : Sendable](message: T): Unit = {
    import cats.implicits._
    Bukkit.getOnlinePlayers.asScala.map { player =>
      for {
        playerSettings <- SeichiAssist.playermap(player.getUniqueId).settings.getBroadcastMutingSettings
        _ <- IO { if (!playerSettings.shouldMuteMessages) implicitly[Sendable[T]].sendMessage(player, message) }
      } yield ()
    }.toList.sequence.unsafeRunSync()
  }

  def getEnchantName(vaname: String, enchlevel: Int): String = {
    val levelLessEnchantmentMapping = Map(
      "WATER_WORKER" -> "水中採掘",
      "SILK_TOUCH" -> "シルクタッチ",
      "ARROW_FIRE" -> "フレイム",
      "ARROW_INFINITE" -> "無限",
      "MENDING" -> "修繕"
    )
    val leveledEnchantmentMapping = Map(
      "PROTECTION_ENVIRONMENTAL" -> "ダメージ軽減",
      "PROTECTION_FIRE" -> "火炎耐性",
      "PROTECTION_FALL" -> "落下耐性",
      "PROTECTION_EXPLOSIONS" -> "爆発耐性",
      "PROTECTION_PROJECTILE" -> "飛び道具耐性",
      "OXYGEN" -> "水中呼吸",
      "THORNS" -> "棘の鎧",
      "DEPTH_STRIDER" -> "水中歩行",
      "FROST_WALKER" -> "氷渡り",
      "DAMAGE_ALL" -> "ダメージ増加",
      "DAMAGE_UNDEAD" -> "アンデッド特効",
      "DAMAGE_ARTHROPODS" -> "虫特効",
      "KNOCKBACK" -> "ノックバック",
      "FIRE_ASPECT" -> "火属性",
      "LOOT_BONUS_MOBS" -> "ドロップ増加",
      "DIG_SPEED" -> "効率強化",
      "DURABILITY" -> "耐久力",
      "LOOT_BONUS_BLOCKS" -> "幸運",
      "ARROW_DAMAGE" -> "射撃ダメージ増加",
      "ARROW_KNOCKBACK" -> "パンチ",
      "LUCK" -> "宝釣り",
      "LURE" -> "入れ食い"
    )
    val enchantmentLevelRepresentation = getEnchantLevelRome(enchlevel)

    levelLessEnchantmentMapping.get(vaname).orElse(
      leveledEnchantmentMapping.get(vaname)
        .map(localizedName => s"$localizedName $enchantmentLevelRepresentation")
    ).getOrElse(vaname)
  }

  private def getEnchantLevelRome(enchantlevel: Int): String = {
    enchantlevel match {
      case 1 => "Ⅰ"
      case 2 => "Ⅱ"
      case 3 => "Ⅲ"
      case 4 => "Ⅳ"
      case 5 => "Ⅴ"
      case 6 => "Ⅵ"
      case 7 => "Ⅶ"
      case 8 => "Ⅷ"
      case 9 => "Ⅸ"
      case 10 => "Ⅹ"
      case _ => enchantlevel.toString
    }

  }

  def getDescFormat(list: List[String]): String = s" ${list.mkString("", "\n", "\n")}"

  def sendEverySound(kind: Sound, volume: Float, pitch: Float): Unit = {
    Bukkit.getOnlinePlayers.forEach(player =>
      player.playSound(player.getLocation, kind, volume, pitch)
    )
  }

  def sendEverySoundWithoutIgnore(kind: Sound, volume: Float, pitch: Float): Unit = {
    import cats.implicits._

    Bukkit.getOnlinePlayers.asScala.toList.map { player =>
      for {
        settings <- SeichiAssist.playermap(player.getUniqueId).settings.getBroadcastMutingSettings
        _ <- IO {
          if (!settings.shouldMuteSounds) player.playSound(player.getLocation, kind, volume, pitch)
        }
      } yield ()
    }.sequence.unsafeRunSync()
  }

  def getName(name: String): String = {
    //小文字にしてるだけだよ
    name.toLowerCase()
  }

  //指定された場所に花火を打ち上げる関数
  def launchFireWorks(loc: Location): Unit = {
    // 花火を作る
    val firework = loc.getWorld.spawn(loc, classOf[Firework])

    // 花火の設定情報オブジェクトを取り出す
    val meta = firework.getFireworkMeta
    val effect = FireworkEffect.builder()
    val rand = new Random()

    // 形状をランダムに決める
    effect.`with`(types(rand.nextInt(types.size)))

    // 基本の色を単色～5色以内でランダムに決める
    effect.withColor(getRandomColors(1 + rand.nextInt(5)): _*)

    // 余韻の色を単色～3色以内でランダムに決める
    effect.withFade(getRandomColors(1 + rand.nextInt(3)): _*)

    // 爆発後に点滅するかをランダムに決める
    effect.flicker(rand.nextBoolean())

    // 爆発後に尾を引くかをランダムに決める
    effect.trail(rand.nextBoolean())

    // 打ち上げ高さを1以上4以内でランダムに決める
    meta.setPower(1 + rand.nextInt(4))

    // 花火の設定情報を花火に設定
    meta.addEffect(effect.build())

    firework.setFireworkMeta(meta)
  }

  //カラーをランダムで決める
  def getRandomColors(length: Int): Array[Color] = {
    // 配列を作る
    val rand = new Random()
    // 配列の要素を順に処理していく
    // 24ビットカラーの範囲でランダムな色を決める

    // 配列を返す
    (0 until length).map { _ => Color.fromBGR(rand.nextInt(1 << 24)) }.toArray
  }

  def isGachaTicket(itemStack: ItemStack): Boolean = {
    val containsRightClickMessage: String => Boolean = _.contains(s"${GREEN}右クリックで使えます")

    if (itemStack.getType != Material.SKULL_ITEM) return false

    val skullMeta = itemStack.getItemMeta.asInstanceOf[SkullMeta]

    if (!(skullMeta.hasOwner && skullMeta.getOwner == "unchama")) return false

    skullMeta.hasLore && skullMeta.getLore.asScala.exists(containsRightClickMessage)
  }

  // TODO: Codec
  def itemStackContainsOwnerName(itemstack: ItemStack, name: String): Boolean = {
    val meta = itemstack.getItemMeta

    val lore =
      if (meta.hasLore)
        meta.getLore.asScala.toList
      else
        Nil

    lore.exists(line =>
      line.contains("所有者：") && line.drop(line.indexOf("所有者：") + 4).toLowerCase == name.toLowerCase()
    )
  }

  import com.github.unchama.seichiassist.util.enumeration.Direction
  def getPlayerDirection(player: Player): Direction = {
    var rotation = ((player.getLocation.getYaw + 180) % 360).toDouble

    if (rotation < 0) rotation += 360.0

    //0,360:south 90:west 180:north 270:east
    if (0.0 <= rotation && rotation < 45.0) Direction.NORTH
    else if (45.0 <= rotation && rotation < 135.0) Direction.EAST
    else if (135.0 <= rotation && rotation < 225.0) Direction.SOUTH
    else if (225.0 <= rotation && rotation < 315.0) Direction.WEST
    else Direction.NORTH
  }

  @deprecated
  def showHour(cal: Calendar): String = {
    new SimpleDateFormat("HH:mm").format(cal.getTime)
  }

  def getTimeZone(cal: Calendar): String = {
    val n = cal.get(Calendar.HOUR_OF_DAY)
    n match {
      case _ if 4 <= n && n < 10 => "morning"
      case _ if 10 <= n && n < 18 => "day"
      case _ => "night"
    }
  }

  def isVotingFairyPeriod(start: Calendar, end: Calendar): Boolean = {
    val now = Calendar.getInstance()
    now.after(start) && now.before(end)
  }

  def setDifficulty(worlds: IndexedSeq[ManagedWorld], difficulty: Difficulty): Unit = {
    worlds.foreach(_.alphabetName.pipe(Bukkit.getWorld).setDifficulty(difficulty))
  }

  def isEnemy(entityType: EntityType): Boolean = entityType match {
    case
      //通常世界MOB
      EntityType.CAVE_SPIDER |
      EntityType.CREEPER |
      EntityType.GUARDIAN |
      EntityType.SILVERFISH |
      EntityType.SKELETON |
      EntityType.SLIME |
      EntityType.SPIDER |
      EntityType.WITCH |
      EntityType.ZOMBIE |
      //ネザーMOB
      EntityType.BLAZE |
      EntityType.GHAST |
      EntityType.MAGMA_CUBE |
      EntityType.PIG_ZOMBIE |
      //エンドMOB
      EntityType.ENDERMAN |
      EntityType.ENDERMITE |
      EntityType.SHULKER => true
    //敵MOB以外(エンドラ,ウィザーは除外)
    case _ => false
  }

  def isMineHeadItem(itemstack: ItemStack): Boolean = {
    itemstack.getType == Material.CARROT_STICK &&
      loreIndexOf(itemstack.getItemMeta.getLore.asScala.toList, "頭を狩り取る形をしている...").nonEmpty
  }

  def getSkullDataFromBlock(block: Block): Option[ItemStack] = {
    if (block.getType != Material.SKULL) return None

    val skull = block.getState.asInstanceOf[Skull]
    val itemStack = new ItemStack(Material.SKULL_ITEM)

    //SkullTypeがプレイヤー以外の場合，SkullTypeだけ設定して終わり
    val stype = skull.getSkullType
    if (stype ne SkullType.PLAYER) {
      val durability = stype match {
        case SkullType.CREEPER
             | SkullType.DRAGON
             | SkullType.SKELETON
             | SkullType.WITHER
             | SkullType.ZOMBIE => stype.ordinal().toShort
        case _ => itemStack.getDurability
      }
      return Some(itemStack.tap(_.setDurability(durability)))
    }
    //プレイヤーの頭の場合，ドロップアイテムからItemStackを取得．データ値をPLAYERにして返す
    Some(block.getDrops.asScala.head.tap(_.setDurability(SkullType.PLAYER.ordinal.toShort)))
  }

  def isLimitedTitanItem(itemstack: ItemStack): Boolean = {
    itemstack.getType == Material.DIAMOND_AXE &&
      isContainedInLore(itemstack, "特別なタイタンをあなたに♡")
  }

  /**
   * 指定された`String`が指定された[[ItemStack]]のloreに含まれているかどうか
   *
   * @param itemStack 確認する`ItemStack`
   * @param sentence  探す文字列
   * @return 含まれていれば`true`、含まれていなければ`false`。ただし、`ItemStack`に`ItemMeta`と`Lore`のいずれかがなければfalse
   */
  def isContainedInLore(itemStack: ItemStack, sentence: String): Boolean =
    if (!itemStack.hasItemMeta || !itemStack.getItemMeta.hasLore) false
    else loreIndexOf(itemStack.getItemMeta.getLore.asScala.toList, sentence).nonEmpty

  /**
   * loreを捜査して、要素の中に`find`が含まれているかを調べる。
   *
   * @param lore 探される対象
   * @param find 探す文字列
   * @return 見つかった場合は`Some(index)`、見つからなかった場合は[[None]]
   */
  def loreIndexOf(lore: List[String], find: String): Option[Int] = {
    lore.zipWithIndex
      .find(_._1.contains(find))
      .map(_._2)
  }
}
