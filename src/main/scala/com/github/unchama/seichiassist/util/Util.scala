package com.github.unchama.seichiassist.util

import cats.data
import cats.effect.IO
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.util.enumeration.Direction
import com.github.unchama.seichiassist.util.typeclass.Sendable
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.ChatColor._
import org.bukkit._
import org.bukkit.block.{Block, Skull}
import org.bukkit.entity.{EntityType, Firework, Player}
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.inventory.{ItemFlag, ItemStack, PlayerInventory}

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

  def sendPlayerDataNullMessage(player: Player): Unit = {
    player.sendMessage(s"${RED}初回ログイン時の読み込み中か、読み込みに失敗しています")
    player.sendMessage(s"${RED}再接続しても改善されない場合はお問い合わせフォームまたは整地鯖公式Discordサーバーからお知らせ下さい")
  }

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

  /**
   * プレイヤーに複数のアイテムを一度に付与する。
   * インベントリに入り切らなかったアイテムはプレーヤーの立ち位置にドロップされる。
   *
   * @param itemStacks 付与するアイテム
   */
  def grantItemStacksEffect(itemStacks: ItemStack*): TargetedEffect[Player] = data.Kleisli { player =>
    val toGive: Seq[ItemStack] = itemStacks.filter(_.getType != Material.AIR)

    for {
      _ <- IO {
        if (toGive.size != itemStacks.size)
          Bukkit.getLogger.warning("attempt to add Material.AIR to player inventory")
      }
      _ <- PluginExecutionContexts.syncShift.shift
      _ <- IO {
        player.getInventory
          .addItem(itemStacks: _*)
          .values().asScala
          .filter(_.getType != Material.AIR)
          .foreach(dropItem(player, _))
      }
    } yield ()
  }

  //プレイヤーのインベントリがフルかどうか確認
  def isPlayerInventoryFull(player: Player): Boolean = player.getInventory.firstEmpty() == -1

  //指定されたアイテムを指定されたプレイヤーにドロップする
  def dropItem(player: Player, itemstack: ItemStack): Unit = {
    player.getWorld.dropItemNaturally(player.getLocation, itemstack)
  }

  //指定されたアイテムを指定されたプレイヤーインベントリに追加する
  def addItem(player: Player, itemstack: ItemStack): Unit = {
    player.getInventory.addItem(itemstack)
  }

  def sendEveryMessage[T : Sendable](message: T): Unit = {
    Bukkit.getOnlinePlayers.forEach(implicitly[Sendable[T]].sendMessage(_, message))
  }

  def sendEveryMessageWithoutIgnore[T : Sendable](message: T): Unit = {
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

  def removeItemfromPlayerInventory(inventory: PlayerInventory,
                                    itemstack: ItemStack, count: Int): Boolean = {
    //持っているアイテムを減らす処理
    if (itemstack.getAmount == count) {
      // アイテムをcount個使うので、プレイヤーの手を素手にする
      inventory.setItemInMainHand(new ItemStack(Material.AIR))
    } else if (itemstack.getAmount > count) {
      // プレイヤーが持っているアイテムをcount個減らす
      itemstack.setAmount(itemstack.getAmount - count)
    } else
      return itemstack.getAmount >= count
    true
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

  /**
   * GUIメニューアイコン作成用
   *
   * @deprecated Use [[com.github.unchama.itemstackbuilder.IconItemStackBuilder]]
   * @author karayuu
   * @param material    メニューアイコンMaterial
   * @param amount      メニューアイコンのアイテム個数
   * @param displayName メニューアイコンのDisplayName
   * @param lore        メニューアイコンのLore
   * @param isHideFlags 攻撃値・ダメージ値を隠すかどうか(true: 隠す / false: 隠さない)
   * @return ItemStack型のメニューアイコン
   */
  def getMenuIcon(material: Material, amount: Int,
                  displayName: String, lore: List[String], isHideFlags: Boolean): ItemStack = {
    new ItemStack(material, amount).tap { itemStack =>
      import itemStack._
      setItemMeta {
        getItemMeta.tap { meta =>
          import meta._
          setDisplayName(displayName)
          setLore(lore.asJava)
          if (isHideFlags) {
            addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
          }
        }
      }
    }
  }

  /**
   * GUIメニューアイコン作成用
   *
   * @deprecated Use [[com.github.unchama.itemstackbuilder.IconItemStackBuilder]]
   * @author karayuu
   * @param material    メニューアイコンMaterial, not `null`
   * @param amount      メニューアイコンのアイテム個数
   * @param durabity    メニューアイコンのダメージ値
   * @param displayName メニューアイコンのDisplayName, not `null`
   * @param lore        メニューアイコンのLore, not `null`
   * @param isHideFlags 攻撃値・ダメージ値を隠すかどうか(true: 隠す / false: 隠さない)
   * @throws IllegalArgumentException Material,DisplayName, Loreのいずれかが `null` の時
   * @return ItemStack型のメニューアイコン
   */
  def getMenuIcon(material: Material, amount: Int, durabity: Int,
                  displayName: String, lore: List[String], isHideFlags: Boolean): ItemStack = {
    new ItemStack(material, amount, durabity.toShort).tap { itemStack =>
      import itemStack._
      setItemMeta {
        getItemMeta.tap { meta =>
          import meta._
          setDisplayName(displayName)
          setLore(lore.asJava)

          if (isHideFlags) addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        }
      }
    }
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

  def showTime(cal: Calendar): String = {
    val date = cal.getTime
    val format = new SimpleDateFormat("yyyy/MM/dd HH:mm")
    format.format(date)
  }

  def showHour(cal: Calendar): String = {
    val date = cal.getTime
    val format = new SimpleDateFormat("HH:mm")
    format.format(date)
  }

  def getTimeZone(cal: Calendar): String = {
    val date = cal.getTime
    val format = new SimpleDateFormat("HH")
    val n = Integer.parseInt(format.format(date))
    if (4 <= n && n < 10)
      "morning"
    else if (10 <= n && n < 18)
      "day"
    else
      "night"
  }

  def isVotingFairyPeriod(start: Calendar, end: Calendar): Boolean = {
    val cur = Calendar.getInstance()
    cur.after(start) && cur.before(end)
  }

  def setDifficulty(worldNameList: List[String], difficulty: Difficulty): Unit = {
    worldNameList.map(name => (name, Option(Bukkit.getWorld(name)))).foreach { worldOpt =>
      worldOpt._2 match {
        case Some(value) => value.setDifficulty(difficulty)
        case None => Bukkit.getLogger.warning(s"${worldOpt._1}という名前のワールドは存在しません。")
      }
    }
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
      loreIndexOfOpt(itemstack.getItemMeta.getLore.asScala.toList, "頭を狩り取る形をしている...").nonEmpty
  }

  def getSkullDataFromBlock(block: Block): Option[ItemStack] = {
    if (block.getType != Material.SKULL) return None

    val skull = block.getState.asInstanceOf[Skull]
    val itemStack = new ItemStack(Material.SKULL_ITEM)

    //SkullTypeがプレイヤー以外の場合，SkullTypeだけ設定して終わり
    if (skull.getSkullType != SkullType.PLAYER) {
      val durability = skull.getSkullType match {
        case st @ SkullType.CREEPER
             | SkullType.DRAGON
             | SkullType.SKELETON
             | SkullType.WITHER
             | SkullType.ZOMBIE => st.ordinal().toShort
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
    else loreIndexOfOpt(itemStack.getItemMeta.getLore.asScala.toList, sentence).nonEmpty

  /**
   * loreを捜査して、要素の中に`find`が含まれているかを調べる。
   *
   * @param lore 探される対象
   * @param find 探す文字列
   * @return 見つかった場合はその添字、見つからなかった場合は-1
   */
  @deprecated def loreIndexOf(lore: List[String], find: String): Int = {
    loreIndexOfOpt(lore, find)
      .getOrElse(-1)
  }

  def loreIndexOfOpt(lore: List[String], find: String): Option[Int] = {
    lore.zipWithIndex
      .find(_._1.contains(find))
      .map(_._2)
  }
}
