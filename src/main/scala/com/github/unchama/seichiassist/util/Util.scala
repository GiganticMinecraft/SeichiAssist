package com.github.unchama.seichiassist.util

import cats.effect.IO
import com.github.unchama.seichiassist.util.OrderedCollectionOps._
import com.github.unchama.seichiassist.util.enumeration.TimePeriodOfDay
import com.github.unchama.seichiassist.util.typeclass.OrderedCollection._
import com.github.unchama.seichiassist.util.typeclass.Sendable
import com.github.unchama.seichiassist.{ManagedWorld, SeichiAssist}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffect}
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.refineV
import org.bukkit.ChatColor._
import org.bukkit._
import org.bukkit.block.{Block, Skull}
import org.bukkit.entity.{EntityType, Firework, Player}
import org.bukkit.inventory.ItemStack

import java.text.SimpleDateFormat
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
    // * 多段implicitは探索の対象にならない
    effect.`with`(types.pipe(new ForList(_)).sample)

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
    (0 until length).map(_ => Color.fromBGR(rand.nextInt(1 << 24))).toArray
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

  def getTimePeriod(cal: Calendar): TimePeriodOfDay = TimePeriodOfDay(refineV[Positive](cal.get(Calendar.HOUR_OF_DAY)).toOption.get)

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
}
