package com.github.unchama.buildassist.listener

import com.github.unchama.buildassist.util.ExternalPlugins
import com.github.unchama.buildassist.{BuildAssist, Util}
import com.github.unchama.seichiassist.{MineStackObjectList, SeichiAssist}
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack
import org.bukkit.{Material, Sound}

import scala.util.control.Breaks

object BlockLineUpTriggerListener extends Listener {

  import collection.JavaConverters._

  @EventHandler
  def onBlockLineUpSkillTrigger(event: PlayerInteractEvent): Unit = {
    val player = event.getPlayer
    val action = event.getAction
    val playerWorld = player.getWorld

    val seichiAssistData = SeichiAssist.playermap.getOrElse(player.getUniqueId, return)
    val buildAssistData = BuildAssist.playermap.getOrElse(player.getUniqueId, return)

    val playerMineStack = seichiAssistData.minestack

    //スキルOFFなら終了
    if (buildAssistData.line_up_flg == 0) return

    //スキル利用可能でないワールドの場合終了
    if (!Util.isSkillEnable(player)) return

    //左クリックの処理
    if (!(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) return

    //プレイヤーインベントリを取得
    val inventory = player.getInventory
    //メインハンドとオフハンドを取得
    val mainHandItem = inventory.getItemInMainHand
    val offhandItem = inventory.getItemInOffHand

    //メインハンドに設置対象ブロックがある場合
    if (!(BuildAssist.materiallist2.contains(mainHandItem.getType) || BuildAssist.material_slab2.contains(mainHandItem.getType))) return

    //オフハンドに木の棒を持ってるときのみ発動させる
    if (offhandItem.getType != Material.STICK) return

    val pl = player.getLocation
    val mainHandItemType = mainHandItem.getType
    val mainHandItemData = mainHandItem.getData.getData

    //仰角は下向きがプラスで上向きがマイナス
    val pitch = pl.getPitch
    val yaw = (pl.getYaw + 360) % 360
    var step_x = 0
    var step_y = 0
    var step_z = 0

    //プレイヤーの足の座標を取得
    var px = pl.getBlockX
    var py = (pl.getY + 1.6).toInt
    var pz = pl.getBlockZ

    //プレイヤーの向いてる方向を判定
    if (pitch > 45) {
      step_y = -1
      py = pl.getBlockY
    } else if (pitch < -45) {
      step_y = 1
    } else {
      if (buildAssistData.line_up_flg == 2) {
        //下設置設定の場合は一段下げる
        py -= 1
      }
      if (yaw > 315 || yaw < 45) { //南
        step_z = 1
      } else if (yaw < 135) { //西
        step_x = -1
      } else if (yaw < 225) { //北
        step_z = -1
      } else { //東
        step_x = 1
      }
    }

    val manaConsumptionPerPlacement = BuildAssist.config.getblocklineupmana_mag()

    val mineStackObjectToBeUsed =
      if (buildAssistData.line_up_minestack_flg == 1)
        MineStackObjectList.minestacklist.find { obj =>
          mainHandItem.getType == obj.material && mainHandItemData.toInt == obj.durability
        }
      else None

    val maxBlockUsage = {
      val availableOnHand = mainHandItem.getAmount.toLong
      val availableInMineStack = mineStackObjectToBeUsed.map {
        playerMineStack.getStackedAmountOf
      }.getOrElse {
        0L
      }

      val available = availableOnHand + availableInMineStack

      val manaCap: Option[Long] = {
        val availableMana = seichiAssistData.activeskilldata.mana.getMana

        if (availableMana < available.toDouble * manaConsumptionPerPlacement)
          Some((availableMana / manaConsumptionPerPlacement).toLong)
        else
          None
      }

      Seq(Some(available), manaCap, Some(64L)).flatten.min
      }.toInt

    def slabToDoubleSlab(material: Material) = material match {
      case Material.STONE_SLAB2 => Material.DOUBLE_STONE_SLAB2
      case Material.PURPUR_SLAB => Material.PURPUR_DOUBLE_SLAB
      case Material.WOOD_STEP => Material.WOOD_DOUBLE_STEP
      case Material.STEP => Material.DOUBLE_STEP
      case _ => mainHandItemType
    }

    val playerHoldsSlabBlock = BuildAssist.material_slab2.contains(mainHandItemType)
    val slabLineUpStepMode = buildAssistData.line_up_step_flg
    val shouldPlaceDoubleSlabs = playerHoldsSlabBlock && slabLineUpStepMode == 2

    val placingBlockData: Byte =
      if (playerHoldsSlabBlock && slabLineUpStepMode == 0)
        (mainHandItemData + 8).toByte
      else mainHandItemData

    val (placingBlockType, itemConsumptionPerPlacement, placementIteration) =
      if (shouldPlaceDoubleSlabs)
        (slabToDoubleSlab(mainHandItemType), 2, maxBlockUsage / 2)
      else
        (mainHandItemType, 1, maxBlockUsage)

    //設置した数
    var placedBlockCount = 0

    val b = new Breaks
    b.breakable {
      while (placedBlockCount < placementIteration) { //設置ループ
        px += step_x
        py += step_y
        pz += step_z
        val block = playerWorld.getBlockAt(px, py, pz)

        //他人の保護がかかっている場合は設置終わり
        if (!ExternalPlugins.getWorldGuard.canBuild(player, block.getLocation)) b.break

        if (block.getType != Material.AIR) {
          //空気以外にぶつかり、ブロック破壊をしないならば終わる
          if (!BuildAssist.material_destruction.contains(block.getType) || buildAssistData.line_up_des_flg == 0) {
            b.break
          }

          block.getDrops.asScala.foreach {
            playerWorld.dropItemNaturally(player.getLocation, _)
          }
        }

        block.setType(placingBlockType)
        block.setData(placingBlockData)

        placedBlockCount += itemConsumptionPerPlacement
      }
    }

    //カウント対象ワールドの場合カウント値を足す
    if (Util.inTrackedWorld(player)) {
      //対象ワールドかチェック
      Util.addBuild1MinAmount(player, new java.math.BigDecimal(placedBlockCount * BuildAssist.config.getBlockCountMag)) //設置した数を足す
    }

    val consumptionFromMainHand = mineStackObjectToBeUsed match {
      case Some(obj) =>
        val mineStackAmount = playerMineStack.getStackedAmountOf(obj)
        val consumptionFromMineStack = Math.min(placedBlockCount.toLong, mineStackAmount)

        playerMineStack.subtractStackedAmountOf(obj, consumptionFromMineStack)

        placedBlockCount - consumptionFromMineStack.toInt
      case _ => placedBlockCount
    }

    // メインハンドのアイテム数を調整する
    if (mainHandItem.getAmount - consumptionFromMainHand > 0) {
      mainHandItem.setAmount(mainHandItem.getAmount - consumptionFromMainHand)
    } else {
      //アイテム数が0になっても消えないので自前で消す
      inventory.setItemInMainHand(new ItemStack(Material.AIR, -1))
    }

    player.playSound(player.getLocation, Sound.BLOCK_STONE_PLACE, 1f, 1f)
  }
}
