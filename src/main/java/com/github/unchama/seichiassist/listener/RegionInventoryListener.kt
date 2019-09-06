package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist.Config
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.GridTemplate
import com.github.unchama.seichiassist.data.RegionMenuData
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.seichiassist.util.Util.Direction
import com.github.unchama.seichiassist.util.Util.DirectionType
import com.github.unchama.seichiassist.util.external.ExternalPlugins
import com.google.common.util.concurrent.ListenableFuture
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldedit.bukkit.selections.Selection
import com.sk89q.worldguard.bukkit.WorldConfiguration
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.bukkit.commands.AsyncCommandHelper
import com.sk89q.worldguard.bukkit.commands.task.RegionAdder
import com.sk89q.worldguard.protection.ApplicableRegionSet
import com.sk89q.worldguard.protection.managers.RegionManager
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import com.sk89q.worldguard.protection.util.DomainInputResolver
import net.md_5.bungee.api.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

import java.util.HashMap
import java.util.UUID

/**
 * 保護関連メニューのListenerクラス
 * @author karayuu
 * 2017/09/02
 */
class RegionInventoryListener : Listener {
  internal var playermap = SeichiAssist.playermap

  /**
   * 木の棒メニューの保護ボタンのみのListener
   * @param event InventoryClickListener
   */
  @EventHandler
  fun onPlayerClickStickMenu(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.type != EntityType.PLAYER) {
      return
    }
    val topinventory = view.topInventory ?: return
    //インベントリが存在しない時終了
    //インベントリサイズが36でない時終了
    if (topinventory.size != 4 * 9) {
      return
    }
    val player = he as Player

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "木の棒メニュー") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }
      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

      //土地保護メニュー
      if (itemstackcurrent.type == Material.DIAMOND_AXE && itemstackcurrent.itemMeta.displayName.contains("土地保護メニュー")) {
        player.openInventory(RegionMenuData.getRegionMenuData(player))
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1.0f, 0.5f)
      }
    }
  }

  /**
   * 保護メニューのInventoryClickListener
   * @param event InventoryClickListener
   */
  @EventHandler
  fun onPlayerRegionMenu(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.type != EntityType.PLAYER) {
      return
    }
    val topinventory = view.topInventory ?: return
    //インベントリが存在しない時終了
    //インベントリタイプがホッパーでない時終了
    if (topinventory.type != InventoryType.HOPPER) {
      return
    }
    val player = he as Player

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.BLACK.toString() + "保護メニュー") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }
      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

      val uuid = player.uniqueId
      val playerdata = playermap[uuid]!!

      if (itemstackcurrent.type == Material.WOOD_AXE) {
        // wand召喚
        player.closeInventory()
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.chat("//wand")
        player.sendMessage(ChatColor.RESET.toString() + "" + ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "保護のかけ方\n"
            + ChatColor.RESET + "" + ChatColor.GREEN + "①召喚された斧を手に持ちます\n"
            + ChatColor.RESET + "" + ChatColor.GREEN + "②保護したい領域の一方の角を" + ChatColor.YELLOW + "左" + ChatColor.GREEN + "クリック\n"
            + ChatColor.RESET + "" + ChatColor.GREEN + "③もう一方の対角線上の角を" + ChatColor.RED + "右" + ChatColor.GREEN + "クリック\n"
            + ChatColor.RESET + "" + ChatColor.GREEN + "④メニューの" + ChatColor.RESET + "" + ChatColor.YELLOW + "金の斧" + ChatColor.RESET + "" + ChatColor.GREEN + "をクリック\n"
        )
      } else if (itemstackcurrent.type == Material.GOLD_AXE) {
        // 保護の設定
        player.closeInventory()
        val selection = ExternalPlugins.getWorldEdit()!!.getSelection(player)
        if (!player.hasPermission("worldguard.region.claim")) {
          player.sendMessage(ChatColor.RED.toString() + "このワールドでは保護を申請できません")
          return
        } else if (selection == null) {
          player.sendMessage(ChatColor.RED.toString() + "先に木の斧で範囲を指定してからこのボタンを押してください")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 0.5f)
          return
        } else if (selection!!.getLength() < 10 || selection!!.getWidth() < 10) {
          player.sendMessage(ChatColor.RED.toString() + "指定された範囲が狭すぎます。1辺当たり最低10ブロック以上にしてください")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 0.5f)
          return
        }

        player.chat("//expand vert")
        player.chat("/rg claim " + player.name + "_" + playerdata.regionCount)
        playerdata.regionCount = playerdata.regionCount + 1
        player.chat("//sel")
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
      } else if (itemstackcurrent.type == Material.STONE_AXE) {
        // 保護リストの表示
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
        player.closeInventory()
        player.sendMessage(ChatColor.GRAY.toString() + "--------------------\n"
            + ChatColor.GRAY + "複数ページの場合… " + ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.BOLD + "/rg list -p " + player.name + " ページNo\n"
            + ChatColor.RESET + "" + ChatColor.GRAY + "先頭に[+]のついた保護はOwner権限\n[-]のついた保護はMember権限を保有しています\n"
            + ChatColor.DARK_GREEN + "解説ページ→" + ChatColor.UNDERLINE + "https://seichi.click/wiki/WorldGuard")
        player.chat("/rg list -p " + player.name)
      } else if (itemstackcurrent.type == Material.DIAMOND_AXE) {
        // ReguionGUI表示
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.closeInventory()
        player.chat("/land")
      } else if (itemstackcurrent.type == Material.IRON_AXE) {
        gridResetFunction(player)
        //グリッド式保護設定画面表示
        player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
        player.openInventory(RegionMenuData.getGridWorldGuardMenu(player))
      }
    }
  }

  /**
   * グリッド式保護メニューInventoryClickListener
   * @param event InventoryClickEvent
   */
  @EventHandler
  fun onPlayerClickGridMenu(event: InventoryClickEvent) {
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
    if (he.type != EntityType.PLAYER) {
      return
    }
    val topinventory = view.topInventory ?: return
    //インベントリが存在しない時終了
    //インベントリタイプがディスペンサーでない時終了
    if (topinventory.type != InventoryType.DISPENSER) {
      return
    }

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.LIGHT_PURPLE.toString() + "グリッド式保護設定メニュー") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      val player = view.player as Player
      val uuid = player.uniqueId
      val playerData = playermap[uuid]!!

      //チャンク延長
      if (itemstackcurrent.type == Material.STAINED_GLASS_PANE && itemstackcurrent.durability.toInt() == 14) {
        gridChangeFunction(player, DirectionType.AHEAD, event)
      } else if (itemstackcurrent.type == Material.STAINED_GLASS_PANE && itemstackcurrent.durability.toInt() == 10) {
        gridChangeFunction(player, DirectionType.LEFT, event)
      } else if (itemstackcurrent.type == Material.STAINED_GLASS_PANE && itemstackcurrent.durability.toInt() == 5) {
        gridChangeFunction(player, DirectionType.RIGHT, event)
      } else if (itemstackcurrent.type == Material.STAINED_GLASS_PANE && itemstackcurrent.durability.toInt() == 13) {
        gridChangeFunction(player, DirectionType.BEHIND, event)
      } else if (itemstackcurrent.type == Material.WOOL && itemstackcurrent.durability.toInt() == 11) {
        player.chat("//expand vert")
        createRegion(player)
        playerData.regionCount = playerData.regionCount + 1
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
        player.closeInventory()
      } else if (itemstackcurrent.type == Material.STAINED_GLASS_PANE && itemstackcurrent.durability.toInt() == 4) {
        gridResetFunction(player)
        player.playSound(player.location, Sound.BLOCK_ANVIL_DESTROY, 0.5f, 1.0f)
        player.openInventory(RegionMenuData.getGridWorldGuardMenu(player))
      } else if (itemstackcurrent.type == Material.STAINED_GLASS_PANE && itemstackcurrent.durability.toInt() == 0) {
        playerData.toggleUnitPerGrid()
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
        player.openInventory(RegionMenuData.getGridWorldGuardMenu(player))
      } else if (itemstackcurrent.type == Material.CHEST) {
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
        player.openInventory(RegionMenuData.getGridTemplateInventory(player))
      }
    }
  }

  private fun createRegion(player: Player) {
    val playerData = SeichiAssist.playermap[player.uniqueId]!!
    val selection = We!!.getSelection(player)

    val region = ProtectedCuboidRegion(player.name + "_" + playerData.regionCount,
        selection.getNativeMinimumPoint().toBlockVector(), selection.getNativeMaximumPoint().toBlockVector())
    val manager = Wg.getRegionManager(player.world)

    val task = RegionAdder(Wg, manager, region)
    task.setLocatorPolicy(DomainInputResolver.UserLocatorPolicy.UUID_ONLY)
    task.setOwnersInput(arrayOf(player.name))
    val future = Wg.getExecutorService().submit(task)

    AsyncCommandHelper.wrap(future, Wg, player).formatUsing(player.name + "_" + playerData.regionCount)
        .registerWithSupervisor("保護申請中").thenRespondWith("保護申請完了。保護名: '%s'", "保護作成失敗")
  }

  @EventHandler
  fun onPlayerClickRegionTemplateMenu(event: InventoryClickEvent) {
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
    if (he.type != EntityType.PLAYER) {
      return
    }
    val topinventory = view.topInventory ?: return
    //インベントリが存在しない時終了

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.LIGHT_PURPLE.toString() + "グリッド式保護・設定保存") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      val player = view.player as Player
      val uuid = player.uniqueId
      val playerData = playermap[uuid]!!

      //戻るボタン
      if (itemstackcurrent.type == Material.BARRIER) {
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.openInventory(RegionMenuData.getGridWorldGuardMenu(player))
      } else {
        val slot = event.slot

        val template = playerData.templateMap[slot]

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
          player.sendMessage(ChatColor.GREEN.toString() + "グリッド式保護設定データ読み込み完了")
          player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
          playerData.setUnitAmount(DirectionType.AHEAD, template.aheadAmount)
          playerData.setUnitAmount(DirectionType.BEHIND, template.behindAmount)
          playerData.setUnitAmount(DirectionType.RIGHT, template.rightAmount)
          playerData.setUnitAmount(DirectionType.LEFT, template.leftAmount)
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

  companion object {
    internal var Wg = ExternalPlugins.getWorldGuard()
    internal var We = ExternalPlugins.getWorldEdit()
    internal var config = SeichiAssist.seichiAssistConfig

    private fun gridResetFunction(player: Player) {
      val playerData = SeichiAssist.playermap[player.uniqueId]!!
      playerData.setUnitAmount(DirectionType.AHEAD, 0)
      playerData.setUnitAmount(DirectionType.BEHIND, 0)
      playerData.setUnitAmount(DirectionType.RIGHT, 0)
      playerData.setUnitAmount(DirectionType.LEFT, 0)
      //始点座標Map(最短)
      val start = getNearlyUnitStart(player)
      //終点座標Map(最短)
      val end = getNearlyUnitEnd(player)
      //範囲選択
      wgSelect(Location(player.world, start["x"]!!, 0.0, start["z"]!!),
          Location(player.world, end["x"]!!, 256.0, end["z"]!!), player)
      canCreateRegion(player)
    }

    private fun gridChangeFunction(player: Player, directionType: DirectionType, event: InventoryClickEvent) {
      val playerData = SeichiAssist.playermap[player.uniqueId]!!
      if (event.isLeftClick) {
        if (playerData.canGridExtend(directionType, player.world.name)) {
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          playerData.addUnitAmount(directionType, playerData.unitPerClick)
          setWGSelection(player)
          canCreateRegion(player)
          player.openInventory(RegionMenuData.getGridWorldGuardMenu(player))
        }
      } else if (event.isRightClick) {
        if (playerData.canGridReduce(directionType)) {
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          playerData.addUnitAmount(directionType, playerData.unitPerClick * -1)
          setWGSelection(player)
          canCreateRegion(player)
          player.openInventory(RegionMenuData.getGridWorldGuardMenu(player))
        }
      }
    }

    private fun setWGSelection(player: Player) {
      val playerData = SeichiAssist.playermap[player.uniqueId]!!
      val unitMap = playerData.unitMap
      val direction = Util.getPlayerDirection(player)
      val world = player.world

      val aheadUnitAmount = unitMap[DirectionType.AHEAD]!!
      val leftsideUnitAmount = unitMap[DirectionType.LEFT]!!
      val rightsideUnitAmount = unitMap[DirectionType.RIGHT]!!
      val behindUnitAmount = unitMap[DirectionType.BEHIND]!!

      //0ユニット指定の始点/終点のx,z座標
      val start_x = getNearlyUnitStart(player)["x"]!!
      val start_z = getNearlyUnitStart(player)["z"]!!
      val end_x = getNearlyUnitEnd(player)["x"]!!
      val end_z = getNearlyUnitEnd(player)["z"]!!

      var start_loc: Location? = null
      var end_loc: Location? = null

      when (direction) {
        Util.Direction.NORTH -> {
          start_loc = Location(world, start_x - 15 * leftsideUnitAmount, 0.0, start_z - 15 * aheadUnitAmount)
          end_loc = Location(world, end_x + 15 * rightsideUnitAmount, 256.0, end_z + 15 * behindUnitAmount)
        }

        Util.Direction.EAST -> {
          start_loc = Location(world, start_x - 15 * behindUnitAmount, 0.0, start_z + 15 * leftsideUnitAmount)
          end_loc = Location(world, end_x + 15 * aheadUnitAmount, 256.0, end_z + 15 * rightsideUnitAmount)
        }

        Util.Direction.SOUTH -> {
          start_loc = Location(world, start_x - 15 * rightsideUnitAmount, 0.0, start_z - 15 * behindUnitAmount)
          end_loc = Location(world, end_x + 15 * leftsideUnitAmount, 256.0, end_z + 15 * aheadUnitAmount)
        }

        Util.Direction.WEST -> {
          start_loc = Location(world, start_x - 15 * aheadUnitAmount, 0.0, start_z - 15 * rightsideUnitAmount)
          end_loc = Location(world, end_x + 15 * behindUnitAmount, 256.0, end_z + 15 * leftsideUnitAmount)
        }
      }//わざと何もしない。
      wgSelect(start_loc!!, end_loc!!, player)
    }

    private fun wgSelect(loc1: Location, loc2: Location, player: Player) {
      player.chat("//;")
      player.chat("//pos1 " + loc1.x.toInt() + "," + loc1.y.toInt() + "," + loc1.z.toInt())
      player.chat("//pos2 " + loc2.x.toInt() + "," + loc2.y.toInt() + "," + loc2.z.toInt())
    }

    private fun canCreateRegion(player: Player) {
      val playerData = SeichiAssist.playermap[player.uniqueId]!!
      val selection = We!!.getSelection(player)
      val manager = Wg.getRegionManager(player.world)
      val wcfg = Wg.getGlobalStateManager().get(player.world)

      if (selection == null) {
        playerData.canCreateRegion = false
      }

      val region = ProtectedCuboidRegion(player.name + "_" + playerData.regionCount,
          selection!!.getNativeMinimumPoint().toBlockVector(), selection!!.getNativeMaximumPoint().toBlockVector())
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

    private fun playerGridTemplateSave(player: Player, i: Int) {
      val playerData = SeichiAssist.playermap[player.uniqueId]!!
      val unitMap = playerData.unitMap

      player.sendMessage(ChatColor.GREEN.toString() + "グリッド式保護の現在の設定を保存しました。")
      player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
      val template = GridTemplate(unitMap[DirectionType.AHEAD]!!, unitMap[DirectionType.BEHIND]!!,
          unitMap[DirectionType.RIGHT]!!, unitMap[DirectionType.LEFT]!!)
      playerData.templateMap[i] = template
    }

    /**
     * ユニット単位における最短の始点のx,z座標を取得します。
     * @param player 該当プレイヤー
     * @return x,z座標のMap
     */
    fun getNearlyUnitStart(player: Player): Map<String, Double> {
      val result = HashMap<String, Double>()

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
    fun getNearlyUnitEnd(player: Player): Map<String, Double> {
      val startCoordinate = getNearlyUnitStart(player)

      val resultMap = HashMap<String, Double>()

      resultMap["x"] = startCoordinate["x"]!! + 14.0
      resultMap["z"] = startCoordinate["z"]!! + 14.0

      return resultMap
    }
  }
}
