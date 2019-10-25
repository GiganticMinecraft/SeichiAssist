package com.github.unchama.seichiassist.listener.invlistener

import com.github.unchama.seichiassist.data.ActiveSkillInventoryData
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.seichiassist.{ActiveSkill, SeichiAssist}
import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver
import org.bukkit.ChatColor._
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.event.Listener
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryType}
import org.bukkit.{Material, Sound}

object OnActiveSkillUnselect extends Listener {

  def onPlayerClickActiveSkillReleaseEvent(event: InventoryClickEvent): Unit = {
    //外枠のクリック処理なら終了
    if (event.getClickedInventory == null) return

    val itemstackcurrent = event.getCurrentItem
    val view = event.getView
    val he = view.getPlayer
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType != EntityType.PLAYER) {
      return
    }

    val topinventory = view.getTopInventory.ifNull {
      return
    }

    import com.github.unchama.util.InventoryUtil._

    //インベントリサイズが36でない時終了
    if (topinventory.row != 5) return
    val player = he.asInstanceOf[Player]
    val uuid = player.getUniqueId
    val playerdata = SeichiAssist.playermap(uuid)

    //インベントリ名が以下の時処理
    if (topinventory.getTitle == DARK_PURPLE.toString + "" + BOLD + "整地スキル選択") {
      event.setCancelled(true)
      //プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      }
      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      if (itemstackcurrent.getType == Material.BEDROCK) {
        val itemmeta = itemstackcurrent.getItemMeta
        val name = itemmeta.getDisplayName
        if (name.contains("エビフライ・ドライブ")) {
          val skillLevel = 4
          val skillType = 1
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.5.toFloat)
          } else if (playerdata.activeskilldata.breakskill < 3) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(3, 3) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.5.toFloat)
          } else {
            playerdata.activeskilldata.arrowskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ホーリー・ショット")) {
          val skillLevel = 5
          val skillType = 1
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.arrowskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.arrowskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ツァーリ・ボンバ")) {
          val skillLevel = 6
          val skillType = 1
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.arrowskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.arrowskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("アーク・ブラスト")) {
          val skillLevel = 7
          val skillType = 1
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.arrowskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.arrowskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ファンタズム・レイ")) {
          val skillLevel = 8
          val skillType = 1
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.arrowskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.arrowskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("スーパー・ノヴァ")) {
          val skillLevel = 9
          val skillType = 1
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.arrowskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.arrowskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            if (playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.breakskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9) {
              player.sendMessage(YELLOW.toString + "" + BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました")
              Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1f, 1.2.toFloat)
              Util.sendEveryMessage(GOLD.toString + "" + BOLD + playerdata.lowercaseName + "が全てのスキルを習得し、アサルトアーマーを解除しました！")
            }
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("トム・ボウイ")) {
          val skillLevel = 4
          val skillType = 2
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.breakskill < 3) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(3, 3) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.multiskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("サンダー・ストーム")) {
          val skillLevel = 5
          val skillType = 2
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.multiskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.multiskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("スターライト・ブレイカー")) {
          val skillLevel = 6
          val skillType = 2
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.multiskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.multiskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("アース・ディバイド")) {
          val skillLevel = 7
          val skillType = 2
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.multiskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.multiskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ヘヴン・ゲイボルグ")) {
          val skillLevel = 8
          val skillType = 2
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.multiskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.multiskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ディシジョン")) {
          val skillLevel = 9
          val skillType = 2
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.multiskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.multiskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            if (playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.breakskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9) {
              player.sendMessage(YELLOW.toString + "" + BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました")
              Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1f, 1.2.toFloat)
              Util.sendEveryMessage(GOLD.toString + "" + BOLD + playerdata.lowercaseName + "が全てのスキルを習得し、アサルトアーマーを解除しました！")
            }
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("デュアル・ブレイク")) {
          val skillLevel = 1
          val skillType = 3
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.breakskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("トリアル・ブレイク")) {
          val skillLevel = 2
          val skillType = 3
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.breakskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.breakskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("エクスプロージョン")) {
          val skillLevel = 3
          val skillType = 3
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.breakskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.breakskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ミラージュ・フレア")) {
          val skillLevel = 4
          val skillType = 3
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.breakskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.breakskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ドッ・カーン")) {
          val skillLevel = 5
          val skillType = 3
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.breakskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.breakskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ギガンティック・ボム")) {
          val skillLevel = 6
          val skillType = 3
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.breakskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.breakskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ブリリアント・デトネーション")) {
          val skillLevel = 7
          val skillType = 3
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.breakskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.breakskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("レムリア・インパクト")) {
          val skillLevel = 8
          val skillType = 3
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.breakskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.breakskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("エターナル・ヴァイス")) {
          val skillLevel = 9
          val skillType = 3
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.breakskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.breakskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            if (playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9) {
              player.sendMessage(YELLOW.toString + "" + BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました")
              Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1f, 1.2.toFloat)
              Util.sendEveryMessage(GOLD.toString + "" + BOLD + playerdata.lowercaseName + "が全てのスキルを習得し、アサルトアーマーを解除しました！")
            }
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ホワイト・ブレス")) {
          val skillLevel = 7
          val skillType = 4
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.breakskill < 3) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(3, 3) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.watercondenskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("アブソリュート・ゼロ")) {
          val skillLevel = 8
          val skillType = 4
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.watercondenskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.watercondenskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ダイアモンド・ダスト")) {
          val skillLevel = 9
          val skillType = 4
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.watercondenskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.watercondenskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            if (playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9) {
              player.sendMessage(YELLOW.toString + "" + BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました")
              Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1f, 1.2.toFloat)
              Util.sendEveryMessage(GOLD.toString + "" + BOLD + playerdata.lowercaseName + "が全てのスキルを習得し、アサルトアーマーを解除しました！")
            }
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ラヴァ・コンデンセーション")) {
          val skillLevel = 7
          val skillType = 5
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.breakskill < 3) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(3, 3) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.lavacondenskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          } /*else if(playerdata.activeskilldata.condenskill < skilllevel - 1){
						player.sendMessage(DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}*/
        } else if (name.contains("モエラキ・ボールダーズ")) {
          val skillLevel = 8
          val skillType = 5
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.lavacondenskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.lavacondenskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("エルト・フェットル")) {
          val skillLevel = 9
          val skillType = 5
          if (playerdata.activeskilldata.skillpoint < skillLevel * 10) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else if (playerdata.activeskilldata.lavacondenskill < skillLevel - 1) {
            player.sendMessage(DARK_RED.toString + "前提スキル[" + ActiveSkill.getActiveSkillName(skillType, skillLevel - 1) + "]を習得する必要があります")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.lavacondenskill = skillLevel
            player.sendMessage(AQUA.toString + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skillType, skillLevel) + "を解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            if (playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9) {
              player.sendMessage(YELLOW.toString + "" + BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました")
              Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1f, 1.2.toFloat)
              Util.sendEveryMessage(GOLD.toString + "" + BOLD + playerdata.lowercaseName + "が全てのスキルを習得し、アサルトアーマーを解除しました！")
            }
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("アサルト・アーマー")) {

        } else if (name.contains("ヴェンダー・ブリザード")) {
          if (playerdata.activeskilldata.skillpoint < 110) {
            player.sendMessage(DARK_RED.toString + "アクティブスキルポイントが足りません")
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          } else {
            playerdata.activeskilldata.fluidcondenskill = 10
            player.sendMessage(AQUA.toString + "" + BOLD + "" + "ヴェンダー・ブリザードを解除しました")
            player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        }
      }
    }
  }
}