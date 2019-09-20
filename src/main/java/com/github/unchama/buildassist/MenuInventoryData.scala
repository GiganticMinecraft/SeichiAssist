package com.github.unchama.buildassist

import com.github.unchama.buildassist.util.AsyncInventorySetter
import com.github.unchama.seichiassist.util.ItemMetaFactory
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.{ItemMeta, SkullMeta}
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.{Bukkit, ChatColor, Material}

object MenuInventoryData {

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
    val player = p.player
    //UUID取得
    val uuid = player.uniqueId
    //プレイヤーデータ
    val playerdata = BuildAssist.playermap[uuid]!! // If NPE, player is already offline

    val inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "「範囲設置スキル」設定画面")
    var itemstack = ItemStack(Material.BARRIER, 1)
    var itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BARRIER)
    var skullmeta = ItemMetaFactory.SKULL.value
    var lore = List(ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動")

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
    itemstack.durability = 3.toShort()
    itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "元のページへ"
    itemmeta.lore = lore
    itemstack.itemMeta = itemmeta
    inventory.setItem(0, itemstack)

    //土設置のON/OFF
    itemstack = ItemStack(Material.DIRT, 1)
    itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE)
    itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "設置時に下の空洞を埋める機能"
    lore = List(ChatColor.RESET.toString() + "" + ChatColor.AQUA + "" + ChatColor.UNDERLINE + "機能の使用設定：" + ZSDirt, ChatColor.RESET.toString() + "" + ChatColor.AQUA + "" + ChatColor.UNDERLINE + "機能の範囲：地下5マスまで")
    itemmeta.lore = lore
    itemstack.itemMeta = itemmeta
    inventory.setItem(4, itemstack)

    //設定状況の表示
    itemstack = ItemStack(Material.STONE, 1)
    itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE)
    itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "現在の設定は以下の通りです"
    lore = List(ChatColor.RESET.toString() + "" + ChatColor.AQUA + "" + ChatColor.UNDERLINE + "スキルの使用設定：" + ZSSkill, ChatColor.RESET.toString() + "" + ChatColor.AQUA + "" + ChatColor.UNDERLINE + "スキルの範囲設定：" + ZSSkillA + "×" + ZSSkillA, ChatColor.RESET.toString() + "" + ChatColor.AQUA + "" + ChatColor.UNDERLINE + "MineStack優先設定:" + ZSSkill_Minestack)
    itemmeta.lore = lore
    itemstack.itemMeta = itemmeta
    inventory.setItem(13, itemstack)

    //範囲をMAXへ
    itemstack = ItemStack(Material.SKULL_ITEM, 11)
    itemstack.durability = 3.toShort()
    skullmeta.displayName = ChatColor.RED.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "範囲設定を最大値に変更"
    lore = List(ChatColor.RESET.toString() + "" + ChatColor.AQUA + "" + "現在の範囲設定：" + ZSSkillA + "×" + ZSSkillA, ChatColor.RESET.toString() + "" + ChatColor.AQUA + "" + ChatColor.UNDERLINE + "変更後の範囲設定：11×11")
    skullmeta.lore = lore
    skullmeta.owner = "MHF_ArrowUp"
    itemstack.itemMeta = skullmeta
    AsyncInventorySetter.setItemAsync(inventory, 19, itemstack)

    //範囲を一段階増加
    itemstack = ItemStack(Material.SKULL_ITEM, 7)
    skullmeta = ItemMetaFactory.SKULL.value
    itemstack.durability = 3.toShort()
    skullmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "範囲設定を一段階大きくする"
    lore = List(ChatColor.RESET.toString() + "" + ChatColor.AQUA + "" + "現在の範囲設定：" + ZSSkillA + "×" + ZSSkillA, ChatColor.RESET.toString() + "" + ChatColor.AQUA + "" + ChatColor.UNDERLINE + "変更後の範囲設定：" + (ZSSkillA + 2) + "×" + (ZSSkillA + 2), ChatColor.RESET.toString() + "" + ChatColor.RED + "" + "※範囲設定の最大値は11×11※")
    skullmeta.lore = lore
    skullmeta.owner = "MHF_ArrowUp"
    itemstack.itemMeta = skullmeta
    AsyncInventorySetter.setItemAsync(inventory, 20, itemstack)

    //範囲を初期値へ
    itemstack = ItemStack(Material.SKULL_ITEM, 5)
    skullmeta = ItemMetaFactory.SKULL.value
    itemstack.durability = 3.toShort()
    skullmeta.displayName = ChatColor.RED.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "範囲設定を初期値に変更"
    lore = List(ChatColor.RESET.toString() + "" + ChatColor.AQUA + "" + "現在の範囲設定：" + ZSSkillA + "×" + ZSSkillA, ChatColor.RESET.toString() + "" + ChatColor.AQUA + "" + ChatColor.UNDERLINE + "変更後の範囲設定：5×5")
    skullmeta.lore = lore
    skullmeta.owner = "MHF_TNT"
    itemstack.itemMeta = skullmeta
    AsyncInventorySetter.setItemAsync(inventory, 22, itemstack)

    //範囲を一段階減少
    itemstack = ItemStack(Material.SKULL_ITEM, 3)
    skullmeta = ItemMetaFactory.SKULL.value
    itemstack.durability = 3.toShort()
    skullmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "範囲設定を一段階小さくする"
    lore = List(ChatColor.RESET.toString() + "" + ChatColor.AQUA + "" + "現在の範囲設定：" + ZSSkillA + "×" + ZSSkillA, ChatColor.RESET.toString() + "" + ChatColor.AQUA + "" + ChatColor.UNDERLINE + "変更後の範囲設定：" + (ZSSkillA - 2) + "×" + (ZSSkillA - 2), ChatColor.RESET.toString() + "" + ChatColor.RED + "" + "※範囲設定の最小値は3×3※")
    skullmeta.lore = lore
    skullmeta.owner = "MHF_ArrowDown"
    itemstack.itemMeta = skullmeta
    AsyncInventorySetter.setItemAsync(inventory, 24, itemstack)


    //範囲をMINへ
    itemstack = ItemStack(Material.SKULL_ITEM, 1)
    skullmeta = ItemMetaFactory.SKULL.value
    itemstack.durability = 3.toShort()
    skullmeta.displayName = ChatColor.RED.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "範囲設定を最小値に変更"
    lore = List(ChatColor.RESET.toString() + "" + ChatColor.AQUA + "" + "現在の範囲設定：" + ZSSkillA + "×" + ZSSkillA, ChatColor.RESET.toString() + "" + ChatColor.AQUA + "" + ChatColor.UNDERLINE + "変更後の範囲設定：3×3")
    skullmeta.lore = lore
    skullmeta.owner = "MHF_ArrowDown"
    itemstack.itemMeta = skullmeta
    AsyncInventorySetter.setItemAsync(inventory, 25, itemstack)

    //35番目にMineStack優先設定を追加
    //MineStackの方を優先して消費する設定
    itemstack = ItemStack(Material.CHEST, 1)
    itemmeta = Bukkit.getItemFactory().getItemMeta(Material.CHEST)
    itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "MineStack優先設定：" + ZSSkill_Minestack
    lore = List(ChatColor.RESET.toString() + "" + ChatColor.GRAY + "スキルでブロックを並べるとき", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "MineStackの在庫を優先して消費します。", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.zoneskillMinestacklevel + "以上で利用可能", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "クリックで切り替え"
    )
    itemmeta.lore = lore
    itemstack.itemMeta = itemmeta
    inventory.setItem(35, itemstack)

    return inventory
  }

  //ブロックを並べる設定メニュー
  def getBlockLineUpData(p: Player): Inventory = {
    //プレイヤーを取得
    val player = p.player
    //UUID取得
    val uuid = player.uniqueId
    //プレイヤーデータ
    val playerdata = BuildAssist.playermap[uuid]!!

    val inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "「ブロックを並べるスキル（仮）」設定")
    var itemstack = ItemStack(Material.SKULL_ITEM, 1)
    var itemmeta: ItemMeta = Bukkit.getItemFactory().getItemMeta(Material.WOOD)
    val skullmeta: SkullMeta = ItemMetaFactory.SKULL.value
    var lore = List(s"${ChatColor.RESET}${ChatColor.DARK_RED}${ChatColor.UNDERLINE}クリックで移動")

    // ホームを開く
    itemstack.durability = 3.toShort()
    skullmeta.displayName = s"${ChatColor.YELLOW}${ChatColor.UNDERLINE}${ChatColor.BOLD}ホームへ"
    skullmeta.lore = lore
    skullmeta.owner = "MHF_ArrowLeft"
    itemstack.itemMeta = skullmeta
    AsyncInventorySetter.setItemAsync(inventory, 27, itemstack)

    //ブロックを並べるスキル設定
    itemstack = ItemStack(Material.WOOD, 1)
    itemmeta.displayName = s"${ChatColor.YELLOW}${ChatColor.UNDERLINE}${ChatColor.BOLD}ブロックを並べるスキル（仮） ：${BuildAssist.line_up_str[playerdata.line_up_flg]}"
    lore = List(
        s"${ChatColor.RESET}${ChatColor.GRAY}オフハンドに木の棒、メインハンドに設置したいブロックを持って",
        s"${ChatColor.RESET}${ChatColor.GRAY}左クリックすると向いてる方向に並べて設置します。",
        s"${ChatColor.RESET}${ChatColor.GRAY}建築LV${BuildAssist.config.getblocklineuplevel()}以上で利用可能",
        s"${ChatColor.RESET}${ChatColor.GRAY}クリックで切り替え"
    )
    itemmeta.lore = lore
    itemstack.itemMeta = itemmeta
    inventory.setItem(0, itemstack)

    //ブロックを並べるスキルハーフブロック設定
    itemstack = ItemStack(Material.STEP, 1)
    itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STEP)
    itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ハーフブロック設定 ：" + BuildAssist.line_up_step_str[playerdata.line_up_step_flg]
    lore = List(
        s"${ChatColor.RESET}${ChatColor.GRAY}ハーフブロックを並べる時の位置を決めます。",
        s"${ChatColor.RESET}${ChatColor.GRAY}クリックで切り替え"
    )
    itemmeta.lore = lore
    itemstack.itemMeta = itemmeta
    inventory.setItem(1, itemstack)

    //ブロックを並べるスキル一部ブロックを破壊して並べる設定
    itemstack = ItemStack(Material.TNT, 1)
    itemmeta = Bukkit.getItemFactory().getItemMeta(Material.TNT)
    itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "破壊設定 ：" + BuildAssist.line_up_off_on_str[playerdata.line_up_des_flg]
    lore = List(
        s"${ChatColor.RESET}${ChatColor.GRAY}ブロックを並べるとき特定のブロックを破壊して並べます。",
        s"${ChatColor.RESET}${ChatColor.GRAY}破壊対象ブロック：草,花,水,雪,松明,きのこ",
        s"${ChatColor.RESET}${ChatColor.GRAY}クリックで切り替え"
    )
    itemmeta.lore = lore
    itemstack.itemMeta = itemmeta
    inventory.setItem(2, itemstack)

    //MineStackの方を優先して消費する設定
    itemstack = ItemStack(Material.CHEST, 1)
    itemmeta = Bukkit.getItemFactory().getItemMeta(Material.CHEST)
    itemmeta.displayName = s"${ChatColor.YELLOW}${ChatColor.UNDERLINE}${ChatColor.BOLD}MineStack優先設定 ：${BuildAssist.line_up_off_on_str[playerdata.line_up_minestack_flg]}"
    lore = List(
        s"${ChatColor.RESET}${ChatColor.GRAY}スキルでブロックを並べるとき",
        s"${ChatColor.RESET}${ChatColor.GRAY}MineStackの在庫を優先して消費します。",
        s"${ChatColor.RESET}${ChatColor.GRAY}建築LV${BuildAssist.config.getblocklineupMinestacklevel()}以上で利用可能",
        s"${ChatColor.RESET}${ChatColor.GRAY}クリックで切り替え"
    )
    itemmeta.lore = lore
    itemstack.itemMeta = itemmeta
    inventory.setItem(8, itemstack)

    return inventory
  }


  //MineStackブロック一括クラフトメニュー
  def getBlockCraftData(p: Player): Inventory = {
    //プレイヤーを取得
    val player = p.player
    //UUID取得
    val uuid = player.uniqueId
    val playerdata_s = SeichiAssist.playermap[uuid]!! // if thrown NPE, player has already left

    val inventory = Bukkit.getServer().createInventory(null, 6 * 9, ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "MineStackブロック一括クラフト1")
    var itemstack: ItemStack
    var itemmeta: ItemMeta
    var skullmeta = ItemMetaFactory.SKULL.value
    var lore: List[String]

    // ホーム目を開く
    itemstack = ItemStack(Material.SKULL_ITEM, 1, 3.toShort())
    skullmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ"
    lore = ImmutableListFactory.of(ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動")
    skullmeta.lore = lore
    skullmeta.owner = "MHF_ArrowLeft"
    itemstack.itemMeta = skullmeta
    AsyncInventorySetter.setItemAsync(inventory, 45, itemstack)

    // 2ページ目を開く
    itemstack = ItemStack(Material.SKULL_ITEM, 1, 3.toShort())
    skullmeta = ItemMetaFactory.SKULL.value
    skullmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "2ページ目へ"
    lore = List(s"${ChatColor.RESET}${ChatColor.DARK_RED}${ChatColor.UNDERLINE}クリックで移動")
    skullmeta.lore = lore
    skullmeta.owner = "MHF_ArrowDown"
    itemstack.itemMeta = skullmeta
    AsyncInventorySetter.setItemAsync(inventory, 53, itemstack)

    //石を石ハーフブロックに変換10～10万
    var num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("stone")!!)
    var num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("step0")!!)
    for (x in 1..5) {
      val p10 = power10[x]
      itemstack = ItemStack(Material.STEP, x)
      itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STEP)
      itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "石を石ハーフブロックに変換します"
      lore = List(
          s"${ChatColor.RESET}${ChatColor.GRAY}石${p10}個→石ハーフブロック${p10 * 2}個",
          s"${ChatColor.RESET}${ChatColor.GRAY}石の数:${comma(num_1)}",
          s"${ChatColor.RESET}${ChatColor.GRAY}石ハーフブロックの数:${comma(num_2)}", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(1) + "以上で利用可能", ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
      )
      itemmeta.lore = lore
      itemstack.itemMeta = itemmeta
      inventory.setItem(x - 1, itemstack)
    }


    //石を石レンガに変換10～10万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("stone")!!)
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("smooth_brick0")!!)
    for (x in 1..5) {
      val p10 = power10[x]
      itemstack = ItemStack(Material.SMOOTH_BRICK, x)
      itemmeta = Bukkit.getItemFactory().getItemMeta(Material.SMOOTH_BRICK)
      itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "石を石レンガに変換します"
      lore = List(ChatColor.RESET.toString() + "" + ChatColor.GRAY + "石" + p10 + "個→石レンガ" + p10 + "個", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "石の数:" + comma(num_1), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "石レンガの数:" + comma(num_2), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(1) + "以上で利用可能", ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
      )
      itemmeta.lore = lore
      itemstack.itemMeta = itemmeta
      inventory.setItem(x + 8, itemstack)
    }

    //花崗岩を磨かれた花崗岩に変換10～1万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("granite")!!)
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("polished_granite")!!)
    for (x in 1..4) {
      val p10 = power10[x]
      itemstack = ItemStack(Material.STONE, x, 2.toShort())
      itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE)
      itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "花崗岩を磨かれた花崗岩に変換します"
      lore = List(ChatColor.RESET.toString() + "" + ChatColor.GRAY + "花崗岩" + p10 + "個→磨かれた花崗岩" + p10 + "個", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "花崗岩の数:" + comma(num_1), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "磨かれた花崗岩の数:" + comma(num_2), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能", ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
      )
      itemmeta.lore = lore
      itemstack.itemMeta = itemmeta
      inventory.setItem(x + 17, itemstack)
    }

    //閃緑岩を磨かれた閃緑岩に変換10～1万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("diorite")!!)
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("polished_diorite")!!)
    for (x in 1..4) {
      val p10 = power10[x]
      itemstack = ItemStack(Material.STONE, x, 4.toShort())
      itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE)
      itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "閃緑岩を磨かれた閃緑岩に変換します"
      lore = List(ChatColor.RESET.toString() + "" + ChatColor.GRAY + "閃緑岩" + p10 + "個→磨かれた閃緑岩" + p10 + "個", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "閃緑岩の数:" + comma(num_1), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "磨かれた閃緑岩の数:" + comma(num_2), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能", ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
      )
      itemmeta.lore = lore
      itemstack.itemMeta = itemmeta
      inventory.setItem(x + 22, itemstack)
    }

    //安山岩を磨かれた安山岩に変換10～1万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("andesite")!!)
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("polished_andesite")!!)
    for (x in 1..4) {
      val p10 = power10[x]
      itemstack = ItemStack(Material.STONE, x, 6.toShort())
      itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE)
      itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "安山岩を磨かれた安山岩に変換します"
      lore = List(ChatColor.RESET.toString() + "" + ChatColor.GRAY + "安山岩" + p10 + "個→磨かれた安山岩" + p10 + "個", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "安山岩の数:" + comma(num_1), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "磨かれた安山岩の数:" + comma(num_2), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能", ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
      )
      itemmeta.lore = lore
      itemstack.itemMeta = itemmeta
      inventory.setItem(x + 26, itemstack)
    }

    //ネザー水晶をネザー水晶ブロックに変換10～1万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("quartz")!!)
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("quartz_block")!!)
    for (x in 1..4) {
      val p10 = power10[x]
      itemstack = ItemStack(Material.QUARTZ_BLOCK, x)
      itemmeta = Bukkit.getItemFactory().getItemMeta(Material.QUARTZ_BLOCK)
      itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ネザー水晶をネザー水晶ブロックに変換します"
      lore = List(ChatColor.RESET.toString() + "" + ChatColor.GRAY + "ネザー水晶" + p10 * 4 + "個→ネザー水晶ブロック" + p10 + "個", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "ネザー水晶の数:" + comma(num_1), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "ネザー水晶ブロックの数:" + comma(num_2), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能", ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
      )
      itemmeta.lore = lore
      itemstack.itemMeta = itemmeta
      inventory.setItem(x + 31, itemstack)
    }

    //レンガをレンガブロックに変換10～1万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("brick_item")!!)
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("brick")!!)
    for (x in 1..4) {
      val p10 = power10[x]
      itemstack = ItemStack(Material.BRICK, x)
      itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BRICK)
      itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "レンガをレンガブロックに変換します"
      lore = List(ChatColor.RESET.toString() + "" + ChatColor.GRAY + "レンガ" + p10 * 4 + "個→レンガブロック" + p10 + "個", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "レンガの数:" + comma(num_1), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "レンガブロックの数:" + comma(num_2), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能", ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
      )
      itemmeta.lore = lore
      itemstack.itemMeta = itemmeta
      inventory.setItem(x + 35, itemstack)
    }

    //ネザーレンガをネザーレンガブロックに変換10～1万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("nether_brick_item")!!)
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("nether_brick")!!)
    for (x in 1..4) {
      val p10 = power10[x]
      itemstack = ItemStack(Material.NETHER_BRICK, x)
      itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHER_BRICK)
      itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ネザーレンガをネザーレンガブロックに変換します"
      lore = List(
          s"${ChatColor.RESET}${ChatColor.GRAY}ネザーレンガ${p10 * 4}個→ネザーレンガブロック${p10}個",
          s"${ChatColor.RESET}${ChatColor.GRAY}ネザーレンガの数:${comma(num_1)}",
          s"${ChatColor.RESET}${ChatColor.GRAY}ネザーレンガブロックの数:${comma(num_2)}",
          s"${ChatColor.RESET}${ChatColor.GRAY}建築LV${BuildAssist.config.getMinestackBlockCraftlevel(2)}以上で利用可能",
          s"${ChatColor.RESET}${ChatColor.DARK_RED}${ChatColor.UNDERLINE}クリックで変換"
      )
      itemmeta.lore = lore
      itemstack.itemMeta = itemmeta
      inventory.setItem(x + 40, itemstack)
    }
    return inventory
  }

  //MineStackブロック一括クラフトメニュー2
  def getBlockCraftData2(p: Player): Inventory = {
    //プレイヤーを取得
    val player = p.player
    //UUID取得
    val uuid = player.uniqueId
    //プレイヤーデータ
    val playerdata_s = SeichiAssist.playermap[uuid]!!

    val inventory = Bukkit.getServer().createInventory(null, 6 * 9, ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "MineStackブロック一括クラフト2")
    var itemstack: ItemStack
    var itemmeta: ItemMeta
    var skullmeta: SkullMeta = ItemMetaFactory.SKULL.value
    var lore: List[String]

    // 1ページ目を開く
    itemstack = ItemStack(Material.SKULL_ITEM, 1, 3)
    skullmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "1ページ目へ"
    lore = ImmutableListFactory.of(ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動")
    skullmeta.lore = lore
    skullmeta.owner = "MHF_ArrowUp"
    itemstack.itemMeta = skullmeta
    AsyncInventorySetter.setItemAsync(inventory, 45, itemstack)

    // 3ページ目を開く
    itemstack = ItemStack(Material.SKULL_ITEM, 1, 3)
    skullmeta = ItemMetaFactory.SKULL.value
    skullmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "3ページ目へ"
    lore = ImmutableListFactory.of(ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動")
    skullmeta.lore = lore
    skullmeta.owner = "MHF_ArrowDown"
    itemstack.itemMeta = skullmeta
    AsyncInventorySetter.setItemAsync(inventory, 53, itemstack)

    //雪玉を雪（ブロック）に変換10～1万
    var num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("snow_ball")!!)
    var num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("snow_block")!!)
    var num_3: Long
    for (x in 1..4) {
      val p10 = power10[x]
      itemstack = ItemStack(Material.SNOW_BLOCK, x)
      itemmeta = Bukkit.getItemFactory().getItemMeta(Material.SNOW_BLOCK)
      itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "雪玉を雪（ブロック）に変換します"
      lore = ImmutableListFactory.of(ChatColor.RESET.toString() + "" + ChatColor.GRAY + "雪玉" + p10 * 4 + "個→雪（ブロック）" + p10 + "個", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "雪玉の数:" + comma(num_1), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "雪（ブロック）の数:" + comma(num_2), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能", ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
      )
      itemmeta.lore = lore
      itemstack.itemMeta = itemmeta
      inventory.setItem(x - 1, itemstack)
    }


    //ネザーウォートとネザーレンガを赤いネザーレンガに変換10～10万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("nether_stalk")!!)
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("red_nether_brick")!!)
    num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("nether_brick_item")!!)
    for (x in 1..5) {
      val p10 = power10[x]
      itemstack = ItemStack(Material.RED_NETHER_BRICK, x)
      itemmeta = Bukkit.getItemFactory().getItemMeta(Material.RED_NETHER_BRICK)
      itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ネザーウォートとネザーレンガを赤いネザーレンガに変換します"
      lore = List(ChatColor.RESET.toString() + "" + ChatColor.GRAY + "ネザーウォート" + p10 * 2 + "個+ネザーレンガ" + p10 * 2 + "個→赤いネザーレンガ" + p10 + "個", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "ネザーウォートの数:" + comma(num_1), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "ネザーレンガの数:" + comma(num_3), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "赤いネザーレンガの数:" + comma(num_2), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能", ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
      )
      itemmeta.lore = lore
      itemstack.itemMeta = itemmeta
      inventory.setItem(x + 4, itemstack)
    }

    //石炭を消費して鉄鉱石を鉄インゴットに変換4～4000
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("iron_ore")!!)
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("iron_ingot")!!)
    num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("coal")!!)
    for (x in 0..3) {
      val p10 = power10[x]
      itemstack = ItemStack(Material.IRON_INGOT, x)
      itemmeta = Bukkit.getItemFactory().getItemMeta(Material.IRON_INGOT)
      itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "石炭を消費して鉄鉱石を鉄インゴットに変換します"
      lore = ImmutableListFactory.of(ChatColor.RESET.toString() + "" + ChatColor.GRAY + "鉄鉱石" + p10 * 4 + "個+石炭" + p10 + "個→鉄インゴット" + p10 * 4 + "個", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "鉄鉱石の数:" + comma(num_1), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "石炭の数:" + comma(num_3), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "鉄インゴットの数:" + comma(num_2), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能", ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
      )
      itemmeta.lore = lore
      itemstack.itemMeta = itemmeta
      inventory.setItem(x + 9, itemstack)
    }

    //溶岩バケツを消費して鉄鉱石を鉄インゴットに変換50～5万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("iron_ore")!!)
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("iron_ingot")!!)
    num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("lava_bucket")!!)
    for (x in 0..3) {
      val p10 = power10[x]
      itemstack = ItemStack(Material.IRON_INGOT, x)
      itemmeta = Bukkit.getItemFactory().getItemMeta(Material.IRON_INGOT)
      itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "溶岩バケツを消費して鉄鉱石を鉄インゴットに変換します"
      lore = ImmutableListFactory.of(ChatColor.RESET.toString() + "" + ChatColor.GRAY + "鉄鉱石" + p10 * 50 + "個+溶岩バケツ" + p10 + "個→鉄インゴット" + p10 * 50 + "個", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "鉄鉱石の数:" + comma(num_1), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "溶岩バケツの数:" + comma(num_3), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "鉄インゴットの数:" + comma(num_2), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能", ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
      )
      itemmeta.lore = lore
      itemstack.itemMeta = itemmeta
      inventory.setItem(x + 14, itemstack)
    }

    //石炭を消費して金鉱石を金インゴットに変換4～4000
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("gold_ore")!!)
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("gold_ingot")!!)
    num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("coal")!!)
    for (x in 0..3) {
      val p10 = power10[x]
      itemstack = ItemStack(Material.GOLD_INGOT, x)
      itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLD_INGOT)
      itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "石炭を消費して金鉱石を金インゴットに変換します"
      lore = ImmutableListFactory.of(ChatColor.RESET.toString() + "" + ChatColor.GRAY + "金鉱石" + p10 * 4 + "個+石炭" + p10 + "個→金インゴット" + p10 * 4 + "個", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "金鉱石の数:" + comma(num_1), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "石炭の数:" + comma(num_3), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "金インゴットの数:" + comma(num_2), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能", ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
      )
      itemmeta.lore = lore
      itemstack.itemMeta = itemmeta
      inventory.setItem(x + 18, itemstack)
    }

    //溶岩バケツを消費して金鉱石を金インゴットに変換50～5万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("gold_ore")!!)
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("gold_ingot")!!)
    num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("lava_bucket")!!)
    for (x in 0..3) {
      val p10 = power10[x]
      itemstack = ItemStack(Material.GOLD_INGOT, x)
      itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLD_INGOT)
      itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "溶岩バケツを消費して金鉱石を金インゴットに変換します"
      lore = ImmutableListFactory.of(ChatColor.RESET.toString() + "" + ChatColor.GRAY + "金鉱石" + p10 * 50 + "個+溶岩バケツ" + p10 + "個→金インゴット" + p10 * 50 + "個", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "金鉱石の数:" + comma(num_1), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "溶岩バケツの数:" + comma(num_3), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "金インゴットの数:" + comma(num_2), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能", ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
      )
      itemmeta.lore = lore
      itemstack.itemMeta = itemmeta
      inventory.setItem(x + 23, itemstack)
    }

    //石炭を消費して砂をガラスに変換4～4000
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("sand")!!)
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("glass")!!)
    num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("coal")!!)
    for (x in 0..3) {
      val p10 = power10[x]
      itemstack = ItemStack(Material.GLASS, x)
      itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GLASS)
      itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "石炭を消費して砂をガラスに変換します"
      lore = ImmutableListFactory.of(ChatColor.RESET.toString() + "" + ChatColor.GRAY + "砂" + p10 * 4 + "個+石炭" + p10 + "個→ガラス" + p10 * 4 + "個", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "砂の数:" + comma(num_1), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "石炭の数:" + comma(num_3), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "ガラスの数:" + comma(num_2), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能", ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
      )
      itemmeta.lore = lore
      itemstack.itemMeta = itemmeta
      inventory.setItem(x + 27, itemstack)
    }

    //溶岩バケツを消費して砂をガラスに変換50～5万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("sand")!!)
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("glass")!!)
    num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("lava_bucket")!!)
    for (x in 0..3) {
      val p10 = power10[x]
      itemstack = ItemStack(Material.GLASS, x)
      itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GLASS)
      itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "溶岩バケツを消費して砂をガラスに変換します"
      lore = ImmutableListFactory.of(ChatColor.RESET.toString() + "" + ChatColor.GRAY + "砂" + p10 * 50 + "個+溶岩バケツ" + p10 + "個→ガラス" + p10 * 50 + "個", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "砂の数:" + comma(num_1), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "溶岩バケツの数:" + comma(num_3), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "ガラスの数:" + comma(num_2), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能", ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
      )
      itemmeta.lore = lore
      itemstack.itemMeta = itemmeta
      inventory.setItem(x + 32, itemstack)
    }

    //石炭を消費してネザーラックをネザーレンガに変換4～4000
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("netherrack")!!)
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("nether_brick_item")!!)
    num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("coal")!!)
    for (x in 0..3) {
      val p10 = power10[x]
      itemstack = ItemStack(Material.NETHER_BRICK_ITEM, x)
      itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHER_BRICK_ITEM)
      itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "石炭を消費してネザーラックをネザーレンガに変換します"
      lore = ImmutableListFactory.of(ChatColor.RESET.toString() + "" + ChatColor.GRAY + "ネザーラック" + p10 * 4 + "個+石炭" + p10 + "個→ネザーレンガ" + p10 * 4 + "個", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "ネザーラックの数:" + comma(num_1), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "石炭の数:" + comma(num_3), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "ネザーレンガの数:" + comma(num_2), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能", ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
      )
      itemmeta.lore = lore
      itemstack.itemMeta = itemmeta
      inventory.setItem(x + 36, itemstack)
    }

    //溶岩バケツを消費してネザーラックをネザーレンガに変換50～5万
    num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("netherrack")!!)
    num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("nether_brick_item")!!)
    num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("lava_bucket")!!)
    for (x in 0..3) {
      val p10 = power10[x]
      itemstack = ItemStack(Material.NETHER_BRICK_ITEM, x)
      itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHER_BRICK_ITEM)
      itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "溶岩バケツを消費してネザーラックをネザーレンガに変換します"
      lore = ImmutableListFactory.of(ChatColor.RESET.toString() + "" + ChatColor.GRAY + "ネザーラック" + p10 * 50 + "個+溶岩バケツ" + p10 + "個→ネザーレンガ" + p10 * 50 + "個", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "ネザーラックの数:" + comma(num_1), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "溶岩バケツの数:" + comma(num_3), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "ネザーレンガの数:" + comma(num_2), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能", ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
      )
      itemmeta.lore = lore
      itemstack.itemMeta = itemmeta
      inventory.setItem(x + 41, itemstack)
    }
    return inventory
  }

  //MineStackブロック一括クラフトメニュー3
  def getBlockCraftData3(p: Player): Inventory = {
    //プレイヤーを取得
    val player = p.player
    //UUID取得
    val uuid = player.uniqueId
    val playerdata_s = SeichiAssist.playermap[uuid]!!

    val inventory = Bukkit.getServer().createInventory(null, 6 * 9, ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "MineStackブロック一括クラフト3")
    var itemstack: ItemStack
    var itemmeta: ItemMeta
    val skullmeta: SkullMeta = ItemMetaFactory.SKULL.value
    var lore: List[String]

    // 2ページ目を開く
    itemstack = ItemStack(Material.SKULL_ITEM, 1, 3.toShort())
    skullmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "2ページ目へ"
    lore = ImmutableListFactory.of(ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動")
    skullmeta.lore = lore
    skullmeta.owner = "MHF_ArrowUp"
    itemstack.itemMeta = skullmeta
    AsyncInventorySetter.setItemAsync(inventory, 45, itemstack)

    //石炭を消費して粘土をレンガに変換4～4000
    run {
      val num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("clay_ball")!!)
      val num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("brick_item")!!)
      val num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("coal")!!)
      for (x in 0..3) {
        val p10 = power10[x]
        itemstack = ItemStack(Material.CLAY_BRICK, x)
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.CLAY_BRICK)
        itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "石炭を消費して粘土をレンガに変換します"
        lore = List(s"${ChatColor.RESET}${ChatColor.GRAY}粘土${p10 * 4}個+石炭${p10}個→レンガ${p10 * 4}個", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "粘土の数:" + comma(num_1), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "石炭の数:" + comma(num_3), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "レンガの数:" + comma(num_2), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能", ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
        )
        itemmeta.lore = lore
        itemstack.itemMeta = itemmeta
        inventory.setItem(x, itemstack)
      }
    }

    //溶岩バケツを消費して粘土をレンガに変換50～5万
    run {
      val num_1 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("clay_ball")!!)
      val num_2 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("brick_item")!!)
      val num_3 = playerdata_s.minestack.getStackedAmountOf(Util.findMineStackObjectByName("lava_bucket")!!)
      for (x in 0..3) {
        val p10 = power10[x]
        itemstack = ItemStack(Material.CLAY_BRICK, x)
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.CLAY_BRICK)
        itemmeta.displayName = ChatColor.YELLOW.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "溶岩バケツを消費して粘土をレンガに変換します"
        lore = ImmutableListFactory.of(ChatColor.RESET.toString() + "" + ChatColor.GRAY + "粘土" + p10 * 50 + "個+溶岩バケツ" + p10 + "個→レンガ" + p10 * 50 + "個", ChatColor.RESET.toString() + "" + ChatColor.GRAY + "粘土の数:" + comma(num_1), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "溶岩バケツの数:" + comma(num_3), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "レンガの数:" + comma(num_2), ChatColor.RESET.toString() + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能", ChatColor.RESET.toString() + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
        )
        itemmeta.lore = lore
        itemstack.itemMeta = itemmeta
        inventory.setItem(x + 5, itemstack)
      }
    }

    return inventory
  }

  private def comma(i: Long): String = {
    return NumberFormat.getNumberInstance(Locale.US).format(i)
  }
}
