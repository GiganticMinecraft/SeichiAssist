package com.github.unchama.seichiassist.util

import java.text.SimpleDateFormat
import java.util.stream.IntStream
import java.util.{Calendar, Random}

import cats.data
import cats.effect.IO
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.minestack.MineStackObj
import com.github.unchama.seichiassist.{MineStackObjectList, SeichiAssist}
import com.github.unchama.targetedeffect.TargetedEffect
import enumeratum._
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.ChatColor._
import org.bukkit._
import org.bukkit.block.{Block, Skull}
import org.bukkit.entity.{EntityType, Firework, Player}
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.inventory.{ItemFlag, ItemStack, PlayerInventory}

object Util {

  import com.github.unchama.util.syntax._

  import scala.jdk.CollectionConverters._

  private val types = List(FireworkEffect.Type.BALL, FireworkEffect.Type.BALL_LARGE, FireworkEffect.Type.BURST, FireworkEffect.Type.CREEPER, FireworkEffect.Type.STAR)

  def sendPlayerDataNullMessage(player: Player): Unit = {
    player.sendMessage(RED.toString + "初回ログイン時の読み込み中か、読み込みに失敗しています")
    player.sendMessage(RED.toString + "再接続しても改善されない場合はお問い合わせフォームからお知らせ下さい")
  }

  //スキルの発動可否の処理(発動可能ならtrue、発動不可ならfalse)
  def isSkillEnable(player: Player): Boolean = {
    val seichiWorldPrefix = if (SeichiAssist.DEBUG) SeichiAssist.DEBUGWORLDNAME else SeichiAssist.SEICHIWORLDNAME
    val worldNameLowerCase = player.getWorld.getName.toLowerCase()

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
   * プレイヤーが整地ワールドにいるかどうかの判定処理(整地ワールド=true、それ以外=false)
   *
   * @deprecated use ManagedWorld
   */
  @Deprecated()
  def isSeichiWorld(player: Player): Boolean = {
    //デバッグモード時は全ワールドtrue(DEBUGWORLDNAME = worldの場合)
    var worldname = SeichiAssist.SEICHIWORLDNAME
    if (SeichiAssist.DEBUG) {
      worldname = SeichiAssist.DEBUGWORLDNAME
    }
    //整地ワールドではtrue
    player.getWorld.getName.toLowerCase().startsWith(worldname)
  }

  //ガチャ券アイテムスタック型の取得
  def getskull(name: String): ItemStack = {
    new ItemStack(Material.SKULL_ITEM, 1).modify { skull =>
      import skull._
      setDurability(3.toShort)
      setItemMeta {
        ItemMetaFactory.SKULL.getValue.modify { skullMeta =>
          import skullMeta._
          setDisplayName(s"$YELLOW${BOLD}ガチャ券")
          setLore {
            List(
              s"$RESET${GREEN}右クリックで使えます",
              s"$RESET${DARK_GREEN}所有者:$name"
            ).asJava
          }
          setOwner("unchama")
        }
      }
    }
  }

  /**
   * プレイヤーに安全にアイテムを付与します。
   *
   * @param player    付与する対象プレイヤー
   * @param itemStack 付与するアイテム
   * @deprecated use [[grantItemStacksEffect]]
   */
  @deprecated def addItemToPlayerSafely(player: Player, itemStack: ItemStack): Unit = {
    com.github.unchama.seichiassist.unsafe.runIOAsync(
      "アイテムスタックを付与する",
      grantItemStacksEffect(itemStack).run(player)
    )
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

  def sendAdminMessage(str: String): Unit = {
    Bukkit.getOnlinePlayers.forEach { player =>
      if (player.hasPermission("SeichiAssist.admin")) {
        player.sendMessage(str)
      }
    }
  }

  def sendEveryMessage(str: String): Unit = {
    Bukkit.getOnlinePlayers.forEach(_.sendMessage(str))
  }

  def sendEveryMessageWithoutIgnore(str: String): Unit = {
    import cats.implicits._

    Bukkit.getOnlinePlayers.asScala.map { player =>
      for {
        playerSettings <- SeichiAssist.playermap(player.getUniqueId).settings.getBroadcastMutingSettings
        _ <- IO { if (!playerSettings.shouldMuteMessages) player.sendMessage(str) }
      } yield ()
    }.toList.sequence.unsafeRunSync()
  }

  def sendEveryMessageWithoutIgnore(base: BaseComponent): Unit = {
    import cats.implicits._

    // TODO remove duplicates
    Bukkit.getOnlinePlayers.asScala.map { player =>
      for {
        playerSettings <- SeichiAssist.playermap(player.getUniqueId).settings.getBroadcastMutingSettings
        _ <- IO { if (!playerSettings.shouldMuteMessages) player.spigot().sendMessage(base) }
      } yield ()
    }.toList.sequence.unsafeRunSync()
  }

  /**
   * json形式のチャットを送信する際に使用
   */
  def sendEveryMessage(base: BaseComponent): Unit = {
    Bukkit.getOnlinePlayers.asScala.foreach(_.spigot().sendMessage(base))
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

  //ガチャアイテムを含んでいるか調べる
  def containsGachaTicket(player: Player): Boolean = {
    player.getInventory.getStorageContents.foreach { itemStack =>
      val material = itemStack.getType
      if (material == Material.SKULL_ITEM) {
        val skullmeta = itemStack.getItemMeta.asInstanceOf[SkullMeta]
        if (skullmeta.hasOwner) {
          if (skullmeta.getOwner == "unchama") {
            return true
          }
        }
      }
    }

    false
  }

  def isGachaTicket(itemstack: ItemStack): Boolean = {
    if (itemstack.getType != Material.SKULL_ITEM) return false

    val skullMeta = itemstack.getItemMeta.asInstanceOf[SkullMeta]

    // オーナーがunchamaか？
    skullMeta.hasOwner && skullMeta.getOwner == "unchama"
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

  def getForBugskull(name: String): ItemStack = {
    new ItemStack(Material.SKULL_ITEM, 1).modify { itemStack =>
      import itemStack._
      setDurability(3)
      setItemMeta {
        ItemMetaFactory.SKULL.getValue.modify { meta =>
          import meta._
          setDisplayName(s"$YELLOW${BOLD}ガチャ券")
          setLore {
            List(
              s"$RESET${GREEN}右クリックで使えます",
              s"$RESET${DARK_GREEN}所有者：$name",
              s"$RESET${DARK_RED}運営から不具合のお詫びです"
            ).asJava
          }
          setOwner("unchama")
        }
      }
    }
  }

  def getVoteskull(name: String): ItemStack = {
    new ItemStack(Material.SKULL_ITEM, 1).modify { itemStack =>
      import itemStack._
      setDurability(3)
      setItemMeta {
        ItemMetaFactory.SKULL.getValue.modify { meta =>
          import meta._
          setDisplayName(s"$YELLOW${BOLD}ガチャ券")
          setLore {
            List(
              s"$RESET${GREEN}右クリックで使えます",
              s"$RESET${DARK_GREEN}所有者：$name",
              s"$RESET${LIGHT_PURPLE}投票ありがとナス♡"
            ).asJava
          }
          setOwner("unchama")
        }
      }
    }
  }

  def getExchangeskull(name: String): ItemStack = {
    new ItemStack(Material.SKULL_ITEM, 1).modify { itemStack =>
      import itemStack._
      setDurability(3)
      setItemMeta {
        ItemMetaFactory.SKULL.getValue.modify { meta =>
          import meta._
          setDisplayName(s"$YELLOW${BOLD}ガチャ券")
          setLore {
            List(
              s"$RESET${GREEN}右クリックで使えます",
              s"$RESET${DARK_GREEN}所有者：$name",
              s"$RESET${GRAY}ガチャ景品と交換しました。"
            ).asJava
          }
          setOwner("unchama")
        }
      }
    }
  }

  def itemStackContainsOwnerName(itemstack: ItemStack, name: String): Boolean = {
    val meta = itemstack.getItemMeta

    val lore: List[String] =
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
    new ItemStack(material, amount).modify { itemStack =>
      import itemStack._
      setItemMeta {
        getItemMeta.modify { meta =>
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
                  displayName: String, lore: List[String], isHideFlags: Boolean): ItemStack =
    new ItemStack(material, amount, durabity.toShort).modify { itemStack =>
      import itemStack._
      setItemMeta {
        getItemMeta.modify { meta =>
          import meta._
          setDisplayName(displayName)
          setLore(lore.asJava)

          if (isHideFlags) addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        }
      }
    }

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
    val n = TypeConverter.toInt(format.format(date))
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
    worldNameList.foreach { name =>
      val world = Bukkit.getWorld(name)

      if (world == null)
        Bukkit.getLogger.warning(name + "という名前のワールドは存在しません。")
      else
        world.setDifficulty(difficulty)
    }
  }

  /**
   * 指定した名前のマインスタックオブジェクトを返す
   */
  // TODO これはここにあるべきではない
  @Deprecated()
  def findMineStackObjectByName(name: String): Option[MineStackObj] = {
    MineStackObjectList.minestacklist.find(_.mineStackObjName == name)
  }

  def isEnemy(entityType: EntityType): Boolean = {
    entityType match {
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
  }

  def isMineHeadItem(itemstack: ItemStack): Boolean = {
    itemstack.getType == Material.CARROT_STICK &&
      loreIndexOf(itemstack.getItemMeta.getLore.asScala.toList, "頭を狩り取る形をしている...") >= 0
  }

  def getSkullDataFromBlock(block: Block): ItemStack = {
    //ブロックがskullじゃない場合石でも返しとく
    // TODO ????
    if (block.getType != Material.SKULL) {
      return new ItemStack(Material.STONE)
    }

    val skull = block.getState.asInstanceOf[Skull]
    val itemStack = new ItemStack(Material.SKULL_ITEM)

    //SkullTypeがプレイヤー以外の場合，SkullTypeだけ設定して終わり
    if (skull.getSkullType != SkullType.PLAYER) {
      val durability = skull.getSkullType match {
        case SkullType.CREEPER => SkullType.CREEPER.ordinal.toShort
        case SkullType.DRAGON => SkullType.DRAGON.ordinal.toShort
        case SkullType.SKELETON => SkullType.SKELETON.ordinal.toShort
        case SkullType.WITHER => SkullType.WITHER.ordinal.toShort
        case SkullType.ZOMBIE => SkullType.ZOMBIE.ordinal.toShort
        case _ => itemStack.getDurability
      }
      return itemStack.modify(_.setDurability(durability))
    }

    //プレイヤーの頭の場合，ドロップアイテムからItemStackを取得．データ値をPLAYERにして返す
    block.getDrops.asScala.head.modify(_.setDurability(SkullType.PLAYER.ordinal.toShort))
  }

  def isLimitedTitanItem(itemstack: ItemStack): Boolean = {
    itemstack.getType == Material.DIAMOND_AXE &&
      loreIndexOf(itemstack.getItemMeta.getLore.asScala.toList, "特別なタイタンをあなたに♡") >= 0
  }

  /**
   * loreを捜査して、要素の中に`find`が含まれているかを調べる。
   *
   * @param lore 探される対象
   * @param find 探す文字列
   * @return 見つかった場合はその添字、見つからなかった場合は-1
   */
  def loreIndexOf(lore: List[String], find: String): Int = {
    IntStream.range(0, lore.size)
      .filter { i => lore(i).contains(find) }
      .findFirst()
      .orElse(-1)
  }

  /**
   * PlayerDataでチャンク数をゲット・セットするためのenum
   */
  sealed trait DirectionType extends EnumEntry

  /**
   * PlayerDataなどで使用する方角関係のenum
   */
  sealed trait Direction extends EnumEntry

  case object DirectionType extends Enum[DirectionType] {

    val values: IndexedSeq[DirectionType] = findValues

    /**
     * for Java interop
     */
    def ahead: AHEAD.type = AHEAD

    def behind: BEHIND.type = BEHIND

    def right: RIGHT.type = RIGHT

    def left: LEFT.type = LEFT

    case object AHEAD extends DirectionType

    case object BEHIND extends DirectionType

    case object RIGHT extends DirectionType

    case object LEFT extends DirectionType
  }

  case object Direction extends Enum[Direction] {

    val values: IndexedSeq[Direction] = findValues

    case object NORTH extends Direction

    case object SOUTH extends Direction

    case object EAST extends Direction

    case object WEST extends Direction
  }
}
