package com.github.unchama.buildassist

import cats.effect.{IO, SyncIO}
import com.github.unchama.buildassist.util.AsyncInventorySetter
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.minecraft.JdbcBackedUuidRepository
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.{Bukkit, Material}

object MenuInventoryData {

  import scala.jdk.CollectionConverters._

  JdbcBackedUuidRepository.initializeStaticInstance[SyncIO].unsafeRunSync().apply[SyncIO]

  // ブロックを並べる設定メニュー
  def getBlockLineUpData(
    p: Player
  )(implicit playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]): Inventory = {
    // プレイヤーを取得
    val player = p.getPlayer
    // UUID取得
    val uuid = player.getUniqueId
    // プレイヤーデータ
    val playerdata = BuildAssist.instance.temporaryData(uuid)

    val inventory = Bukkit.getServer.createInventory(null, 4 * 9, s"$DARK_PURPLE$BOLD「直列設置」設定")
    var itemstack = new ItemStack(Material.PLAYER_HEAD, 11)
    var itemmeta: ItemMeta = itemstack.getItemMeta
    var lore = List(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")

    // ホームを開く
    itemstack = new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft)
      .title(s"$YELLOW$UNDERLINE${BOLD}ホームへ")
      .lore(lore)
      .build()
    AsyncInventorySetter.setItemAsync(inventory, 27, itemstack)

    itemstack = new IconItemStackBuilder(Material.OAK_PLANKS)
      .title(
        s"$YELLOW$UNDERLINE${BOLD}直列設置 ：${BuildAssist.line_up_str(playerdata.line_up_flg)}"
      )
      .lore(
        s"$RESET${GRAY}オフハンドに木の棒、メインハンドに設置したいブロックを持って",
        s"$RESET${GRAY}左クリックすると向いてる方向に並べて設置します。",
        s"$RESET${GRAY}建築Lv${BuildAssist.config.getblocklineuplevel}以上で利用可能",
        s"$RESET${GRAY}クリックで切り替え"
      )
      .build()

    inventory.setItem(0, itemstack)

    // 直列設置ハーフブロック設定
    itemstack = new ItemStack(Material.STONE_SLAB, 1)
    itemmeta = itemstack.getItemMeta
    itemmeta.setDisplayName(
      s"$YELLOW$UNDERLINE${BOLD}ハーフブロック設定 ：${BuildAssist.line_up_step_str(playerdata.line_up_step_flg)}"
    )
    lore = List(s"$RESET${GRAY}ハーフブロックを並べる時の位置を決めます。", s"$RESET${GRAY}クリックで切り替え")
    itemmeta.setLore(lore.asJava)
    itemstack.setItemMeta(itemmeta)
    inventory.setItem(1, itemstack)

    // 直列設置一部ブロックを破壊して並べる設定
    itemstack = new ItemStack(Material.TNT, 1)
    itemmeta = Bukkit.getItemFactory.getItemMeta(Material.TNT)
    itemmeta.setDisplayName(
      s"$YELLOW$UNDERLINE${BOLD}破壊設定 ：${BuildAssist.line_up_off_on_str(playerdata.line_up_des_flg)}"
    )
    lore = List(
      s"$RESET${GRAY}ブロックを並べるとき特定のブロックを破壊して並べます。",
      s"$RESET${GRAY}破壊対象ブロック：草、花、水、雪、松明、きのこ、マグマ、ツタ",
      s"$RESET${GRAY}クリックで切り替え"
    )
    itemmeta.setLore(lore.asJava)
    itemstack.setItemMeta(itemmeta)
    inventory.setItem(2, itemstack)

    // MineStackの方を優先して消費する設定
    itemstack = new ItemStack(Material.CHEST, 1)
    itemmeta = Bukkit.getItemFactory.getItemMeta(Material.CHEST)
    itemmeta.setDisplayName(
      s"$YELLOW$UNDERLINE${BOLD}MineStack優先設定 ：${BuildAssist.line_up_off_on_str(playerdata.line_up_minestack_flg)}"
    )
    lore = List(
      s"$RESET${GRAY}スキルでブロックを並べるとき",
      s"$RESET${GRAY}MineStackの在庫を優先して消費します。",
      s"$RESET${GRAY}建築Lv${BuildAssist.config.getblocklineupMinestacklevel}以上で利用可能",
      s"$RESET${GRAY}クリックで切り替え"
    )
    itemmeta.setLore(lore.asJava)
    itemstack.setItemMeta(itemmeta)
    inventory.setItem(8, itemstack)

    inventory
  }
}
