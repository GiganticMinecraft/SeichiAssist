package com.github.unchama.buildassist.data

import com.github.unchama.buildassist.BuildAssist
import com.github.unchama.buildassist.util.AsyncInventorySetter
import com.github.unchama.seichiassist.util.ItemMetaFactory
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.{ItemMeta, SkullMeta}
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.{Bukkit, Material}

object MenuInventoryData {

  import scala.jdk.CollectionConverters._

  def getSetBlockSkillData(p: Player): Inventory = {
    //プレイヤーを取得
    val player = p.getPlayer
    //UUID取得
    val uuid = player.getUniqueId
    //プレイヤーデータ
    val playerdata = BuildAssist.playermap(uuid) // If NPE, player is already offline

    val inventory = Bukkit.getServer.createInventory(null, 4 * 9, s"$DARK_PURPLE$BOLD「範囲設置スキル」設定画面")
    var itemstack = new ItemStack(Material.BARRIER, 1)
    var itemmeta = Bukkit.getItemFactory.getItemMeta(Material.BARRIER)
    var skullmeta = ItemMetaFactory.SKULL.getValue
    var lore = List(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")

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
    itemmeta.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}元のページへ")
    itemmeta.setLore(lore.asJava)
    itemstack.setItemMeta(itemmeta)
    inventory.setItem(0, itemstack)

    //土設置のON/OFF
    itemstack = new ItemStack(Material.DIRT, 1)
    itemmeta = Bukkit.getItemFactory.getItemMeta(Material.STONE)
    itemmeta.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}設置時に下の空洞を埋める機能")
    lore = List(
      s"$RESET$AQUA${UNDERLINE}機能の使用設定：$ZSDirt",
      s"$RESET$AQUA${UNDERLINE}機能の範囲：地下5マスまで"
    )
    itemmeta.setLore(lore.asJava)
    itemstack.setItemMeta(itemmeta)
    inventory.setItem(4, itemstack)

    //設定状況の表示
    itemstack = new ItemStack(Material.STONE, 1)
    itemmeta = Bukkit.getItemFactory.getItemMeta(Material.STONE)
    itemmeta.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}現在の設定は以下の通りです")
    lore = List(
      s"$RESET$AQUA${UNDERLINE}スキルの使用設定：$ZSSkill",
      s"$RESET$AQUA${UNDERLINE}スキルの範囲設定：$ZSSkillA×$ZSSkillA",
      s"$RESET$AQUA${UNDERLINE}MineStack優先設定:$ZSSkill_Minestack")
    itemmeta.setLore(lore.asJava)
    itemstack.setItemMeta(itemmeta)
    inventory.setItem(13, itemstack)

    //範囲をMAXへ
    itemstack = new ItemStack(Material.SKULL_ITEM, 11)
    itemstack.setDurability(3.toShort)
    skullmeta.setDisplayName(s"$RED$UNDERLINE${BOLD}範囲設定を最大値に変更")
    lore = List(
      s"$RESET${AQUA}現在の範囲設定：$ZSSkillA×$ZSSkillA",
      s"$RESET$AQUA${UNDERLINE}変更後の範囲設定：11×11"
    )
    skullmeta.setLore(lore.asJava)
    skullmeta.setOwner("MHF_ArrowUp")
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 19, itemstack)

    //範囲を一段階増加
    itemstack = new ItemStack(Material.SKULL_ITEM, 7)
    skullmeta = ItemMetaFactory.SKULL.getValue
    itemstack.setDurability(3.toShort)
    skullmeta.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}範囲設定を一段階大きくする")
    lore = List(
      s"$RESET${AQUA}現在の範囲設定：$ZSSkillA×$ZSSkillA",
      s"$RESET$AQUA${UNDERLINE}変更後の範囲設定：${ZSSkillA + 2}×${ZSSkillA + 2}",
      s"$RESET$RED※範囲設定の最大値は11×11※"
    )
    skullmeta.setLore(lore.asJava)
    skullmeta.setOwner("MHF_ArrowUp")
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 20, itemstack)

    //範囲を初期値へ
    itemstack = new ItemStack(Material.SKULL_ITEM, 5)
    skullmeta = ItemMetaFactory.SKULL.getValue
    itemstack.setDurability(3.toShort)
    skullmeta.setDisplayName(s"$RED$UNDERLINE${BOLD}範囲設定を初期値に変更")
    lore = List(
      s"$RESET${AQUA}現在の範囲設定：$ZSSkillA×$ZSSkillA",
      s"$RESET$AQUA${UNDERLINE}変更後の範囲設定：5×5"
    )
    skullmeta.setLore(lore.asJava)
    skullmeta.setOwner("MHF_TNT")
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 22, itemstack)

    //範囲を一段階減少
    itemstack = new ItemStack(Material.SKULL_ITEM, 3)
    skullmeta = ItemMetaFactory.SKULL.getValue
    itemstack.setDurability(3.toShort)
    skullmeta.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}範囲設定を一段階小さくする")
    lore = List(
      s"$RESET${AQUA}現在の範囲設定：$ZSSkillA×$ZSSkillA",
      s"$RESET$AQUA${UNDERLINE}変更後の範囲設定：${ZSSkillA - 2}×${ZSSkillA - 2}",
      s"$RESET$RED※範囲設定の最小値は3×3※"
    )
    skullmeta.setLore(lore.asJava)
    skullmeta.setOwner("MHF_ArrowDown")
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 24, itemstack)


    //範囲をMINへ
    itemstack = new ItemStack(Material.SKULL_ITEM, 1)
    skullmeta = ItemMetaFactory.SKULL.getValue
    itemstack.setDurability(3.toShort)
    skullmeta.setDisplayName(s"$RED$UNDERLINE${BOLD}範囲設定を最小値に変更")
    lore = List(
      s"$RESET${AQUA}現在の範囲設定：$ZSSkillA×$ZSSkillA",
      s"$RESET$AQUA${UNDERLINE}変更後の範囲設定：3×3"
    )
    skullmeta.setLore(lore.asJava)
    skullmeta.setOwner("MHF_ArrowDown")
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 25, itemstack)

    //35番目にMineStack優先設定を追加
    //MineStackの方を優先して消費する設定
    itemstack = new ItemStack(Material.CHEST, 1)
    itemmeta = Bukkit.getItemFactory.getItemMeta(Material.CHEST)
    itemmeta.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}MineStack優先設定：$ZSSkill_Minestack")
    lore = List(
      s"$RESET${GRAY}スキルでブロックを並べるとき",
      s"$RESET${GRAY}MineStackの在庫を優先して消費します。",
      s"$RESET${GRAY}建築LV${BuildAssist.config.getZoneskillMinestacklevel}以上で利用可能",
      s"$RESET${GRAY}クリックで切り替え"
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

    val inventory = Bukkit.getServer.createInventory(null, 4 * 9, s"$DARK_PURPLE$BOLD「ブロックを並べるスキル（仮）」設定")
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
    itemmeta.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}ハーフブロック設定 ：${BuildAssist.line_up_step_str(playerdata.line_up_step_flg)}")
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
    itemmeta.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}破壊設定 ：${BuildAssist.line_up_off_on_str(playerdata.line_up_des_flg)}")
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
}
