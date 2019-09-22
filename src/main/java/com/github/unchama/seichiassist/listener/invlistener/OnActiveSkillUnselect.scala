package com.github.unchama.seichiassist.listener.invlistener

import com.github.unchama.seichiassist.data.ActiveSkillInventoryData
import com.github.unchama.seichiassist.{ActiveSkill, SeichiAssist}
import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver
import org.bukkit.ChatColor._
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.event.Listener
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryType}
import org.bukkit.{Material, Sound}

object OnActiveSkillUnselect extends Listener {
  def onPlayerClickActiveSkillReleaseEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.getClickedInventory == null) {
      return
    }

    val itemstackcurrent = event.getCurrentItem
    val view = event.getView
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType !== EntityType.PLAYER) {
      return
    }

    val topinventory = view.getTopInventory.ifNull { return }
    //インベントリが存在しない時終了
    //インベントリサイズが36でない時終了
    if (topinventory.row != 5) {
      return
    }
    val player = he.asInstanceOf[Player]
    val uuid = player.uniqueId
    val playerdata = SeichiAssist.playermap(uuid)

    //インベントリ名が以下の時処理
    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "整地スキル選択") {
      event.setCancelled(true)
      //プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType === InventoryType.PLAYER) {
        return
      }
      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      if (itemstackcurrent.getType === Material.BEDROCK) {
        val itemmeta = itemstackcurrent.itemMeta
        val skilllevel: Int
        val skilltype: Int
        val name = itemmeta.displayName
        if (name.contains("エビフライ・ドライブ")) {
          skilllevel = 4
          skilltype = 1
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.5.toFloat())
          } else if (playerdata.activeskilldata.breakskill < 3) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(3, 3) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.5.toFloat())
          } else {
            playerdata.activeskilldata.arrowskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("ホーリー・ショット")) {
          skilllevel = 5
          skilltype = 1
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.arrowskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.arrowskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("ツァーリ・ボンバ")) {
          skilllevel = 6
          skilltype = 1
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.arrowskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.arrowskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("アーク・ブラスト")) {
          skilllevel = 7
          skilltype = 1
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.arrowskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.arrowskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("ファンタズム・レイ")) {
          skilllevel = 8
          skilltype = 1
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.arrowskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.arrowskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("スーパー・ノヴァ")) {
          skilllevel = 9
          skilltype = 1
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.arrowskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.arrowskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            if (playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.breakskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9) {
              player.sendMessage(YELLOW.toString() + "" + BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました")
              Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1f, 1.2.toFloat())
              Util.sendEveryMessage(GOLD.toString() + "" + BOLD + playerdata.lowercaseName + "が全てのスキルを習得し、アサルトアーマーを解除しました！")
            }
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("トム・ボウイ")) {
          skilllevel = 4
          skilltype = 2
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < 3) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(3, 3) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.multiskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("サンダー・ストーム")) {
          skilllevel = 5
          skilltype = 2
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.multiskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.multiskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("スターライト・ブレイカー")) {
          skilllevel = 6
          skilltype = 2
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.multiskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.multiskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("アース・ディバイド")) {
          skilllevel = 7
          skilltype = 2
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.multiskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.multiskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("ヘヴン・ゲイボルグ")) {
          skilllevel = 8
          skilltype = 2
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.multiskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.multiskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("ディシジョン")) {
          skilllevel = 9
          skilltype = 2
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.multiskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.multiskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            if (playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.breakskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9) {
              player.sendMessage(YELLOW.toString() + "" + BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました")
              Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1f, 1.2.toFloat())
              Util.sendEveryMessage(GOLD.toString() + "" + BOLD + playerdata.lowercaseName + "が全てのスキルを習得し、アサルトアーマーを解除しました！")
            }
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("デュアル・ブレイク")) {
          skilllevel = 1
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("トリアル・ブレイク")) {
          skilllevel = 2
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("エクスプロージョン")) {
          skilllevel = 3
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("ミラージュ・フレア")) {
          skilllevel = 4
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("ドッ・カーン")) {
          skilllevel = 5
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("ギガンティック・ボム")) {
          skilllevel = 6
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("ブリリアント・デトネーション")) {
          skilllevel = 7
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("レムリア・インパクト")) {
          skilllevel = 8
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("エターナル・ヴァイス")) {
          skilllevel = 9
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            if (playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9) {
              player.sendMessage(YELLOW.toString() + "" + BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました")
              Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1f, 1.2.toFloat())
              Util.sendEveryMessage(GOLD.toString() + "" + BOLD + playerdata.lowercaseName + "が全てのスキルを習得し、アサルトアーマーを解除しました！")
            }
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("ホワイト・ブレス")) {
          skilllevel = 7
          skilltype = 4
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < 3) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(3, 3) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.watercondenskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("アブソリュート・ゼロ")) {
          skilllevel = 8
          skilltype = 4
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.watercondenskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.watercondenskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("ダイアモンド・ダスト")) {
          skilllevel = 9
          skilltype = 4
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.watercondenskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.watercondenskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            if (playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9) {
              player.sendMessage(YELLOW.toString() + "" + BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました")
              Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1f, 1.2.toFloat())
              Util.sendEveryMessage(GOLD.toString() + "" + BOLD + playerdata.lowercaseName + "が全てのスキルを習得し、アサルトアーマーを解除しました！")
            }
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("ラヴァ・コンデンセーション")) {
          skilllevel = 7
          skilltype = 5
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < 3) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(3, 3) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.lavacondenskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }/*else if(playerdata.activeskilldata.condenskill < skilllevel - 1){
						player.sendMessage(DARK_RED + "前提スキル[" + ActiveSkill.activeSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}*/
        } else if (name.contains("モエラキ・ボールダーズ")) {
          skilllevel = 8
          skilltype = 5
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.lavacondenskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.lavacondenskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("エルト・フェットル")) {
          skilllevel = 9
          skilltype = 5
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.lavacondenskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.activeSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.lavacondenskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.activeSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            if (playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9) {
              player.sendMessage(YELLOW.toString() + "" + BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました")
              Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1f, 1.2.toFloat())
              Util.sendEveryMessage(GOLD.toString() + "" + BOLD + playerdata.lowercaseName + "が全てのスキルを習得し、アサルトアーマーを解除しました！")
            }
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        } else if (name.contains("アサルト・アーマー")) {

        } else if (name.contains("ヴェンダー・ブリザード")) {
          if (playerdata.activeskilldata.skillpoint < 110) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.fluidcondenskill = 10
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + "ヴェンダー・ブリザードを解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
          }
        }


      }
    }
  }
}