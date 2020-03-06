package com.github.unchama.buildassist

import java.text.NumberFormat
import java.util.Locale

import cats.data.NonEmptyList
import com.github.unchama.buildassist.util.AsyncInventorySetter
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.util.ItemMetaFactory
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.{ItemMeta, SkullMeta}
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.{Bukkit, Material}

object MenuInventoryData {

  import scala.jdk.CollectionConverters._

  /**
   * インデックス0 = 10 ** 0 (=> 1)
   * インデックス1 = 10 ** 1 (=> 10)
   * インデックス2 = 10 ** 2 (=> 100)
   * ...
   * インデックスn = 10 ** n
   */
  private val power10 = PowerOf10.power10

  def getSetBlockSkillData(p: Player): Inventory = {
    //プレイヤーを取得
    val player = p.getPlayer
    //UUID取得
    val uuid = player.getUniqueId
    //プレイヤーデータ
    val playerdata = BuildAssist.playermap(uuid) // If NPE, player is already offline

    val inventory = Bukkit.getServer.createInventory(null, 4 * 9, DARK_PURPLE.toString + "" + BOLD + "「範囲設置スキル」設定画面")
    var itemstack = new ItemStack(Material.BARRIER, 1)
    var itemmeta = Bukkit.getItemFactory.getItemMeta(Material.BARRIER)
    var skullmeta = ItemMetaFactory.SKULL.getValue
    var lore = List(RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで移動")

    // FIXME: BAD NAME
    val ZSSkill = if (playerdata.ZoneSetSkillFlag) {
      "ON"
    } else {
      "OFF"
    }

    // FIXME: BAD NAME
    val ZSDirt = if (playerdata.zsSkillDirtFlag) {
      "ON"
    } else {
      "OFF"
    }

    // FIXME: BAD NAME
    val ZSSkill_Minestack = if (playerdata.zs_minestack_flag) {
      "ON"
    } else {
      "OFF"
    }

    // FIXME: BAD NAME
    val ZSSkillA = playerdata.AREAint * 2 + 1

    //初期画面へ移動
    itemstack.setDurability(3.toShort)
    itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "元のページへ")
    itemmeta.setLore(lore.asJava)
    itemstack.setItemMeta(itemmeta)
    inventory.setItem(0, itemstack)

    //土設置のON/OFF
    itemstack = new ItemStack(Material.DIRT, 1)
    itemmeta = Bukkit.getItemFactory.getItemMeta(Material.STONE)
    itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "設置時に下の空洞を埋める機能")
    lore = List(RESET.toString + "" + AQUA + "" + UNDERLINE + "機能の使用設定：" + ZSDirt, RESET.toString + "" + AQUA + "" + UNDERLINE + "機能の範囲：地下5マスまで")
    itemmeta.setLore(lore.asJava)
    itemstack.setItemMeta(itemmeta)
    inventory.setItem(4, itemstack)

    //設定状況の表示
    itemstack = new ItemStack(Material.STONE, 1)
    itemmeta = Bukkit.getItemFactory.getItemMeta(Material.STONE)
    itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "現在の設定は以下の通りです")
    lore = List(RESET.toString + "" + AQUA + "" + UNDERLINE + "スキルの使用設定：" + ZSSkill, RESET.toString + "" + AQUA + "" + UNDERLINE + "スキルの範囲設定：" + ZSSkillA + "×" + ZSSkillA, RESET.toString + "" + AQUA + "" + UNDERLINE + "MineStack優先設定:" + ZSSkill_Minestack)
    itemmeta.setLore(lore.asJava)
    itemstack.setItemMeta(itemmeta)
    inventory.setItem(13, itemstack)

    //範囲をMAXへ
    itemstack = new ItemStack(Material.SKULL_ITEM, 11)
    itemstack.setDurability(3.toShort)
    skullmeta.setDisplayName(RED.toString + "" + UNDERLINE + "" + BOLD + "範囲設定を最大値に変更")
    lore = List(RESET.toString + "" + AQUA + "" + "現在の範囲設定：" + ZSSkillA + "×" + ZSSkillA, RESET.toString + "" + AQUA + "" + UNDERLINE + "変更後の範囲設定：11×11")
    skullmeta.setLore(lore.asJava)
    skullmeta.setOwner("MHF_ArrowUp")
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 19, itemstack)

    //範囲を一段階増加
    itemstack = new ItemStack(Material.SKULL_ITEM, 7)
    skullmeta = ItemMetaFactory.SKULL.getValue
    itemstack.setDurability(3.toShort)
    skullmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "範囲設定を一段階大きくする")
    lore = List(RESET.toString + "" + AQUA + "" + "現在の範囲設定：" + ZSSkillA + "×" + ZSSkillA, RESET.toString + "" + AQUA + "" + UNDERLINE + "変更後の範囲設定：" + (ZSSkillA + 2) + "×" + (ZSSkillA + 2), RESET.toString + "" + RED + "" + "※範囲設定の最大値は11×11※")
    skullmeta.setLore(lore.asJava)
    skullmeta.setOwner("MHF_ArrowUp")
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 20, itemstack)

    //範囲を初期値へ
    itemstack = new ItemStack(Material.SKULL_ITEM, 5)
    skullmeta = ItemMetaFactory.SKULL.getValue
    itemstack.setDurability(3.toShort)
    skullmeta.setDisplayName(RED.toString + "" + UNDERLINE + "" + BOLD + "範囲設定を初期値に変更")
    lore = List(RESET.toString + "" + AQUA + "" + "現在の範囲設定：" + ZSSkillA + "×" + ZSSkillA, RESET.toString + "" + AQUA + "" + UNDERLINE + "変更後の範囲設定：5×5")
    skullmeta.setLore(lore.asJava)
    skullmeta.setOwner("MHF_TNT")
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 22, itemstack)

    //範囲を一段階減少
    itemstack = new ItemStack(Material.SKULL_ITEM, 3)
    skullmeta = ItemMetaFactory.SKULL.getValue
    itemstack.setDurability(3.toShort)
    skullmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "範囲設定を一段階小さくする")
    lore = List(RESET.toString + "" + AQUA + "" + "現在の範囲設定：" + ZSSkillA + "×" + ZSSkillA, RESET.toString + "" + AQUA + "" + UNDERLINE + "変更後の範囲設定：" + (ZSSkillA - 2) + "×" + (ZSSkillA - 2), RESET.toString + "" + RED + "" + "※範囲設定の最小値は3×3※")
    skullmeta.setLore(lore.asJava)
    skullmeta.setOwner("MHF_ArrowDown")
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 24, itemstack)


    //範囲をMINへ
    itemstack = new ItemStack(Material.SKULL_ITEM, 1)
    skullmeta = ItemMetaFactory.SKULL.getValue
    itemstack.setDurability(3.toShort)
    skullmeta.setDisplayName(RED.toString + "" + UNDERLINE + "" + BOLD + "範囲設定を最小値に変更")
    lore = List(RESET.toString + "" + AQUA + "" + "現在の範囲設定：" + ZSSkillA + "×" + ZSSkillA, RESET.toString + "" + AQUA + "" + UNDERLINE + "変更後の範囲設定：3×3")
    skullmeta.setLore(lore.asJava)
    skullmeta.setOwner("MHF_ArrowDown")
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 25, itemstack)

    //35番目にMineStack優先設定を追加
    //MineStackの方を優先して消費する設定
    itemstack = new ItemStack(Material.CHEST, 1)
    itemmeta = Bukkit.getItemFactory.getItemMeta(Material.CHEST)
    itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "MineStack優先設定：" + ZSSkill_Minestack)
    lore = List(RESET.toString + "" + GRAY + "スキルでブロックを並べるとき", RESET.toString + "" + GRAY + "MineStackの在庫を優先して消費します。", RESET.toString + "" + GRAY + "建築LV" + BuildAssist.config.getZoneskillMinestacklevel + "以上で利用可能", RESET.toString + "" + GRAY + "クリックで切り替え"
    )
    itemmeta.setLore(lore.asJava)
    itemstack.setItemMeta(itemmeta)
    inventory.setItem(35, itemstack)

    inventory
  }

  //ブロックを並べる設定メニュー
  def getBlockLineUpData(p: Player): Inventory = {
    //プレイヤーを取得
    val player = p.getPlayer
    //UUID取得
    val uuid = player.getUniqueId
    //プレイヤーデータ
    val playerdata = BuildAssist.playermap(uuid)

    val inventory = Bukkit.getServer.createInventory(null, 4 * 9, DARK_PURPLE.toString + "" + BOLD + "「ブロックを並べるスキル（仮）」設定")
    var itemstack = new ItemStack(Material.SKULL_ITEM, 1)
    var itemmeta: ItemMeta = Bukkit.getItemFactory.getItemMeta(Material.WOOD)
    val skullmeta: SkullMeta = ItemMetaFactory.SKULL.getValue
    var lore = List(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")

    // ホームを開く
    itemstack.setDurability(3.toShort)
    skullmeta.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}ホームへ")
    skullmeta.setLore(lore.asJava)
    skullmeta.setOwner("MHF_ArrowLeft")
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 27, itemstack)

    //ブロックを並べるスキル設定
    itemstack = new ItemStack(Material.WOOD, 1)
    itemmeta.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}ブロックを並べるスキル（仮） ：${BuildAssist.line_up_str(playerdata.line_up_flg)}")
    lore = List(
      s"$RESET${GRAY}オフハンドに木の棒、メインハンドに設置したいブロックを持って",
      s"$RESET${GRAY}左クリックすると向いてる方向に並べて設置します。",
      s"$RESET${GRAY}建築LV${BuildAssist.config.getblocklineuplevel()}以上で利用可能",
      s"$RESET${GRAY}クリックで切り替え"
    )
    itemmeta.setLore(lore.asJava)
    itemstack.setItemMeta(itemmeta)
    inventory.setItem(0, itemstack)

    //ブロックを並べるスキルハーフブロック設定
    itemstack = new ItemStack(Material.STEP, 1)
    itemmeta = Bukkit.getItemFactory.getItemMeta(Material.STEP)
    itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "ハーフブロック設定 ：" + BuildAssist.line_up_step_str(playerdata.line_up_step_flg))
    lore = List(
      s"$RESET${GRAY}ハーフブロックを並べる時の位置を決めます。",
      s"$RESET${GRAY}クリックで切り替え"
    )
    itemmeta.setLore(lore.asJava)
    itemstack.setItemMeta(itemmeta)
    inventory.setItem(1, itemstack)

    //ブロックを並べるスキル一部ブロックを破壊して並べる設定
    itemstack = new ItemStack(Material.TNT, 1)
    itemmeta = Bukkit.getItemFactory.getItemMeta(Material.TNT)
    itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "破壊設定 ：" + BuildAssist.line_up_off_on_str(playerdata.line_up_des_flg))
    lore = List(
      s"$RESET${GRAY}ブロックを並べるとき特定のブロックを破壊して並べます。",
      s"$RESET${GRAY}破壊対象ブロック：草,花,水,雪,松明,きのこ",
      s"$RESET${GRAY}クリックで切り替え"
    )
    itemmeta.setLore(lore.asJava)
    itemstack.setItemMeta(itemmeta)
    inventory.setItem(2, itemstack)

    //MineStackの方を優先して消費する設定
    itemstack = new ItemStack(Material.CHEST, 1)
    itemmeta = Bukkit.getItemFactory.getItemMeta(Material.CHEST)
    itemmeta.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}MineStack優先設定 ：${BuildAssist.line_up_off_on_str(playerdata.line_up_minestack_flg)}")
    lore = List(
      s"$RESET${GRAY}スキルでブロックを並べるとき",
      s"$RESET${GRAY}MineStackの在庫を優先して消費します。",
      s"$RESET${GRAY}建築LV${BuildAssist.config.getblocklineupMinestacklevel()}以上で利用可能",
      s"$RESET${GRAY}クリックで切り替え"
    )
    itemmeta.setLore(lore.asJava)
    itemstack.setItemMeta(itemmeta)
    inventory.setItem(8, itemstack)

    inventory
  }


  //MineStackブロック一括クラフトメニュー
  def getBlockCraftData(p: Player): Inventory = {
    //プレイヤーを取得
    val player = p.getPlayer
    //UUID取得
    val uuid = player.getUniqueId
    val playerdata_s = SeichiAssist.playermap(uuid) // if thrown NPE, player has already left

    val inventory = Bukkit.getServer.createInventory(null, 6 * 9, DARK_PURPLE.toString + "" + BOLD + "MineStackブロック一括クラフト1")
    var itemmeta: ItemMeta = null
    var lore: List[String] = null
    var itemstack: ItemStack = null
    var skullmeta = ItemMetaFactory.SKULL.getValue

    // ホーム目を開く
    itemstack = new ItemStack(Material.SKULL_ITEM, 1, 3.toShort)
    skullmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "ホームへ")
    lore = List(RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで移動")
    skullmeta.setLore(lore.asJava)
    skullmeta.setOwner("MHF_ArrowLeft")
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 45, itemstack)

    // 2ページ目を開く
    itemstack = new ItemStack(Material.SKULL_ITEM, 1, 3.toShort)
    skullmeta = ItemMetaFactory.SKULL.getValue
    skullmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "2ページ目へ")
    lore = List(s"""$RESET$DARK_RED${UNDERLINE}クリックで移動""")
    skullmeta.setLore(lore.asJava)
    skullmeta.setOwner("MHF_ArrowDown")
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 53, itemstack)

    //石を石ハーフブロックに変換10～10万
    collectCraftMenu(p,inventory,1,NonEmptyList.of(("stone",1)), NonEmptyList.of(("step0",2)),1,5,-1)


    //石を石レンガに変換10～1万
    collectCraftMenu(p,inventory,1,NonEmptyList.of(("stone",1)),NonEmptyList.of(("smooth_brick0",1)),1,5,8)

    //花崗岩を磨かれた花崗岩に変換10～1万
    collectCraftMenu(p,inventory,2,NonEmptyList.of(("granite",1)),NonEmptyList.of(("polished_granite",1)),1,4,17)

    //閃緑岩を磨かれた閃緑岩に変換10～1万
    collectCraftMenu(p,inventory,2,NonEmptyList.of(("diorite",1)),NonEmptyList.of(("polished_diorite",1)),1,4,22)

    //安山岩を磨かれた安山岩に変換10～1万
    collectCraftMenu(p,inventory,2,NonEmptyList.of(("andesite",1)),NonEmptyList.of(("polished_andesite",1)),1,4,26)

    //ネザー水晶をネザー水晶ブロックに変換10～1万
    collectCraftMenu(p,inventory,2,NonEmptyList.of(("quartz",4)),NonEmptyList.of(("quartz_block",1)),1,4,31)

    //レンガをレンガブロックに変換10～1万
    collectCraftMenu(p,inventory,2,NonEmptyList.of(("brick_item",4)),NonEmptyList.of(("brick",1)),1,4,35)

    //ネザーレンガをネザーレンガブロックに変換10～1万
    collectCraftMenu(p,inventory,2,NonEmptyList.of(("nether_brick_item",4)),NonEmptyList.of(("nether_brick",1)),1,4,40)

    inventory
  }

  //MineStackブロック一括クラフトメニュー2
  def getBlockCraftData2(p: Player): Inventory = {
    //プレイヤーを取得
    val player = p.getPlayer
    //UUID取得
    val uuid = player.getUniqueId
    //プレイヤーデータ
    val playerdata_s = SeichiAssist.playermap(uuid)

    val inventory = Bukkit.getServer.createInventory(null, 6 * 9, DARK_PURPLE.toString + "" + BOLD + "MineStackブロック一括クラフト2")
    var itemstack: ItemStack = null
    var itemmeta: ItemMeta = null
    var skullmeta: SkullMeta = ItemMetaFactory.SKULL.getValue
    var lore: List[String] = null

    // 1ページ目を開く
    itemstack = new ItemStack(Material.SKULL_ITEM, 1, 3)
    skullmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "1ページ目へ")
    lore = List(RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで移動")
    skullmeta.setLore(lore.asJava)
    skullmeta.setOwner("MHF_ArrowUp")
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 45, itemstack)

    // 3ページ目を開く
    itemstack = new ItemStack(Material.SKULL_ITEM, 1, 3)
    skullmeta = ItemMetaFactory.SKULL.getValue
    skullmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "3ページ目へ")
    lore = List(RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで移動")
    skullmeta.setLore(lore.asJava)
    skullmeta.setOwner("MHF_ArrowDown")
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 53, itemstack)

    //雪玉を雪（ブロック）に変換10～1万
    collectCraftMenu(p,inventory,2,NonEmptyList.of(("snow_ball",4)),NonEmptyList.of(("snow_block",1)),1,4,-1)

    //ネザーウォートとネザーレンガを赤いネザーレンガに変換10～1万
    collectCraftMenu(p,inventory,2,NonEmptyList.of(("nether_stalk",2),("nether_brick_item",2)),NonEmptyList.of(("red_nether_brick",1)),1,5,4)

    //石炭を消費して鉄鉱石を鉄インゴットに変換4～4000
    collectCraftMenu(p,inventory,3,NonEmptyList.of(("iron_ore",4),("coal",1)),NonEmptyList.of(("iron_ingot",4)),0,3,9)

    //溶岩バケツを消費して鉄鉱石を鉄インゴットに変換50～5万
    collectCraftMenu(p,inventory,3,NonEmptyList.of(("iron_ore",50),("lava_bucket",1)),NonEmptyList.of(("iron_ingot",50)),0,3,14)

    //石炭を消費して金鉱石を金インゴットに変換4～4000
    collectCraftMenu(p,inventory,3,NonEmptyList.of(("gold_ore",4),("coal",1)),NonEmptyList.of(("gold_ingot",4)),0,3,18)

    //溶岩バケツを消費して金鉱石を金インゴットに変換50～5万
    collectCraftMenu(p,inventory,3,NonEmptyList.of(("gold_ore",50),("lava_bucket",1)),NonEmptyList.of(("gold_ingot",50)),0,3,23)

    //石炭を消費して砂をガラスに変換4～4000
    collectCraftMenu(p,inventory,3,NonEmptyList.of(("sand",4),("coal",1)),NonEmptyList.of(("glass",4)),0,3,27)

    //溶岩バケツを消費して砂をガラスに変換50～5万
    collectCraftMenu(p,inventory,3,NonEmptyList.of(("sand",50),("lava_bucket",1)),NonEmptyList.of(("glass",50)),0,3,32)

    //石炭を消費してネザーラックをネザーレンガに変換4～4000
    collectCraftMenu(p,inventory,3,NonEmptyList.of(("netherrack",4),("coal",1)),NonEmptyList.of(("nether_brick_item",4)),0,3,36)

    //溶岩バケツを消費してネザーラックをネザーレンガに変換50～5万
    collectCraftMenu(p,inventory,3,NonEmptyList.of(("netherrack",50),("lava_bucket",1)),NonEmptyList.of(("nether_brick_item",50)),0,3,41)

    inventory
  }

  //MineStackブロック一括クラフトメニュー3
  def getBlockCraftData3(p: Player): Inventory = {
    //プレイヤーを取得
    val player = p.getPlayer
    //UUID取得
    val uuid = player.getUniqueId
    val playerdata_s = SeichiAssist.playermap(uuid)

    val inventory = Bukkit.getServer.createInventory(null, 6 * 9, DARK_PURPLE.toString + "" + BOLD + "MineStackブロック一括クラフト3")
    var itemstack: ItemStack = null
    var itemmeta: ItemMeta = null
    var lore: List[String] = null

    // 2ページ目を開く
    itemstack = new ItemStack(Material.SKULL_ITEM, 1, 3.toShort)
    skullmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "2ページ目へ")
    lore = List(RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで移動")
    skullmeta.setLore(lore.asJava)
    skullmeta.setOwner("MHF_ArrowUp")
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 45, itemstack)

    // 4ページ目を開く
    itemstack = new ItemStack(Material.SKULL_ITEM, 1, 3)
    val skullmeta = metaFactory.getValue
    skullmeta.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}4ページ目へ")
    lore = List(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
    skullmeta.setLore(lore.asJava)
    skullmeta.setOwner("MHF_ArrowDown")
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 53, itemstack)

    //石炭を消費して粘土をレンガに変換4～4000
    collectCraftMenu(p,inventory,3,NonEmptyList.of(("clay_ball",4),("coal",1)),NonEmptyList.of(("brick_item",4)),0,3,0)

    //溶岩バケツを消費して粘土をレンガに変換50～5万
    collectCraftMenu(p,inventory,3,NonEmptyList.of(("clay_ball",50),("lava_bucket",1)),NonEmptyList.of(("brick_item",50)),0,3,5)

    inventory
  }

  private def collectCraftMenu(p: Player, inventory: Inventory, minestackBlockCraftlevel: Int, beforeItemID: NonEmptyList[(String, Int)], afterItemID: NonEmptyList[(String, Int)], minPower10Index: Int, maxPower10Index: Int, inventoryIndex: Int): Unit ={
    //プレイヤーを取得
    val player = p.getPlayer
    //UUID取得
    val uuid = player.getUniqueId
    val playerdata_s = SeichiAssist.playermap(uuid)
    //順番に処理をする
    val ingredients = beforeItemID
      .toList
      .map(string => (Util.findMineStackObjectByName(string._1), string._2))
    //1個以上ないが…
    val products = afterItemID.toList
      .map(string => (Util.findMineStackObjectByName(string._1), string._2))

    {
      for (x <- minPower10Index to maxPower10Index) {
        val p10 = power10(x)
        val itemstack = products.head._1.itemStack
        val itemmeta = itemstack.getItemMeta()
        itemmeta.setDisplayName(s"$YELLOW$UNDERLINE$BOLD${ingredients.map(mineStackObj => mineStackObj._1.uiName.get).mkString("と")}を${products.map(mineStackObj => mineStackObj._1.uiName.get).mkString("と")}に変換します")
        val ingredientRequest = ingredients.map(mineStackObjName => s"${mineStackObjName._1.uiName.get}${p10 * mineStackObjName._2}個").mkString("+")
        val lore = List(
          s"$RESET$GRAY$ingredientRequest→${products.head._1.uiName.get}${p10 * products.head._2}個"
        ) ::: {ingredients.map(mineStackObjName => s"$RESET$GRAY${mineStackObjName._1.uiName.get}の数:${comma(playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName(mineStackObjName._1.mineStackObjName)))}")} :::
          {products.map(mineStackObjName => s"${RESET.toString}$GRAY${mineStackObjName._1.uiName.get}の数:${comma(playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName(mineStackObjName._1.mineStackObjName)))}")} :::
          List(
            s"$RESET${GRAY}建築LV${BuildAssist.config.getMinestackBlockCraftlevel(minestackBlockCraftlevel)}以上で利用可能",
            s"$RESET$DARK_RED${UNDERLINE}クリックで変換"
          )
        itemmeta.setLore(lore.asJava)
        itemstack.setItemMeta(itemmeta)
        inventory.setItem(x + inventoryIndex, itemstack)
      }
    }
  }
  private def comma(i: Long): String = {
    NumberFormat.getNumberInstance(Locale.US).format(i)
  }
}
