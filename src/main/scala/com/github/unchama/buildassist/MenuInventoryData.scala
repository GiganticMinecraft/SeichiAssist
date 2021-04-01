package com.github.unchama.buildassist

import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.util.AsyncInventorySetter
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.{Bukkit, Material}

object MenuInventoryData {
  //ブロックを並べる設定メニュー
  def getBlockLineUpData(p: Player): Inventory = {
    //プレイヤーを取得
    val player = p.getPlayer
    //UUID取得
    val uuid = player.getUniqueId
    //プレイヤーデータ
    val playerdata = BuildAssist.instance.temporaryData(uuid)

    val inventory = Bukkit.getServer.createInventory(null, 4 * 9, s"$DARK_PURPLE$BOLD「直列設置」設定")

    val mapping = Map(
      // ホームを開く
      27 -> {
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}ホームへ")
          .lore(
            s"$RESET$DARK_RED${UNDERLINE}クリックで移動"
          )
          .build()
      },
      //直列設置設定
      0 -> {
        new IconItemStackBuilder(Material.WOOD)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}直列設置 ：${BuildAssist.lineFillStateDescriptions(playerdata.lineFillStatus)}")
          .lore(
            s"$RESET${GRAY}オフハンドに木の棒、メインハンドに設置したいブロックを持って",
            s"$RESET${GRAY}左クリックすると向いてる方向に並べて設置します。",
            s"$RESET${GRAY}建築Lv${BuildAssist.config.getLineFillUnlockLevel}以上で利用可能",
            s"$RESET${GRAY}クリックで切り替え"
          )
          .build()
      },
      //直列設置ハーフブロック設定
      1 -> {
        new IconItemStackBuilder(Material.STEP)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}ハーフブロック設定 ：${BuildAssist.lineFillSlabPositionDescriptions(playerdata.lineFillSlabPosition)}")
          .lore(
            s"$RESET${GRAY}ハーフブロックを並べる時の位置を決めます。",
            s"$RESET${GRAY}クリックで切り替え"
          )
          .build()
      },
      //直列設置一部ブロックを破壊して並べる設定
      2 -> {
        new IconItemStackBuilder(Material.TNT)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}破壊設定 ：${BuildAssist.asDescription(playerdata.lineFillDestructWeakBlocks)}")
          .lore(
            s"$RESET${GRAY}ブロックを並べるとき特定のブロックを破壊して並べます。",
            s"$RESET${GRAY}破壊対象ブロック：草,花,水,雪,松明,きのこ",
            s"$RESET${GRAY}クリックで切り替え"
          )
          .build()
      },
      //MineStackの方を優先して消費する設定
      8 -> {
        new IconItemStackBuilder(Material.CHEST)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}MineStack優先設定 ：${BuildAssist.asDescription(playerdata.lineFillPrioritizeMineStack)}")
          .lore(
            s"$RESET${GRAY}スキルでブロックを並べるとき",
            s"$RESET${GRAY}MineStackの在庫を優先して消費します。",
            s"$RESET${GRAY}建築Lv${BuildAssist.config.getLineFillFromMineStackUnlockLevel}以上で利用可能",
            s"$RESET${GRAY}クリックで切り替え"
          )
          .build()
      },
    )
    mapping.foreach { case (index, item) =>
      AsyncInventorySetter.setItemAsync(inventory, index, item)
    }
    inventory
  }
}
