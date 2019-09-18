package com.github.unchama.buildassist

import com.github.unchama.buildassist.util.ExternalPlugins
import com.github.unchama.seichiassist.MineStackObjectList
import com.github.unchama.seichiassist.SeichiAssist
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.math.BigDecimal
import kotlin.math.min

class BlockLineUp : Listener {

  @EventHandler
  fun onPlayerClick(event: PlayerInteractEvent) {
    val player = event.player
    val action = event.action
    val playerWorld = player.world

    val seichiAssistData = SeichiAssist.playermap[player.uniqueId] ?: return
    val buildAssistData = BuildAssist.playermap[player.uniqueId] ?: return

    val playerMineStack = seichiAssistData.minestack

    //スキルOFFなら終了
    if (buildAssistData.line_up_flg == 0) return

    //スキル利用可能でないワールドの場合終了
    if (!Util.isSkillEnable(player)) return

    //左クリックの処理
    if (!(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) return

    //プレイヤーインベントリを取得
    val inventory = player.inventory
    //メインハンドとオフハンドを取得
    val mainHandItem = inventory.itemInMainHand
    val offhandItem = inventory.itemInOffHand

    //メインハンドに設置対象ブロックがある場合
    if (!(BuildAssist.materiallist2.contains(mainHandItem.type) || BuildAssist.material_slab2.contains(mainHandItem.type))) return

    //オフハンドに木の棒を持ってるときのみ発動させる
    if (offhandItem.type != Material.STICK) return

    val pl = player.location
    val mainHandItemType = mainHandItem.type
    val mainHandItemData = mainHandItem.data.data

    //仰角は下向きがプラスで上向きがマイナス
    val pitch = pl.pitch
    val yaw = (pl.yaw + 360) % 360
    var step_x = 0
    var step_y = 0
    var step_z = 0

    //プレイヤーの足の座標を取得
    var px = pl.blockX
    var py = (pl.y + 1.6).toInt()
    var pz = pl.blockZ

    //プレイヤーの向いてる方向を判定
    if (pitch > 45) {
      step_y = -1
      py = pl.blockY
    } else if (pitch < -45) {
      step_y = 1
    } else {
      if (buildAssistData.line_up_flg == 2) {
        //下設置設定の場合は一段下げる
        py--
      }
      if (yaw > 315 || yaw < 45) {//南
        step_z = 1
      } else if (yaw < 135) {//西
        step_x = -1
      } else if (yaw < 225) {//北
        step_z = -1
      } else {//東
        step_x = 1
      }
    }

    val manaConsumptionPerPlacement = BuildAssist.config.getblocklineupmana_mag()

    val mineStackObjectToBeUsed =
        if (buildAssistData.line_up_minestack_flg == 1)
          MineStackObjectList.minestacklist!!.find {
            mainHandItem.type == it.material && mainHandItemData.toInt() == it.durability
          }
        else null

    val maxBlockUsage = run {
      val availableOnHand = mainHandItem.amount
      val availableInMineStack = mineStackObjectToBeUsed?.let { playerMineStack.getStackedAmountOf(it) } ?: 0

      val available = availableOnHand + availableInMineStack

      val manaCap = run {
        val availableMana = seichiAssistData.activeskilldata.mana.mana

        if (availableMana < available.toDouble() * manaConsumptionPerPlacement)
          (availableMana / manaConsumptionPerPlacement).toLong()
        else
          null
      }

      listOfNotNull(available, manaCap, 64L).min()!!
    }.toInt()

    fun slabToDoubleSlab(material: Material) = when (material) {
      Material.STONE_SLAB2 -> Material.DOUBLE_STONE_SLAB2
      Material.PURPUR_SLAB -> Material.PURPUR_DOUBLE_SLAB
      Material.WOOD_STEP -> Material.WOOD_DOUBLE_STEP
      Material.STEP -> Material.DOUBLE_STEP
      else -> mainHandItemType
    }

    val playerHoldsSlabBlock = BuildAssist.material_slab2.contains(mainHandItemType)
    val slabLineUpStepMode = buildAssistData.line_up_step_flg
    val shouldPlaceDoubleSlabs = playerHoldsSlabBlock && slabLineUpStepMode == 2

    val placingBlockData: Byte =
        if (playerHoldsSlabBlock && slabLineUpStepMode == 0)
          (mainHandItemData + 8).toByte()
        else mainHandItemData

    val (placingBlockType, itemConsumptionPerPlacement, placementIteration) =
        if (shouldPlaceDoubleSlabs)
          Triple(slabToDoubleSlab(mainHandItemType), 2, maxBlockUsage / 2)
        else
          Triple(mainHandItemType, 1, maxBlockUsage)

    //設置した数
    var placedBlockCount = 0
    while (placedBlockCount < placementIteration) {//設置ループ
      px += step_x
      py += step_y
      pz += step_z
      val block = playerWorld.getBlockAt(px, py, pz)

      //他人の保護がかかっている場合は設置終わり
      if (!ExternalPlugins.getWorldGuard().canBuild(player, block.location)) break

      if (block.type != Material.AIR) {
        //空気以外にぶつかり、ブロック破壊をしないならば終わる
        if (!BuildAssist.material_destruction.contains(block.type) || buildAssistData.line_up_des_flg == 0) {
          break
        }

        block.drops.forEach {
          playerWorld.dropItemNaturally(player.location, it)
        }
      }

      block.type = placingBlockType
      block.data = placingBlockData

      placedBlockCount += itemConsumptionPerPlacement
    }

    //カウント対象ワールドの場合カウント値を足す
    if (Util.isBlockCount(player)) {
      //対象ワールドかチェック
      Util.addBuild1MinAmount(player, BigDecimal(placedBlockCount * BuildAssist.config.blockCountMag))  //設置した数を足す
    }

    val consumptionFromMainHand = if (mineStackObjectToBeUsed != null) {
      val mineStackAmount = playerMineStack.getStackedAmountOf(mineStackObjectToBeUsed)
      val consumptionFromMineStack = min(placedBlockCount.toLong(), mineStackAmount)

      playerMineStack.subtractStackedAmountOf(mineStackObjectToBeUsed, consumptionFromMineStack)

      placedBlockCount - consumptionFromMineStack.toInt()
    } else placedBlockCount

    // メインハンドのアイテム数を調整する
    if (mainHandItem.amount - consumptionFromMainHand > 0) {
      mainHandItem.amount -= consumptionFromMainHand
    } else {
      //アイテム数が0になっても消えないので自前で消す
      inventory.itemInMainHand = ItemStack(Material.AIR, -1)
    }

    player.playSound(player.location, Sound.BLOCK_STONE_PLACE, 1f, 1f)
  }
}
