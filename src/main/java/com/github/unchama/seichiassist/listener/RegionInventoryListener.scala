package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver
import com.sk89q.worldguard.bukkit.commands.AsyncCommandHelper
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.{EventHandler, Listener}

/**
 * 保護関連メニューのListenerクラス
 * @author karayuu
 * 2017/09/02
 */
class RegionInventoryListener  extends  Listener {
  var playermap = SeichiAssist.playermap

  /**
   * グリッド式保護メニューInventoryClickListener
   * @param event InventoryClickEvent
   */
  @EventHandler
  def onPlayerClickGridMenu(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }
    //クリックしたところにアイテムがない場合終了
    if (event.currentItem == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.getView
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType != EntityType.PLAYER) {
      return
    }
    val topinventory = view.getTopInventory.ifNull { return }
    //インベントリが存在しない時終了
    //インベントリタイプがディスペンサーでない時終了
    if (topinventory.getType != InventoryType.DISPENSER) {
      return
    }

    //インベントリ名が以下の時処理
    if (topinventory.title == LIGHT_PURPLE.toString() + "グリッド式保護設定メニュー") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.getType == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      val player = view.player.asInstanceOf[Player]
      val uuid = player.uniqueId
      val playerData = playermap[uuid]

      //チャンク延長
      if (itemstackcurrent.getType == Material.STAINED_GLASS_PANE && itemstackcurrent.durability.toInt() == 14) {
        gridChangeFunction(player, DirectionType.AHEAD, event)
      } else if (itemstackcurrent.getType == Material.STAINED_GLASS_PANE && itemstackcurrent.durability.toInt() == 10) {
        gridChangeFunction(player, DirectionType.LEFT, event)
      } else if (itemstackcurrent.getType == Material.STAINED_GLASS_PANE && itemstackcurrent.durability.toInt() == 5) {
        gridChangeFunction(player, DirectionType.RIGHT, event)
      } else if (itemstackcurrent.getType == Material.STAINED_GLASS_PANE && itemstackcurrent.durability.toInt() == 13) {
        gridChangeFunction(player, DirectionType.BEHIND, event)
      } else if (itemstackcurrent.getType == Material.WOOL && itemstackcurrent.durability.toInt() == 11) {
        player.chat("//expand vert")
        createRegion(player)
        playerData.regionCount = playerData.regionCount + 1
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
        player.closeInventory()
      } else if (itemstackcurrent.getType == Material.STAINED_GLASS_PANE && itemstackcurrent.durability.toInt() == 4) {
        gridResetFunction(player)
        player.playSound(player.location, Sound.BLOCK_ANVIL_DESTROY, 0.5f, 1.0f)
        player.openInventory(RegionMenuData.gridWorldGuardMenu(player))
      } else if (itemstackcurrent.getType == Material.STAINED_GLASS_PANE && itemstackcurrent.durability.toInt() == 0) {
        playerData.toggleUnitPerGrid()
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
        player.openInventory(RegionMenuData.gridWorldGuardMenu(player))
      } else if (itemstackcurrent.getType == Material.CHEST) {
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
        player.openInventory(RegionMenuData.gridTemplateInventory(player))
      }
    }
  }

  private def createRegion(player: Player) {
    val playerData = SeichiAssist.playermap[player.uniqueId]
    val selection = We.selection(player)

    val region = ProtectedCuboidRegion(player.name + "_" + playerData.regionCount,
        selection.getNativeMinimumPoint().toBlockVector(), selection.getNativeMaximumPoint().toBlockVector())
    val manager = Wg.regionManager(player.world)

    val task = RegionAdder(Wg, manager, region)
    task.setLocatorPolicy(DomainInputResolver.UserLocatorPolicy.UUID_ONLY)
    task.setOwnersInput(arrayOf(player.name))
    val future = Wg.executorService().submit(task)

    AsyncCommandHelper.wrap(future, Wg, player).formatUsing(player.name + "_" + playerData.regionCount)
        .registerWithSupervisor("保護申請中").thenRespondWith("保護申請完了。保護名: '%s'", "保護作成失敗")
  }

  @EventHandler
  def onPlayerClickRegionTemplateMenu(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }
    //クリックしたところにアイテムがない場合終了
    if (event.currentItem == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType != EntityType.PLAYER) {
      return
    }
    val topinventory = view.topInventory.ifNull { return }
    //インベントリが存在しない時終了

    //インベントリ名が以下の時処理
    if (topinventory.title == LIGHT_PURPLE.toString() + "グリッド式保護・設定保存") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.getType == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      val player = view.player.asInstanceOf[Player]
      val uuid = player.uniqueId
      val playerData = playermap[uuid]

      //戻るボタン
      if (itemstackcurrent.getType == Material.BARRIER) {
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.openInventory(RegionMenuData.gridWorldGuardMenu(player))
      } else {
        val slot = event.slot

        val template = playerData.templateMap[slot]

        if (template == null) {
          //何も登録されてないとき
          if (event.isLeftClick) {
            //左クリックの時は新規登録処理
            playerGridTemplateSave(player, slot)
            player.openInventory(RegionMenuData.gridTemplateInventory(player))
          }
          return
        }

        if (event.isLeftClick) {
          player.sendMessage(GREEN.toString() + "グリッド式保護設定データ読み込み完了")
          player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
          playerData.setUnitAmount(DirectionType.AHEAD, template.aheadAmount)
          playerData.setUnitAmount(DirectionType.BEHIND, template.behindAmount)
          playerData.setUnitAmount(DirectionType.RIGHT, template.rightAmount)
          playerData.setUnitAmount(DirectionType.LEFT, template.leftAmount)
          setWGSelection(player)
          canCreateRegion(player)
          player.openInventory(RegionMenuData.gridWorldGuardMenu(player))
        }

        if (event.isRightClick) {
          //新規登録処理
          playerGridTemplateSave(player, slot)
          player.openInventory(RegionMenuData.gridTemplateInventory(player))
        }
      }
    }
  }
}

object RegionInventoryListener {
  internal var Wg = ExternalPlugins.worldGuard()
  internal var We = ExternalPlugins.worldEdit()
  internal var config = SeichiAssist.seichiAssistConfig

  private def gridResetFunction(player: Player) {
    val playerData = SeichiAssist.playermap[player.uniqueId]
      playerData.setUnitAmount(DirectionType.AHEAD, 0)
    playerData.setUnitAmount(DirectionType.BEHIND, 0)
    playerData.setUnitAmount(DirectionType.RIGHT, 0)
    playerData.setUnitAmount(DirectionType.LEFT, 0)
    //始点座標Map(最短)
    val start = getNearlyUnitStart(player)
    //終点座標Map(最短)
    val end = getNearlyUnitEnd(player)
    //範囲選択
    wgSelect(Location(player.world, start["x"], 0.0, start["z"]),
      Location(player.world, end["x"], 256.0, end["z"]), player)
    canCreateRegion(player)
  }

  private def gridChangeFunction(player: Player, directionType: DirectionType, event: InventoryClickEvent) {
    val playerData = SeichiAssist.playermap[player.uniqueId]
    if (event.isLeftClick) {
      if (playerData.canGridExtend(directionType, player.world.name)) {
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerData.addUnitAmount(directionType, playerData.unitPerClick)
        setWGSelection(player)
        canCreateRegion(player)
        player.openInventory(RegionMenuData.gridWorldGuardMenu(player))
      }
    } else if (event.isRightClick) {
      if (playerData.canGridReduce(directionType)) {
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerData.addUnitAmount(directionType, playerData.unitPerClick * -1)
        setWGSelection(player)
        canCreateRegion(player)
        player.openInventory(RegionMenuData.gridWorldGuardMenu(player))
      }
    }
  }

  private def setWGSelection(player: Player) {
    val playerData = SeichiAssist.playermap[player.uniqueId]
    val unitMap = playerData.unitMap
    val direction = Util.playerDirection(player)
    val world = player.world

    val aheadUnitAmount = unitMap[DirectionType.AHEAD]
    val leftsideUnitAmount = unitMap[DirectionType.LEFT]
    val rightsideUnitAmount = unitMap[DirectionType.RIGHT]
    val behindUnitAmount = unitMap[DirectionType.BEHIND]

    //0ユニット指定の始点/終点のx,z座標
    val start_x = getNearlyUnitStart(player)["x"]
    val start_z = getNearlyUnitStart(player)["z"]
    val end_x = getNearlyUnitEnd(player)["x"]
    val end_z = getNearlyUnitEnd(player)["z"]

    var start_loc: Location? = null
    var end_loc: Location? = null

    when (direction) {
      Util.Direction.NORTH => {
        start_loc = Location(world, start_x - 15 * leftsideUnitAmount, 0.0, start_z - 15 * aheadUnitAmount)
        end_loc = Location(world, end_x + 15 * rightsideUnitAmount, 256.0, end_z + 15 * behindUnitAmount)
      }

      Util.Direction.EAST => {
        start_loc = Location(world, start_x - 15 * behindUnitAmount, 0.0, start_z + 15 * leftsideUnitAmount)
        end_loc = Location(world, end_x + 15 * aheadUnitAmount, 256.0, end_z + 15 * rightsideUnitAmount)
      }

      Util.Direction.SOUTH => {
        start_loc = Location(world, start_x - 15 * rightsideUnitAmount, 0.0, start_z - 15 * behindUnitAmount)
        end_loc = Location(world, end_x + 15 * leftsideUnitAmount, 256.0, end_z + 15 * aheadUnitAmount)
      }

      Util.Direction.WEST => {
        start_loc = Location(world, start_x - 15 * aheadUnitAmount, 0.0, start_z - 15 * rightsideUnitAmount)
        end_loc = Location(world, end_x + 15 * behindUnitAmount, 256.0, end_z + 15 * leftsideUnitAmount)
      }
    }//わざと何もしない。
    wgSelect(start_loc, end_loc, player)
  }

  private def wgSelect(loc1: Location, loc2: Location, player: Player) {
    player.chat("//;")
    player.chat("//pos1 " + loc1.x.toInt() + "," + loc1.y.toInt() + "," + loc1.z.toInt())
    player.chat("//pos2 " + loc2.x.toInt() + "," + loc2.y.toInt() + "," + loc2.z.toInt())
  }

  private def canCreateRegion(player: Player) {
    val playerData = SeichiAssist.playermap[player.uniqueId]
    val selection = We.selection(player)
    val manager = Wg.regionManager(player.world)
    val wcfg = Wg.globalStateManager().get(player.world)

    if (selection == null) {
      playerData.canCreateRegion = false
    }

    val region = ProtectedCuboidRegion(player.name + "_" + playerData.regionCount,
      selection.getNativeMinimumPoint().toBlockVector(), selection.getNativeMaximumPoint().toBlockVector())
    val regions = manager.getApplicableRegions(region)

    if (regions.size() !== 0) {
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

  private def playerGridTemplateSave(player: Player, i: Int) {
    val playerData = SeichiAssist.playermap[player.uniqueId]
    val unitMap = playerData.unitMap

    player.sendMessage(GREEN.toString() + "グリッド式保護の現在の設定を保存しました。")
    player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
    val template = GridTemplate(unitMap[DirectionType.AHEAD], unitMap[DirectionType.BEHIND],
      unitMap[DirectionType.RIGHT], unitMap[DirectionType.LEFT])
    playerData.templateMap[i] = template
  }

  /**
   * ユニット単位における最短の始点のx,z座標を取得します。
   * @param player 該当プレイヤー
   * @return x,z座標のMap
   */
  def getNearlyUnitStart(player: Player): Map[String, Double] = {
    val result = HashMap[String, Double]()

    val player_x = player.location.blockX.toDouble()
    val player_z = player.location.blockZ.toDouble()

    if (player_x % 15 == 0.0) {
      result["x"] = player_x
    } else {
      result["x"] = Math.floor(player_x / 15) * 15
    }

    if (player_z % 15 == 0.0) {
      result["z"] = player_z
    } else {
      result["z"] = Math.floor(player_z / 15) * 15
    }
    return result
  }

  /**
   * ユニット単位における最短の終点(始点から対角になる)のx,z座標を取得します。
   * @param player 該当プレイヤー
   * @return x,z座標のMap
   */
  def getNearlyUnitEnd(player: Player): Map[String, Double] = {
    val startCoordinate = getNearlyUnitStart(player)

    val resultMap = HashMap[String, Double]()

    resultMap["x"] = startCoordinate["x"] + 14.0
    resultMap["z"] = startCoordinate["z"] + 14.0

    return resultMap
  }
}
