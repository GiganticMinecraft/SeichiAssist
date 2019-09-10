package com.github.unchama.seichiassist.util

import com.github.unchama.seichiassist.MineStackObjectList
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.minestack.MineStackObj
import com.github.unchama.util.collection.ImmutableListFactory
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.Skull
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.SkullMeta
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.IntStream

object Util {

  private val types = arrayOf(FireworkEffect.Type.BALL, FireworkEffect.Type.BALL_LARGE, FireworkEffect.Type.BURST, FireworkEffect.Type.CREEPER, FireworkEffect.Type.STAR)

  fun sendPlayerDataNullMessage(player: Player) {
    player.sendMessage(ChatColor.RED.toString() + "初回ログイン時の読み込み中か、読み込みに失敗しています")
    player.sendMessage(ChatColor.RED.toString() + "再接続しても改善されない場合はお問い合わせフォームからお知らせ下さい")
  }

  //スキルの発動可否の処理(発動可能ならtrue、発動不可ならfalse)
  fun isSkillEnable(player: Player): Boolean {
    val seichiWorldPrefix = if (SeichiAssist.DEBUG) SeichiAssist.DEBUGWORLDNAME else SeichiAssist.SEICHIWORLDNAME

    // 整地ワールドzeroではスキル発動不可
    return if (player.world.name.equals("world_sw_zero", ignoreCase = true)) {
      false
    } else player.world.name.toLowerCase().startsWith(seichiWorldPrefix)
        || player.world.name.equals("world", ignoreCase = true)
        || player.world.name.equals("world_2", ignoreCase = true)
        || player.world.name.equals("world_nether", ignoreCase = true)
        || player.world.name.equals("world_the_end", ignoreCase = true)
        || player.world.name.equals("world_TT", ignoreCase = true)
        || player.world.name.equals("world_nether_TT", ignoreCase = true)
        || player.world.name.equals("world_the_end_TT", ignoreCase = true)
  }

  /**
   * プレイヤーが整地ワールドにいるかどうかの判定処理(整地ワールド=true、それ以外=false)
   *
   */
  @Deprecated("use ManagedWorld")
  fun isSeichiWorld(player: Player): Boolean {
    //デバッグモード時は全ワールドtrue(DEBUGWORLDNAME = worldの場合)
    var worldname = SeichiAssist.SEICHIWORLDNAME
    if (SeichiAssist.DEBUG) {
      worldname = SeichiAssist.DEBUGWORLDNAME
    }
    //整地ワールドではtrue
    return player.world.name.toLowerCase().startsWith(worldname)

    //それ以外のワールドの場合
  }

  //ガチャ券アイテムスタック型の取得
  fun getskull(name: String): ItemStack {
    val skull: ItemStack
    val skullmeta: SkullMeta
    skull = ItemStack(Material.SKULL_ITEM, 1)
    skullmeta = ItemMetaFactory.SKULL.value
    skull.durability = 3.toShort()
    skullmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.BOLD + "ガチャ券"
    val lore = ImmutableListFactory.of(ChatColor.RESET.toString() + "" + ChatColor.GREEN + "右クリックで使えます", ChatColor.RESET.toString() + "" + ChatColor.DARK_GREEN + "所有者:" + name)
    skullmeta.lore = lore
    skullmeta.owner = "unchama"
    skull.itemMeta = skullmeta
    return skull
  }

  //プレイヤーのインベントリがフルかどうか確認
  fun isPlayerInventoryFull(player: Player): Boolean {
    return player.inventory.firstEmpty() == -1
  }

  //指定されたアイテムを指定されたプレイヤーにドロップする
  fun dropItem(player: Player, itemstack: ItemStack) {
    player.world.dropItemNaturally(player.location, itemstack)
  }

  //指定されたアイテムを指定されたプレイヤーインベントリに追加する
  fun addItem(player: Player, itemstack: ItemStack) {
    player.inventory.addItem(itemstack)
  }

  /**
   * プレイヤーに安全にアイテムを付与します。
   *
   * @param player 付与する対象プレイヤー
   * @param itemStack 付与するアイテム
   */
  fun addItemToPlayerSafely(player: Player, itemStack: ItemStack) {
    if (isPlayerInventoryFull(player)) {
      dropItem(player, itemStack)
    } else {
      addItem(player, itemStack)
    }
  }

  fun sendAdminMessage(str: String) {
    for (player in Bukkit.getOnlinePlayers()) {
      if (player.hasPermission("SeichiAssist.admin")) {
        player.sendMessage(str)
      }
    }
  }


  fun sendEveryMessage(str: String) {
    for (player in Bukkit.getOnlinePlayers()) {
      player.sendMessage(str)
    }
  }

  fun sendEveryMessageWithoutIgnore(str: String) {
    runBlocking {
      for (player in Bukkit.getOnlinePlayers()) {
        if (SeichiAssist.playermap[player.uniqueId]!!.settings.getBroadcastMutingSettings().shouldMuteMessages()) {
          player.sendMessage(str)
        }
      }
    }
  }

  fun sendEveryMessageWithoutIgnore(base: BaseComponent) {
    runBlocking {
      for (player in Bukkit.getOnlinePlayers()) {
        if (SeichiAssist.playermap[player.uniqueId]!!.settings.getBroadcastMutingSettings().shouldMuteMessages()) {
          player.spigot().sendMessage(base)
        }
      }
    }
  }

  /**
   * json形式のチャットを送信する際に使用
   */
  fun sendEveryMessage(base: BaseComponent) {
    for (player in Bukkit.getOnlinePlayers()) {
      player.spigot().sendMessage(base)
    }
  }

  fun getEnchantName(vaname: String, enchlevel: Int): String {
    when (vaname) {
      "PROTECTION_ENVIRONMENTAL" -> return "ダメージ軽減" + " " + getEnchantLevelRome(enchlevel)

      "PROTECTION_FIRE" -> return "火炎耐性" + " " + getEnchantLevelRome(enchlevel)

      "PROTECTION_FALL" -> return "落下耐性" + " " + getEnchantLevelRome(enchlevel)

      "PROTECTION_EXPLOSIONS" -> return "爆発耐性" + " " + getEnchantLevelRome(enchlevel)

      "PROTECTION_PROJECTILE" -> return "飛び道具耐性" + " " + getEnchantLevelRome(enchlevel)

      "OXYGEN" -> return "水中呼吸" + " " + getEnchantLevelRome(enchlevel)

      "WATER_WORKER" -> return "水中採掘"

      "THORNS" -> return "棘の鎧" + " " + getEnchantLevelRome(enchlevel)

      "DEPTH_STRIDER" -> return "水中歩行" + " " + getEnchantLevelRome(enchlevel)

      "FROST_WALKER" -> return "氷渡り" + " " + getEnchantLevelRome(enchlevel)

      "DAMAGE_ALL" -> return "ダメージ増加" + " " + getEnchantLevelRome(enchlevel)

      "DAMAGE_UNDEAD" -> return "アンデッド特効" + " " + getEnchantLevelRome(enchlevel)

      "DAMAGE_ARTHROPODS" -> return "虫特効" + " " + getEnchantLevelRome(enchlevel)

      "KNOCKBACK" -> return "ノックバック" + " " + getEnchantLevelRome(enchlevel)

      "FIRE_ASPECT" -> return "火属性" + " " + getEnchantLevelRome(enchlevel)

      "LOOT_BONUS_MOBS" -> return "ドロップ増加" + " " + getEnchantLevelRome(enchlevel)

      "DIG_SPEED" -> return "効率強化" + " " + getEnchantLevelRome(enchlevel)

      "SILK_TOUCH" -> return "シルクタッチ"

      "DURABILITY" -> return "耐久力" + " " + getEnchantLevelRome(enchlevel)

      "LOOT_BONUS_BLOCKS" -> return "幸運" + " " + getEnchantLevelRome(enchlevel)

      "ARROW_DAMAGE" -> return "射撃ダメージ増加" + " " + getEnchantLevelRome(enchlevel)

      "ARROW_KNOCKBACK" -> return "パンチ" + " " + getEnchantLevelRome(enchlevel)

      "ARROW_FIRE" -> return "フレイム"

      "ARROW_INFINITE" -> return "無限"

      "LUCK" -> return "宝釣り" + " " + getEnchantLevelRome(enchlevel)

      "LURE" -> return "入れ食い" + " " + getEnchantLevelRome(enchlevel)

      "MENDING" -> return "修繕"

      else -> return vaname
    }
  }

  private fun getEnchantLevelRome(enchantlevel: Int): String {
    when (enchantlevel) {
      1 -> return "Ⅰ"

      2 -> return "Ⅱ"

      3 -> return "Ⅲ"

      4 -> return "Ⅳ"

      5 -> return "Ⅴ"

      6 -> return "Ⅵ"

      7 -> return "Ⅶ"

      8 -> return "Ⅷ"

      9 -> return "Ⅸ"

      10 -> return "Ⅹ"

      else -> return enchantlevel.toString()
    }

  }

  fun getDescFormat(list: List<String>): String {
    return " " + list.joinToString("\n") + "\n"
  }

  fun sendEverySound(kind: Sound, a: Float, b: Float) {
    for (player in Bukkit.getOnlinePlayers()) {
      player.playSound(player.location, kind, a, b)
    }
  }

  fun sendEverySoundWithoutIgnore(kind: Sound, a: Float, b: Float) {
    runBlocking {
      for (player in Bukkit.getOnlinePlayers()) {
        if (SeichiAssist.playermap[player.uniqueId]!!.settings.getBroadcastMutingSettings().shouldMuteMessages()) {
          player.playSound(player.location, kind, a, b)
        }
      }
    }
  }

  fun getName(name: String): String {
    //小文字にしてるだけだよ
    return name.toLowerCase()
  }

  //指定された場所に花火を打ち上げる関数
  fun launchFireWorks(loc: Location) {
    // 花火を作る
    val firework = loc.world.spawn(loc, Firework::class.java)

    // 花火の設定情報オブジェクトを取り出す
    val meta = firework.fireworkMeta
    val effect = FireworkEffect.builder()
    val rand = Random()

    // 形状をランダムに決める
    effect.with(types[rand.nextInt(types.size)])

    // 基本の色を単色～5色以内でランダムに決める
    effect.withColor(*getRandomColors(1 + rand.nextInt(5)))

    // 余韻の色を単色～3色以内でランダムに決める
    effect.withFade(*getRandomColors(1 + rand.nextInt(3)))

    // 爆発後に点滅するかをランダムに決める
    effect.flicker(rand.nextBoolean())

    // 爆発後に尾を引くかをランダムに決める
    effect.trail(rand.nextBoolean())

    // 打ち上げ高さを1以上4以内でランダムに決める
    meta.power = 1 + rand.nextInt(4)

    // 花火の設定情報を花火に設定
    meta.addEffect(effect.build())
    firework.fireworkMeta = meta

  }

  //カラーをランダムで決める
  fun getRandomColors(length: Int): Array<Color> {
    // 配列を作る
    val rand = Random()
    // 配列の要素を順に処理していく
    // 24ビットカラーの範囲でランダムな色を決める

    // 配列を返す
    return (0 until length).map { Color.fromBGR(rand.nextInt(1 shl 24)) }.toTypedArray()
  }

  //ガチャアイテムを含んでいるか調べる
  fun containsGachaTicket(player: Player): Boolean {
    val inventory = player.inventory.storageContents
    var material: Material
    var skullmeta: SkullMeta
    for (itemStack in inventory) {
      material = itemStack.type
      if (material == Material.SKULL_ITEM) {
        skullmeta = itemStack.itemMeta as SkullMeta
        if (skullmeta.hasOwner()) {
          if (skullmeta.owner == "unchama") {
            return true
          }
        }
      }
    }
    return false
  }

  /**
   * loreを捜査して、要素の中に`find`が含まれているかを調べる。
   * @param lore 探される対象
   * @param find 探す文字列
   * @return 見つかった場合はその添字、見つからなかった場合は-1
   */
  fun loreIndexOf(lore: List<String>, find: String): Int {
    return IntStream.range(0, lore.size)
        .filter { i -> lore[i].contains(find) }
        .findFirst()
        .orElse(-1)
  }

  fun isGachaTicket(itemstack: ItemStack): Boolean {
    if (itemstack.type != Material.SKULL_ITEM) {
      return false
    }
    val skullmeta = itemstack.itemMeta as SkullMeta

    //ownerがいない場合処理終了
    return if (!skullmeta.hasOwner()) {
      false
    } else skullmeta.owner == "unchama"
    // オーナーがunchamaか？
  }

  fun removeItemfromPlayerInventory(inventory: PlayerInventory,
                                    itemstack: ItemStack, count: Int): Boolean {
    //持っているアイテムを減らす処理
    if (itemstack.amount == count) {
      // アイテムをcount個使うので、プレイヤーの手を素手にする
      inventory.itemInMainHand = ItemStack(Material.AIR)
    } else if (itemstack.amount > count) {
      // プレイヤーが持っているアイテムをcount個減らす
      itemstack.amount = itemstack.amount - count
    } else
      return itemstack.amount >= count
    return true
  }

  fun getForBugskull(name: String): ItemStack {
    val skull: ItemStack
    val skullmeta: SkullMeta
    skull = ItemStack(Material.SKULL_ITEM, 1)
    skullmeta = ItemMetaFactory.SKULL.value
    skull.durability = 3.toShort()
    skullmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.BOLD + "ガチャ券"
    val lore = ImmutableListFactory.of(ChatColor.RESET.toString() + "" + ChatColor.GREEN + "右クリックで使えます", ChatColor.RESET.toString() + "" + ChatColor.DARK_GREEN + "所有者：" + name, ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "運営から不具合のお詫びです")
    skullmeta.lore = lore
    skullmeta.owner = "unchama"
    skull.itemMeta = skullmeta
    return skull
  }

  fun getVoteskull(name: String): ItemStack {
    val skull: ItemStack
    val skullmeta: SkullMeta
    skull = ItemStack(Material.SKULL_ITEM, 1)
    skullmeta = ItemMetaFactory.SKULL.value
    skull.durability = 3.toShort()
    skullmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.BOLD + "ガチャ券"
    val lore = ImmutableListFactory.of(ChatColor.RESET.toString() + "" + ChatColor.GREEN + "右クリックで使えます", ChatColor.RESET.toString() + "" + ChatColor.DARK_GREEN + "所有者：" + name, ChatColor.RESET.toString() + "" + ChatColor.LIGHT_PURPLE + "投票ありがとナス♡")
    skullmeta.lore = lore
    skullmeta.owner = "unchama"
    skull.itemMeta = skullmeta
    return skull
  }

  fun getExchangeskull(name: String): ItemStack {
    val skull: ItemStack
    val skullmeta: SkullMeta
    skull = ItemStack(Material.SKULL_ITEM, 1)
    skullmeta = ItemMetaFactory.SKULL.value
    skull.durability = 3.toShort()
    skullmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.BOLD + "ガチャ券"
    val lore = ImmutableListFactory.of(ChatColor.RESET.toString() + "" + ChatColor.GREEN + "右クリックで使えます", ChatColor.RESET.toString() + "" + ChatColor.DARK_GREEN + "所有者：" + name, ChatColor.RESET.toString() + "" + ChatColor.GRAY + "ガチャ景品と交換しました。")
    skullmeta.lore = lore
    skullmeta.owner = "unchama"
    skull.itemMeta = skullmeta
    return skull
  }

  fun itemStackContainsOwnerName(itemstack: ItemStack, name: String): Boolean {
    val meta = itemstack.itemMeta

    val lore: List<String> = if (meta.hasLore()) {
      meta.lore
    } else {
      ArrayList()
    }

    for (s in lore) {
      if (s.contains("所有者：")) { //"所有者:がある"
        var idx = s.lastIndexOf("所有者：")
        idx += 4 //「所有者：」の右端(名前の左端)までidxを移動
        val temp = s.substring(idx)
        if (temp.equals(name, ignoreCase = true)) {
          return true
        }
      }
    }
    return false
  }

  /**
   * GUIメニューアイコン作成用
   * @author karayuu
   *
   * @param material メニューアイコンMaterial, not `null`
   * @param amount メニューアイコンのアイテム個数
   * @param displayName メニューアイコンのDisplayName, not `null`
   * @param lore メニューアイコンのLore, not `null`
   * @param isHideFlags 攻撃値・ダメージ値を隠すかどうか(true: 隠す / false: 隠さない)
   * @throws IllegalArgumentException Material,DisplayName, Loreのいずれかが `null` の時
   * @return ItemStack型のメニューアイコン
   */
  fun getMenuIcon(material: Material?, amount: Int,
                  displayName: String?, lore: List<String>?, isHideFlags: Boolean): ItemStack {
    if (material == null || displayName == null || lore == null) {
      throw IllegalArgumentException("Material,DisplayName,LoreにNullは指定できません。")
    }
    val menuicon = ItemStack(material, amount)
    val itemMeta = Bukkit.getItemFactory().getItemMeta(material)
    itemMeta.displayName = displayName
    itemMeta.lore = lore
    if (isHideFlags) {
      itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
    }
    menuicon.itemMeta = itemMeta

    return menuicon
  }

  /**
   * GUIメニューアイコン作成用
   * @author karayuu
   *
   * @param material メニューアイコンMaterial, not `null`
   * @param amount メニューアイコンのアイテム個数
   * @param durabity メニューアイコンのダメージ値
   * @param displayName メニューアイコンのDisplayName, not `null`
   * @param lore メニューアイコンのLore, not `null`
   * @param isHideFlags 攻撃値・ダメージ値を隠すかどうか(true: 隠す / false: 隠さない)
   * @throws IllegalArgumentException Material,DisplayName, Loreのいずれかが `null` の時
   * @return ItemStack型のメニューアイコン
   */
  fun getMenuIcon(material: Material?, amount: Int, durabity: Int,
                  displayName: String?, lore: List<String>?, isHideFlags: Boolean): ItemStack {
    if (material == null || displayName == null || lore == null) {
      throw IllegalArgumentException("Material,DisplayName,LoreにNullは指定できません。")
    }
    val menuicon = ItemStack(material, amount, durabity.toShort())
    val itemMeta = Bukkit.getItemFactory().getItemMeta(material)
    itemMeta.displayName = displayName
    itemMeta.lore = lore
    if (isHideFlags) {
      itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
    }
    menuicon.itemMeta = itemMeta

    return menuicon
  }

  /**
   * PlayerDataでチャンク数をゲット・セットするためのenum
   */
  enum class DirectionType {
    AHEAD,
    BEHIND,
    RIGHT,
    LEFT
  }

  /**
   * PlayerDataなどで使用する方角関係のenum
   */
  enum class Direction {
    NORTH,
    SOUTH,
    EAST,
    WEST
  }

  fun getPlayerDirection(player: Player): Direction? {
    var rotation = ((player.location.yaw + 180) % 360).toDouble()

    if (rotation < 0) {
      rotation += 360.0
    }

    //0,360:south 90:west 180:north 270:east
    if (0.0 <= rotation && rotation < 45.0) {
      //前が北(North)
      return Direction.NORTH
    } else if (45.0 <= rotation && rotation < 135.0) {
      //前が東(East)
      return Direction.EAST
    } else if (135.0 <= rotation && rotation < 225.0) {
      //前が南(South)
      return Direction.SOUTH
    } else if (225.0 <= rotation && rotation < 315.0) {
      //前が西(West)
      return Direction.WEST
    } else if (315.0 <= rotation && rotation < 360.0) {
      //前が北(North)
      return Direction.NORTH
    }
    //ここに到達はありえない。
    return null
  }

  fun showTime(cal: Calendar): String {
    val date = cal.time
    val format = SimpleDateFormat("yyyy/MM/dd HH:mm")
    return format.format(date)
  }

  fun showHour(cal: Calendar): String {
    val date = cal.time
    val format = SimpleDateFormat("HH:mm")
    return format.format(date)
  }

  fun getTimeZone(cal: Calendar): String {
    val date = cal.time
    val format = SimpleDateFormat("HH")
    val n = TypeConverter.toInt(format.format(date))
    return if (4 <= n && n < 10)
      "morning"
    else if (10 <= n && n < 18)
      "day"
    else
      "night"
  }

  fun isVotingFairyPeriod(start: Calendar, end: Calendar): Boolean {
    val cur = Calendar.getInstance()
    return cur.after(start) && cur.before(end)
  }

  fun getWorldName(s: String): String {
    val worldname: String
    when (s) {
      "world_spawn" -> worldname = "スポーンワールド"
      "world" -> worldname = "メインワールド"
      "world_SW" -> worldname = "第一整地ワールド"
      "world_SW_2" -> worldname = "第二整地ワールド"
      "world_SW_3" -> worldname = "第三整地ワールド"
      "world_SW_nether" -> worldname = "整地ネザー"
      "world_SW_the_end" -> worldname = "整地エンド"
      else -> worldname = s
    }
    return worldname
  }

  fun setDifficulty(worldNameList: List<String>, difficulty: Difficulty) {
    for (name in worldNameList) {
      val world = Bukkit.getWorld(name)
      if (world == null) {
        Bukkit.getLogger().warning(name + "という名前のワールドは存在しません。")
        continue
      }
      world.difficulty = difficulty
    }
  }

  /**
   * 指定した名前のマインスタックオブジェクトを返す
   */
  // TODO これはここにあるべきではない
  @Deprecated("")
  fun findMineStackObjectByName(name: String): MineStackObj? {
    return MineStackObjectList.minestacklist!!.stream()
        .filter { obj -> name == obj.mineStackObjName }
        .findFirst().orElse(null)
  }

  fun isEnemy(type: EntityType): Boolean {
    when (type) {
      //通常世界MOB
      EntityType.CAVE_SPIDER -> return true
      EntityType.CREEPER -> return true
      EntityType.GUARDIAN -> return true
      EntityType.SILVERFISH -> return true
      EntityType.SKELETON -> return true
      EntityType.SLIME -> return true
      EntityType.SPIDER -> return true
      EntityType.WITCH -> return true
      EntityType.ZOMBIE -> return true
      //ネザーMOB
      EntityType.BLAZE -> return true
      EntityType.GHAST -> return true
      EntityType.MAGMA_CUBE -> return true
      EntityType.PIG_ZOMBIE -> return true
      //エンドMOB
      EntityType.ENDERMAN -> return true
      EntityType.ENDERMITE -> return true
      EntityType.SHULKER -> return true
      //敵MOB以外(エンドラ,ウィザーは除外)
      else -> return false
    }
  }

  fun isMineHeadItem(itemstack: ItemStack): Boolean {
    return itemstack.type == Material.CARROT_STICK && loreIndexOf(itemstack.itemMeta.lore, "頭を狩り取る形をしている...") >= 0
  }

  fun getSkullDataFromBlock(block: Block): ItemStack {
    //ブロックがskullじゃない場合石でも返しとく
    if (block.type != Material.SKULL) {
      return ItemStack(Material.STONE)
    }

    val skull = block.state as Skull
    var itemStack = ItemStack(Material.SKULL_ITEM)

    //SkullTypeがプレイヤー以外の場合，SkullTypeだけ設定して終わり
    if (skull.skullType != SkullType.PLAYER) {
      when (skull.skullType) {
        SkullType.CREEPER -> itemStack.durability = SkullType.CREEPER.ordinal.toShort()
        SkullType.DRAGON -> itemStack.durability = SkullType.DRAGON.ordinal.toShort()
        SkullType.SKELETON -> itemStack.durability = SkullType.SKELETON.ordinal.toShort()
        SkullType.WITHER -> itemStack.durability = SkullType.WITHER.ordinal.toShort()
        SkullType.ZOMBIE -> itemStack.durability = SkullType.ZOMBIE.ordinal.toShort()
        else -> {
        }
      }
      return itemStack
    }

    //プレイヤーの頭の場合，ドロップアイテムからItemStackを取得．データ値をPLAYERにして返す
    val drops = block.drops
    for (drop in drops) {
      itemStack = drop
    }

    itemStack.durability = SkullType.PLAYER.ordinal.toShort()
    return itemStack
  }

  fun isLimitedTitanItem(itemstack: ItemStack): Boolean {
    return itemstack.type == Material.DIAMOND_AXE && loreIndexOf(itemstack.itemMeta.lore, "特別なタイタンをあなたに♡") >= 0
  }

}// インスタンスを作成したところでメソッドが呼べるわけでもないので封印
