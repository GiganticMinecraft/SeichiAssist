package com.github.unchama.buildassist

import java.text.NumberFormat
import java.util.Locale

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
    var num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("stone"))
    var num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("step0"))
    for (x <- 1 to 5) {
      val p10 = power10(x)
      itemstack = new ItemStack(Material.STEP, x)
      itemmeta = Bukkit.getItemFactory.getItemMeta(Material.STEP)
      itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "石を石ハーフブロックに変換します")
      lore = List(
        s"$RESET${GRAY}石${p10}個→石ハーフブロック${p10 * 2}個",
        s"$RESET${GRAY}石の数:${comma(num_1)}",
        s"$RESET${GRAY}石ハーフブロックの数:${comma(num_2)}", RESET.toString + "" + GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(1) + "以上で利用可能", RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで変換"
      )
      itemmeta.setLore(lore.asJava)
      itemstack.setItemMeta(itemmeta)
      inventory.setItem(x - 1, itemstack)
    }


    //石を石レンガに変換10～10万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("stone"))
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("smooth_brick0"))
    for (x <- 1 to 5) {
      val p10 = power10(x)
      itemstack = new ItemStack(Material.SMOOTH_BRICK, x)
      itemmeta = Bukkit.getItemFactory.getItemMeta(Material.SMOOTH_BRICK)
      itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "石を石レンガに変換します")
      lore = List(RESET.toString + "" + GRAY + "石" + p10 + "個→石レンガ" + p10 + "個", RESET.toString + "" + GRAY + "石の数:" + comma(num_1), RESET.toString + "" + GRAY + "石レンガの数:" + comma(num_2), RESET.toString + "" + GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(1) + "以上で利用可能", RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで変換"
      )
      itemmeta.setLore(lore.asJava)
      itemstack.setItemMeta(itemmeta)
      inventory.setItem(x + 8, itemstack)
    }

    //花崗岩を磨かれた花崗岩に変換10～1万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("granite"))
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("polished_granite"))
    for (x <- 1 to 4) {
      val p10 = power10(x)
      itemstack = new ItemStack(Material.STONE, x, 2.toShort)
      itemmeta = Bukkit.getItemFactory.getItemMeta(Material.STONE)
      itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "花崗岩を磨かれた花崗岩に変換します")
      lore = List(RESET.toString + "" + GRAY + "花崗岩" + p10 + "個→磨かれた花崗岩" + p10 + "個", RESET.toString + "" + GRAY + "花崗岩の数:" + comma(num_1), RESET.toString + "" + GRAY + "磨かれた花崗岩の数:" + comma(num_2), RESET.toString + "" + GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能", RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで変換"
      )
      itemmeta.setLore(lore.asJava)
      itemstack.setItemMeta(itemmeta)
      inventory.setItem(x + 17, itemstack)
    }

    //閃緑岩を磨かれた閃緑岩に変換10～1万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("diorite"))
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("polished_diorite"))
    for (x <- 1 to 4) {
      val p10 = power10(x)
      itemstack = new ItemStack(Material.STONE, x, 4.toShort)
      itemmeta = Bukkit.getItemFactory.getItemMeta(Material.STONE)
      itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "閃緑岩を磨かれた閃緑岩に変換します")
      lore = List(RESET.toString + "" + GRAY + "閃緑岩" + p10 + "個→磨かれた閃緑岩" + p10 + "個", RESET.toString + "" + GRAY + "閃緑岩の数:" + comma(num_1), RESET.toString + "" + GRAY + "磨かれた閃緑岩の数:" + comma(num_2), RESET.toString + "" + GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能", RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで変換"
      )
      itemmeta.setLore(lore.asJava)
      itemstack.setItemMeta(itemmeta)
      inventory.setItem(x + 22, itemstack)
    }

    //安山岩を磨かれた安山岩に変換10～1万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("andesite"))
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("polished_andesite"))
    for (x <- 1 to 4) {
      val p10 = power10(x)
      itemstack = new ItemStack(Material.STONE, x, 6.toShort)
      itemmeta = Bukkit.getItemFactory.getItemMeta(Material.STONE)
      itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "安山岩を磨かれた安山岩に変換します")
      lore = List(RESET.toString + "" + GRAY + "安山岩" + p10 + "個→磨かれた安山岩" + p10 + "個", RESET.toString + "" + GRAY + "安山岩の数:" + comma(num_1), RESET.toString + "" + GRAY + "磨かれた安山岩の数:" + comma(num_2), RESET.toString + "" + GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能", RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで変換"
      )
      itemmeta.setLore(lore.asJava)
      itemstack.setItemMeta(itemmeta)
      inventory.setItem(x + 26, itemstack)
    }

    //ネザー水晶をネザー水晶ブロックに変換10～1万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("quartz"))
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("quartz_block"))
    for (x <- 1 to 4) {
      val p10 = power10(x)
      itemstack = new ItemStack(Material.QUARTZ_BLOCK, x)
      itemmeta = Bukkit.getItemFactory.getItemMeta(Material.QUARTZ_BLOCK)
      itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "ネザー水晶をネザー水晶ブロックに変換します")
      lore = List(RESET.toString + "" + GRAY + "ネザー水晶" + p10 * 4 + "個→ネザー水晶ブロック" + p10 + "個", RESET.toString + "" + GRAY + "ネザー水晶の数:" + comma(num_1), RESET.toString + "" + GRAY + "ネザー水晶ブロックの数:" + comma(num_2), RESET.toString + "" + GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能", RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで変換"
      )
      itemmeta.setLore(lore.asJava)
      itemstack.setItemMeta(itemmeta)
      inventory.setItem(x + 31, itemstack)
    }

    //レンガをレンガブロックに変換10～1万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("brick_item"))
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("brick"))
    for (x <- 1 to 4) {
      val p10 = power10(x)
      itemstack = new ItemStack(Material.BRICK, x)
      itemmeta = Bukkit.getItemFactory.getItemMeta(Material.BRICK)
      itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "レンガをレンガブロックに変換します")
      lore = List(RESET.toString + "" + GRAY + "レンガ" + p10 * 4 + "個→レンガブロック" + p10 + "個", RESET.toString + "" + GRAY + "レンガの数:" + comma(num_1), RESET.toString + "" + GRAY + "レンガブロックの数:" + comma(num_2), RESET.toString + "" + GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能", RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで変換"
      )
      itemmeta.setLore(lore.asJava)
      itemstack.setItemMeta(itemmeta)
      inventory.setItem(x + 35, itemstack)
    }

    //ネザーレンガをネザーレンガブロックに変換10～1万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("nether_brick_item"))
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("nether_brick"))
    for (x <- 1 to 4) {
      val p10 = power10(x)
      itemstack = new ItemStack(Material.NETHER_BRICK, x)
      itemmeta = Bukkit.getItemFactory.getItemMeta(Material.NETHER_BRICK)
      itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "ネザーレンガをネザーレンガブロックに変換します")
      lore = List(
        s"$RESET${GRAY}ネザーレンガ${p10 * 4}個→ネザーレンガブロック${p10}個",
        s"$RESET${GRAY}ネザーレンガの数:${comma(num_1)}",
        s"$RESET${GRAY}ネザーレンガブロックの数:${comma(num_2)}",
        s"$RESET${GRAY}建築LV${BuildAssist.config.getMinestackBlockCraftlevel(2)}以上で利用可能",
        s"$RESET$DARK_RED${UNDERLINE}クリックで変換"
      )
      itemmeta.setLore(lore.asJava)
      itemstack.setItemMeta(itemmeta)
      inventory.setItem(x + 40, itemstack)
    }
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
    var num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("snow_ball"))
    var num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("snow_block"))
    var num_3: Long = 0
    for (x <- 1 to 4) {
      val p10 = power10(x)
      itemstack = new ItemStack(Material.SNOW_BLOCK, x)
      itemmeta = Bukkit.getItemFactory.getItemMeta(Material.SNOW_BLOCK)
      itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "雪玉を雪（ブロック）に変換します")
      lore = List(RESET.toString + "" + GRAY + "雪玉" + p10 * 4 + "個→雪（ブロック）" + p10 + "個", RESET.toString + "" + GRAY + "雪玉の数:" + comma(num_1), RESET.toString + "" + GRAY + "雪（ブロック）の数:" + comma(num_2), RESET.toString + "" + GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能", RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで変換"
      )
      itemmeta.setLore(lore.asJava)
      itemstack.setItemMeta(itemmeta)
      inventory.setItem(x - 1, itemstack)
    }


    //ネザーウォートとネザーレンガを赤いネザーレンガに変換10～10万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("nether_stalk"))
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("red_nether_brick"))
    num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("nether_brick_item"))
    for (x <- 1 to 5) {
      val p10 = power10(x)
      itemstack = new ItemStack(Material.RED_NETHER_BRICK, x)
      itemmeta = Bukkit.getItemFactory.getItemMeta(Material.RED_NETHER_BRICK)
      itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "ネザーウォートとネザーレンガを赤いネザーレンガに変換します")
      lore = List(RESET.toString + "" + GRAY + "ネザーウォート" + p10 * 2 + "個+ネザーレンガ" + p10 * 2 + "個→赤いネザーレンガ" + p10 + "個", RESET.toString + "" + GRAY + "ネザーウォートの数:" + comma(num_1), RESET.toString + "" + GRAY + "ネザーレンガの数:" + comma(num_3), RESET.toString + "" + GRAY + "赤いネザーレンガの数:" + comma(num_2), RESET.toString + "" + GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能", RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで変換"
      )
      itemmeta.setLore(lore.asJava)
      itemstack.setItemMeta(itemmeta)
      inventory.setItem(x + 4, itemstack)
    }

    //石炭を消費して鉄鉱石を鉄インゴットに変換4～4000
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("iron_ore"))
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("iron_ingot"))
    num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("coal"))
    for (x <- 0 to 3) {
      val p10 = power10(x)
      itemstack = new ItemStack(Material.IRON_INGOT, x)
      itemmeta = Bukkit.getItemFactory.getItemMeta(Material.IRON_INGOT)
      itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "石炭を消費して鉄鉱石を鉄インゴットに変換します")
      lore = List(RESET.toString + "" + GRAY + "鉄鉱石" + p10 * 4 + "個+石炭" + p10 + "個→鉄インゴット" + p10 * 4 + "個", RESET.toString + "" + GRAY + "鉄鉱石の数:" + comma(num_1), RESET.toString + "" + GRAY + "石炭の数:" + comma(num_3), RESET.toString + "" + GRAY + "鉄インゴットの数:" + comma(num_2), RESET.toString + "" + GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能", RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで変換"
      )
      itemmeta.setLore(lore.asJava)
      itemstack.setItemMeta(itemmeta)
      inventory.setItem(x + 9, itemstack)
    }

    //溶岩バケツを消費して鉄鉱石を鉄インゴットに変換50～5万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("iron_ore"))
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("iron_ingot"))
    num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("lava_bucket"))
    for (x <- 0 to 3) {
      val p10 = power10(x)
      itemstack = new ItemStack(Material.IRON_INGOT, x)
      itemmeta = Bukkit.getItemFactory.getItemMeta(Material.IRON_INGOT)
      itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "溶岩バケツを消費して鉄鉱石を鉄インゴットに変換します")
      lore = List(RESET.toString + "" + GRAY + "鉄鉱石" + p10 * 50 + "個+溶岩バケツ" + p10 + "個→鉄インゴット" + p10 * 50 + "個", RESET.toString + "" + GRAY + "鉄鉱石の数:" + comma(num_1), RESET.toString + "" + GRAY + "溶岩バケツの数:" + comma(num_3), RESET.toString + "" + GRAY + "鉄インゴットの数:" + comma(num_2), RESET.toString + "" + GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能", RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで変換"
      )
      itemmeta.setLore(lore.asJava)
      itemstack.setItemMeta(itemmeta)
      inventory.setItem(x + 14, itemstack)
    }

    //石炭を消費して金鉱石を金インゴットに変換4～4000
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("gold_ore"))
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("gold_ingot"))
    num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("coal"))
    for (x <- 0 to 3) {
      val p10 = power10(x)
      itemstack = new ItemStack(Material.GOLD_INGOT, x)
      itemmeta = Bukkit.getItemFactory.getItemMeta(Material.GOLD_INGOT)
      itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "石炭を消費して金鉱石を金インゴットに変換します")
      lore = List(RESET.toString + "" + GRAY + "金鉱石" + p10 * 4 + "個+石炭" + p10 + "個→金インゴット" + p10 * 4 + "個", RESET.toString + "" + GRAY + "金鉱石の数:" + comma(num_1), RESET.toString + "" + GRAY + "石炭の数:" + comma(num_3), RESET.toString + "" + GRAY + "金インゴットの数:" + comma(num_2), RESET.toString + "" + GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能", RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで変換"
      )
      itemmeta.setLore(lore.asJava)
      itemstack.setItemMeta(itemmeta)
      inventory.setItem(x + 18, itemstack)
    }

    //溶岩バケツを消費して金鉱石を金インゴットに変換50～5万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("gold_ore"))
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("gold_ingot"))
    num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("lava_bucket"))
    for (x <- 0 to 3) {
      val p10 = power10(x)
      itemstack = new ItemStack(Material.GOLD_INGOT, x)
      itemmeta = Bukkit.getItemFactory.getItemMeta(Material.GOLD_INGOT)
      itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "溶岩バケツを消費して金鉱石を金インゴットに変換します")
      lore = List(RESET.toString + "" + GRAY + "金鉱石" + p10 * 50 + "個+溶岩バケツ" + p10 + "個→金インゴット" + p10 * 50 + "個", RESET.toString + "" + GRAY + "金鉱石の数:" + comma(num_1), RESET.toString + "" + GRAY + "溶岩バケツの数:" + comma(num_3), RESET.toString + "" + GRAY + "金インゴットの数:" + comma(num_2), RESET.toString + "" + GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能", RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで変換"
      )
      itemmeta.setLore(lore.asJava)
      itemstack.setItemMeta(itemmeta)
      inventory.setItem(x + 23, itemstack)
    }

    //石炭を消費して砂をガラスに変換4～4000
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("sand"))
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("glass"))
    num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("coal"))
    for (x <- 0 to 3) {
      val p10 = power10(x)
      itemstack = new ItemStack(Material.GLASS, x)
      itemmeta = Bukkit.getItemFactory.getItemMeta(Material.GLASS)
      itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "石炭を消費して砂をガラスに変換します")
      lore = List(RESET.toString + "" + GRAY + "砂" + p10 * 4 + "個+石炭" + p10 + "個→ガラス" + p10 * 4 + "個", RESET.toString + "" + GRAY + "砂の数:" + comma(num_1), RESET.toString + "" + GRAY + "石炭の数:" + comma(num_3), RESET.toString + "" + GRAY + "ガラスの数:" + comma(num_2), RESET.toString + "" + GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能", RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで変換"
      )
      itemmeta.setLore(lore.asJava)
      itemstack.setItemMeta(itemmeta)
      inventory.setItem(x + 27, itemstack)
    }

    //溶岩バケツを消費して砂をガラスに変換50～5万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("sand"))
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("glass"))
    num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("lava_bucket"))
    for (x <- 0 to 3) {
      val p10 = power10(x)
      itemstack = new ItemStack(Material.GLASS, x)
      itemmeta = Bukkit.getItemFactory.getItemMeta(Material.GLASS)
      itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "溶岩バケツを消費して砂をガラスに変換します")
      lore = List(RESET.toString + "" + GRAY + "砂" + p10 * 50 + "個+溶岩バケツ" + p10 + "個→ガラス" + p10 * 50 + "個", RESET.toString + "" + GRAY + "砂の数:" + comma(num_1), RESET.toString + "" + GRAY + "溶岩バケツの数:" + comma(num_3), RESET.toString + "" + GRAY + "ガラスの数:" + comma(num_2), RESET.toString + "" + GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能", RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで変換"
      )
      itemmeta.setLore(lore.asJava)
      itemstack.setItemMeta(itemmeta)
      inventory.setItem(x + 32, itemstack)
    }

    //石炭を消費してネザーラックをネザーレンガに変換4～4000
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("netherrack"))
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("nether_brick_item"))
    num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("coal"))
    for (x <- 0 to 3) {
      val p10 = power10(x)
      itemstack = new ItemStack(Material.NETHER_BRICK_ITEM, x)
      itemmeta = Bukkit.getItemFactory.getItemMeta(Material.NETHER_BRICK_ITEM)
      itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "石炭を消費してネザーラックをネザーレンガに変換します")
      lore = List(RESET.toString + "" + GRAY + "ネザーラック" + p10 * 4 + "個+石炭" + p10 + "個→ネザーレンガ" + p10 * 4 + "個", RESET.toString + "" + GRAY + "ネザーラックの数:" + comma(num_1), RESET.toString + "" + GRAY + "石炭の数:" + comma(num_3), RESET.toString + "" + GRAY + "ネザーレンガの数:" + comma(num_2), RESET.toString + "" + GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能", RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで変換"
      )
      itemmeta.setLore(lore.asJava)
      itemstack.setItemMeta(itemmeta)
      inventory.setItem(x + 36, itemstack)
    }

    //溶岩バケツを消費してネザーラックをネザーレンガに変換50～5万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("netherrack"))
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("nether_brick_item"))
    num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("lava_bucket"))
    for (x <- 0 to 3) {
      val p10 = power10(x)
      itemstack = new ItemStack(Material.NETHER_BRICK_ITEM, x)
      itemmeta = Bukkit.getItemFactory.getItemMeta(Material.NETHER_BRICK_ITEM)
      itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "溶岩バケツを消費してネザーラックをネザーレンガに変換します")
      lore = List(RESET.toString + "" + GRAY + "ネザーラック" + p10 * 50 + "個+溶岩バケツ" + p10 + "個→ネザーレンガ" + p10 * 50 + "個", RESET.toString + "" + GRAY + "ネザーラックの数:" + comma(num_1), RESET.toString + "" + GRAY + "溶岩バケツの数:" + comma(num_3), RESET.toString + "" + GRAY + "ネザーレンガの数:" + comma(num_2), RESET.toString + "" + GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能", RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで変換"
      )
      itemmeta.setLore(lore.asJava)
      itemstack.setItemMeta(itemmeta)
      inventory.setItem(x + 41, itemstack)
    }
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
    val skullmeta: SkullMeta = ItemMetaFactory.SKULL.getValue
    var lore: List[String] = null

    // 2ページ目を開く
    itemstack = new ItemStack(Material.SKULL_ITEM, 1, 3.toShort)
    skullmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "2ページ目へ")
    lore = List(RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで移動")
    skullmeta.setLore(lore.asJava)
    skullmeta.setOwner("MHF_ArrowUp")
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 45, itemstack)

    //石炭を消費して粘土をレンガに変換4～4000
    {
      val num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("clay_ball"))
      val num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("brick_item"))
      val num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("coal"))
      for (x <- 0 to 3) {
        val p10 = power10(x)
        itemstack = new ItemStack(Material.CLAY_BRICK, x)
        itemmeta = Bukkit.getItemFactory.getItemMeta(Material.CLAY_BRICK)
        itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "石炭を消費して粘土をレンガに変換します")
        lore = List(s"$RESET${GRAY}粘土${p10 * 4}個+石炭${p10}個→レンガ${p10 * 4}個", RESET.toString + "" + GRAY + "粘土の数:" + comma(num_1), RESET.toString + "" + GRAY + "石炭の数:" + comma(num_3), RESET.toString + "" + GRAY + "レンガの数:" + comma(num_2), RESET.toString + "" + GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能", RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで変換"
        )
        itemmeta.setLore(lore.asJava)
        itemstack.setItemMeta(itemmeta)
        inventory.setItem(x, itemstack)
      }
    }

    //溶岩バケツを消費して粘土をレンガに変換50～5万
    {
      val num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("clay_ball"))
      val num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("brick_item"))
      val num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("lava_bucket"))
      for (x <- 0 to 3) {
        val p10 = power10(x)
        itemstack = new ItemStack(Material.CLAY_BRICK, x)
        itemmeta = Bukkit.getItemFactory.getItemMeta(Material.CLAY_BRICK)
        itemmeta.setDisplayName(YELLOW.toString + "" + UNDERLINE + "" + BOLD + "溶岩バケツを消費して粘土をレンガに変換します")
        lore = List(RESET.toString + "" + GRAY + "粘土" + p10 * 50 + "個+溶岩バケツ" + p10 + "個→レンガ" + p10 * 50 + "個", RESET.toString + "" + GRAY + "粘土の数:" + comma(num_1), RESET.toString + "" + GRAY + "溶岩バケツの数:" + comma(num_3), RESET.toString + "" + GRAY + "レンガの数:" + comma(num_2), RESET.toString + "" + GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能", RESET.toString + "" + DARK_RED + "" + UNDERLINE + "クリックで変換"
        )
        itemmeta.setLore(lore.asJava)
        itemstack.setItemMeta(itemmeta)
        inventory.setItem(x + 5, itemstack)
      }
    }

    inventory
  }

  private def comma(i: Long): String = {
    NumberFormat.getNumberInstance(Locale.US).format(i)
  }
}
