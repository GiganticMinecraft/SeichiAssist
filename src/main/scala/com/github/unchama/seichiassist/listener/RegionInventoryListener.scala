package com.github.unchama.seichiassist.listener

import java.util.UUID

import com.github.unchama.seichiassist.{Config, SeichiAssist}
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.data.{GridTemplate, RegionMenuData}
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.seichiassist.util.Util.DirectionType
import com.github.unchama.seichiassist.util.external.ExternalPlugins
import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.bukkit.commands.AsyncCommandHelper
import com.sk89q.worldguard.bukkit.commands.task.RegionAdder
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import com.sk89q.worldguard.protection.util.DomainInputResolver
import org.bukkit.ChatColor._
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryType}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.{Location, Material, Sound}

import scala.collection.mutable

/**
 * 保護関連メニューのListenerクラス
 *
 * @author karayuu
 *         2017/09/02
 */
class RegionInventoryListener extends Listener {
  val playermap: mutable.HashMap[UUID, PlayerData] = SeichiAssist.playermap

  import RegionInventoryListener._

  /**
   * グリッド式保護メニューInventoryClickListener
   *
   * @param event InventoryClickEvent
   */
  @EventHandler
  def onPlayerClickGridMenu(event: InventoryClickEvent): Unit = {
    //外枠のクリック処理なら終了
    if (event.getClickedInventory == null) {
      return
    }
    //クリックしたところにアイテムがない場合終了
    if (event.getCurrentItem == null) {
      return
    }

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
    //インベントリが存在しない時終了
    //インベントリタイプがディスペンサーでない時終了
    if (topinventory.getType != InventoryType.DISPENSER) {
      return
    }

    //インベントリ名が以下の時処理
    if (topinventory.getTitle == LIGHT_PURPLE.toString + "グリッド式保護設定メニュー") {
      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      val player = view.getPlayer.asInstanceOf[Player]
      val uuid = player.getUniqueId
      val playerData = playermap(uuid)

      //チャンク延長
      if (itemstackcurrent.getType == Material.STAINED_GLASS_PANE && itemstackcurrent.getDurability.toInt == 14) {
        gridChangeFunction(player, DirectionType.AHEAD, event)
      } else if (itemstackcurrent.getType == Material.STAINED_GLASS_PANE && itemstackcurrent.getDurability.toInt == 10) {
        gridChangeFunction(player, DirectionType.LEFT, event)
      } else if (itemstackcurrent.getType == Material.STAINED_GLASS_PANE && itemstackcurrent.getDurability.toInt == 5) {
        gridChangeFunction(player, DirectionType.RIGHT, event)
      } else if (itemstackcurrent.getType == Material.STAINED_GLASS_PANE && itemstackcurrent.getDurability.toInt == 13) {
        gridChangeFunction(player, DirectionType.BEHIND, event)
      } else if (itemstackcurrent.getType == Material.WOOL && itemstackcurrent.getDurability.toInt == 11) {
        player.chat("//expand vert")
        createRegion(player)
        playerData.regionCount = playerData.regionCount + 1
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
        player.closeInventory()
      } else if (itemstackcurrent.getType == Material.STAINED_GLASS_PANE && itemstackcurrent.getDurability.toInt == 4) {
        gridResetFunction(player)
        player.playSound(player.getLocation, Sound.BLOCK_ANVIL_DESTROY, 0.5f, 1.0f)
        player.openInventory(RegionMenuData.getGridWorldGuardMenu(player))
      } else if (itemstackcurrent.getType == Material.STAINED_GLASS_PANE && itemstackcurrent.getDurability.toInt == 0) {
        playerData.toggleUnitPerGrid()
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
        player.openInventory(RegionMenuData.getGridWorldGuardMenu(player))
      } else if (itemstackcurrent.getType == Material.CHEST) {
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
        player.openInventory(RegionMenuData.getGridTemplateInventory(player))
      }
    }
  }

  private def createRegion(player: Player): Unit = {
    val playerData = SeichiAssist.playermap(player.getUniqueId)
    val selection = We.getSelection(player)

    val region = new ProtectedCuboidRegion(player.getName + "_" + playerData.regionCount,
      selection.getNativeMinimumPoint.toBlockVector, selection.getNativeMaximumPoint.toBlockVector)
    val manager = Wg.getRegionManager(player.getWorld)

    val task = new RegionAdder(Wg, manager, region)
    task.setLocatorPolicy(DomainInputResolver.UserLocatorPolicy.UUID_ONLY)
    task.setOwnersInput(Array(player.getName))
    val future = Wg.getExecutorService.submit(task)

    AsyncCommandHelper.wrap(future, Wg, player).formatUsing(player.getName + "_" + playerData.regionCount)
      .registerWithSupervisor("保護申請中").thenRespondWith("保護申請完了。保護名: '%s'", "保護作成失敗")
  }

  @EventHandler
  def onPlayerClickRegionTemplateMenu(event: InventoryClickEvent): Unit = {
    //外枠のクリック処理なら終了
    if (event.getClickedInventory == null) {
      return
    }
    //クリックしたところにアイテムがない場合終了
    if (event.getCurrentItem == null) {
      return
    }

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
    //インベントリが存在しない時終了

    //インベントリ名が以下の時処理
    if (topinventory.getTitle == LIGHT_PURPLE.toString + "グリッド式保護・設定保存") {
      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      val player = view.getPlayer.asInstanceOf[Player]
      val uuid = player.getUniqueId
      val playerData = playermap(uuid)

      //戻るボタン
      if (itemstackcurrent.getType == Material.BARRIER) {
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.openInventory(RegionMenuData.getGridWorldGuardMenu(player))
      } else {
        val slot = event.getSlot

        val template = playerData.templateMap(slot)

        if (template == null) {
          //何も登録されてないとき
          if (event.isLeftClick) {
            //左クリックの時は新規登録処理
            playerGridTemplateSave(player, slot)
            player.openInventory(RegionMenuData.getGridTemplateInventory(player))
          }
          return
        }

        if (event.isLeftClick) {
          player.sendMessage(GREEN.toString + "グリッド式保護設定データ読み込み完了")
          player.playSound(player.getLocation, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
          playerData.setUnitAmount(DirectionType.AHEAD, template.getAheadAmount)
          playerData.setUnitAmount(DirectionType.BEHIND, template.getBehindAmount)
          playerData.setUnitAmount(DirectionType.RIGHT, template.getRightAmount)
          playerData.setUnitAmount(DirectionType.LEFT, template.getLeftAmount)
          setWGSelection(player)
          canCreateRegion(player)
          player.openInventory(RegionMenuData.getGridWorldGuardMenu(player))
        }

        if (event.isRightClick) {
          //新規登録処理
          playerGridTemplateSave(player, slot)
          player.openInventory(RegionMenuData.getGridTemplateInventory(player))
        }
      }
    }
  }
}

object RegionInventoryListener {
  val Wg: WorldGuardPlugin = ExternalPlugins.getWorldGuard
  val We: WorldEditPlugin = ExternalPlugins.getWorldEdit
  var config: Config = SeichiAssist.seichiAssistConfig

  private def gridResetFunction(player: Player): Unit = {
    val playerData = SeichiAssist.playermap(player.getUniqueId)
    playerData.setUnitAmount(DirectionType.AHEAD, 0)
    playerData.setUnitAmount(DirectionType.BEHIND, 0)
    playerData.setUnitAmount(DirectionType.RIGHT, 0)
    playerData.setUnitAmount(DirectionType.LEFT, 0)
    //始点座標Map(最短)
    val start = getNearlyUnitStart(player)
    //終点座標Map(最短)
    val end = getNearlyUnitEnd(player)
    //範囲選択
    wgSelect(
      new Location(player.getWorld, start("x"), 0.0, start("z")),
      new Location(player.getWorld, end("x"), 256.0, end("z")),
      player
    )

    canCreateRegion(player)
  }

  private def gridChangeFunction(player: Player, directionType: DirectionType, event: InventoryClickEvent): Unit = {
    val playerData = SeichiAssist.playermap(player.getUniqueId)
    if (event.isLeftClick) {
      if (playerData.canGridExtend(directionType, player.getWorld.getName)) {
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerData.addUnitAmount(directionType, playerData.unitPerClick)
        setWGSelection(player)
        canCreateRegion(player)
        player.openInventory(RegionMenuData.getGridWorldGuardMenu(player))
      }
    } else if (event.isRightClick) {
      if (playerData.canGridReduce(directionType)) {
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerData.addUnitAmount(directionType, playerData.unitPerClick * -1)
        setWGSelection(player)
        canCreateRegion(player)
        player.openInventory(RegionMenuData.getGridWorldGuardMenu(player))
      }
    }
  }

  private def setWGSelection(player: Player): Unit = {
    val playerData = SeichiAssist.playermap(player.getUniqueId)
    val unitMap = playerData.unitMap
    val direction = Util.getPlayerDirection(player)
    val world = player.getWorld

    val aheadUnitAmount = unitMap(DirectionType.AHEAD)
    val leftsideUnitAmount = unitMap(DirectionType.LEFT)
    val rightsideUnitAmount = unitMap(DirectionType.RIGHT)
    val behindUnitAmount = unitMap(DirectionType.BEHIND)

    //0ユニット指定の始点/終点のx,z座標
    val start_x = getNearlyUnitStart(player)("x")
    val start_z = getNearlyUnitStart(player)("z")
    val end_x = getNearlyUnitEnd(player)("x")
    val end_z = getNearlyUnitEnd(player)("z")

    val (start_loc, end_loc) = direction match {
      case Util.Direction.NORTH =>
        (
          new Location(world, start_x - 15 * leftsideUnitAmount, 0.0, start_z - 15 * aheadUnitAmount),
          new Location(world, end_x + 15 * rightsideUnitAmount, 256.0, end_z + 15 * behindUnitAmount)
        )

      case Util.Direction.EAST =>
        (
          new Location(world, start_x - 15 * behindUnitAmount, 0.0, start_z + 15 * leftsideUnitAmount),
          new Location(world, end_x + 15 * aheadUnitAmount, 256.0, end_z + 15 * rightsideUnitAmount)
        )

      case Util.Direction.SOUTH =>
        (
          new Location(world, start_x - 15 * rightsideUnitAmount, 0.0, start_z - 15 * behindUnitAmount),
          new Location(world, end_x + 15 * leftsideUnitAmount, 256.0, end_z + 15 * aheadUnitAmount)
        )

      case Util.Direction.WEST =>
        (
          new Location(world, start_x - 15 * aheadUnitAmount, 0.0, start_z - 15 * rightsideUnitAmount),
          new Location(world, end_x + 15 * behindUnitAmount, 256.0, end_z + 15 * leftsideUnitAmount)
        )
      case _ => (null, null)
    } //わざと何もしない。
    wgSelect(start_loc, end_loc, player)
  }

  private def wgSelect(loc1: Location, loc2: Location, player: Player): Unit = {
    player.chat("//;")
    player.chat("//pos1 " + loc1.getX.toInt + "," + loc1.getY.toInt + "," + loc1.getZ.toInt)
    player.chat("//pos2 " + loc2.getX.toInt + "," + loc2.getY.toInt + "," + loc2.getZ.toInt)
  }

  /**
   * ユニット単位における最短の終点(始点から対角になる)のx,z座標を取得します。
   *
   * @param player 該当プレイヤー
   * @return x,z座標のMap
   */
  def getNearlyUnitEnd(player: Player): Map[String, Double] = {
    val startCoordinate = getNearlyUnitStart(player)

    Map(
      "x" -> (startCoordinate("x") + 14.0),
      "z" -> (startCoordinate("z") + 14.0)
    )
  }

  /**
   * ユニット単位における最短の始点のx,z座標を取得します。
   *
   * @param player 該当プレイヤー
   * @return x,z座標のMap
   */
  def getNearlyUnitStart(player: Player): Map[String, Double] = {
    def getNearestUnitStart(component: Int) = (component / 15) * 15

    val playerLocation: Location = player.getLocation

    Map(
      "x" -> getNearestUnitStart(playerLocation.getBlockX),
      "z" -> getNearestUnitStart(playerLocation.getBlockZ)
    )
  }

  private def canCreateRegion(player: Player): Unit = {
    val playerData = SeichiAssist.playermap(player.getUniqueId)
    val selection = We.getSelection(player)
    val manager = Wg.getRegionManager(player.getWorld)
    val wcfg = Wg.getGlobalStateManager.get(player.getWorld)

    if (selection == null) {
      playerData.canCreateRegion = false
    }

    val region = new ProtectedCuboidRegion(player.getName + "_" + playerData.regionCount,
      selection.getNativeMinimumPoint.toBlockVector, selection.getNativeMaximumPoint.toBlockVector)
    val regions = manager.getApplicableRegions(region)

    if (regions.size() != 0) {
      playerData.canCreateRegion = false
      return
    }

    val maxRegionCount = wcfg.getMaxRegionCount(player)
    if (maxRegionCount >= 0 && manager.getRegionCountOfPlayer(Wg.wrapPlayer(player)) >= maxRegionCount) {
      playerData.canCreateRegion = false
      return
    }

    playerData.canCreateRegion = true
  }

  private def playerGridTemplateSave(player: Player, i: Int): Unit = {
    val playerData = SeichiAssist.playermap(player.getUniqueId)
    val unitMap = playerData.unitMap

    player.sendMessage(GREEN.toString + "グリッド式保護の現在の設定を保存しました。")
    player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
    val template = new GridTemplate(unitMap(DirectionType.AHEAD), unitMap(DirectionType.BEHIND),
      unitMap(DirectionType.RIGHT), unitMap(DirectionType.LEFT))
    playerData.templateMap(i) = template
  }
}
