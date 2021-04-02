package com.github.unchama.buildassist.listener

import cats.effect.{IO, SyncEffect, SyncIO}
import com.github.unchama.buildassist.enums.{LineFillSlabPosition, LineFillStatusFlag}
import com.github.unchama.buildassist.{BuildAssist, MaterialSets}
import com.github.unchama.seichiassist.ManagedWorld._
import com.github.unchama.seichiassist.subsystems.buildcount.application.actions.IncrementBuildExpWhenBuiltWithSkill
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.{MineStackObjectList, SeichiAssist}
import com.github.unchama.util.external.ExternalPlugins
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack
import org.bukkit.{Location, Material, Sound}

class BlockLineUpTriggerListener[
  F[_]
  : IncrementBuildExpWhenBuiltWithSkill[*[_], Player]
  : SyncEffect
](implicit manaApi: ManaApi[IO, SyncIO, Player]) extends Listener {

  @EventHandler
  def onBlockLineUpSkillTrigger(event: PlayerInteractEvent): Unit = {
    val player = event.getPlayer
    val action = event.getAction
    val playerWorld = player.getWorld

    val buildAssistData = BuildAssist.instance.temporaryData(player.getUniqueId)


    //スキルOFFなら終了
    if (buildAssistData.lineFillStatus == LineFillStatusFlag.Disabled) return

    //スキル利用可能でないワールドの場合終了
    if (!player.getWorld.isBlockLineUpSkillEnabled) return

    //左クリックの処理
    if (!(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) return

    //プレイヤーインベントリを取得
    val inventory = player.getInventory
    //メインハンドとオフハンドを取得
    val mainHandItem = inventory.getItemInMainHand
    val offhandItem = inventory.getItemInOffHand

    //メインハンドに設置対象ブロックがある場合
    if (!(MaterialSets.targetForLineFill.contains(mainHandItem.getType) || MaterialSets.halfBlocks.contains(mainHandItem.getType))) return

    //オフハンドに木の棒を持ってるときのみ発動させる
    if (offhandItem.getType != Material.STICK) return

    val pl = player.getLocation
    val mainHandItemType = mainHandItem.getType
    val mainHandItemData = mainHandItem.getData.getData

    //仰角は下向きがプラスで上向きがマイナス
    val pitch = pl.getPitch
    val yaw = (pl.getYaw + 360) % 360

    val manaConsumptionPerPlacement = BuildAssist.config.getLineFillManaCostMultiplier

    val mineStackObjectToBeUsed = Option.when(buildAssistData.lineFillPrioritizeMineStack) {
      MineStackObjectList.minestacklist.find { obj =>
        mainHandItem.getType == obj.material && mainHandItemData.toInt == obj.durability
      }
    }.flatten

    val playerMineStack = SeichiAssist.playermap(player.getUniqueId).minestack

    val maxBlockUsage = {
      val availableOnHand = mainHandItem.getAmount.toLong
      val availableInMineStack = mineStackObjectToBeUsed.map {
        playerMineStack.getStackedAmountOf
      }.getOrElse {
        0L
      }

      val available = availableOnHand + availableInMineStack

      val manaCap: Option[Long] = {
        val availableMana = manaApi.readManaAmount(player).unsafeRunSync().manaAmount.value

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

    val holdPlayerSlab = MaterialSets.halfBlocks.contains(mainHandItemType)
    val slabPosition = buildAssistData.lineFillSlabPosition
    val placeDoubleSlabs = holdPlayerSlab && slabPosition == LineFillSlabPosition.Both

    val placingBlockData: Byte =
      if (holdPlayerSlab && slabPosition == LineFillSlabPosition.Upper)
        (mainHandItemData + 8).toByte
      else mainHandItemData

    val (placingBlockType, itemConsumptionPerPlacement, placementIteration) =
      if (placeDoubleSlabs)
        (slabToDoubleSlab(mainHandItemType), 2, maxBlockUsage / 2)
      else
        (mainHandItemType, 1, maxBlockUsage)

    //プレイヤーの足の座標を取得
    var px = pl.getBlockX
    var py = (pl.getY + 1.6).toInt
    var pz = pl.getBlockZ

    // プレイヤーの向いてる方向を判定し、進むベクトルを確定する
    val (dx, dy, dz) = if (pitch > 45) {
      py = pl.getBlockY
      (0, -1, 0)
    } else if (pitch < -45) {
      (0, 1, 0)
    } else {
      if (buildAssistData.lineFillStatus == LineFillStatusFlag.LowerSide) {
        //下設置設定の場合は一段下げる
        py -= 1
      }
      if (yaw > 315 || yaw < 45) { //南
        (0, 0, 1)
      } else if (yaw < 135) { //西
        (-1, 0, 0)
      } else if (yaw < 225) { //北
        (0, 0, -1)
      } else { //東
        (1, 0, 0)
      }
    }

    //設置した数
    var placedBlockCount = 0

    val destroyWeakBlock = buildAssistData.lineFillDestructWeakBlocks
    val weakBlocks = MaterialSets.autoDestructibleWhenLineFill

    // 置く回数が限度よりも少なく、かつポイントする場所にブロックを設置することができ、かつポイントするブロックが空気であるか、
    // あるいはポイントするブロックのカインドがWeak-Blockとして登録されていて、
    // かつプレイヤーがWeak-Blockを自動破壊する設定を有効にしている間
    while (
      placedBlockCount < placementIteration &&
        ExternalPlugins.getWorldGuard.canBuild(player, new Location(playerWorld, px, py, pz)) &&
        (playerWorld.getBlockAt(px, py, pz).getType match {
          case Material.AIR => true
          case x @ _ => destroyWeakBlock && weakBlocks.contains(x)
        })
    ) {
      px += dx
      py += dy
      pz += dz
      val block = playerWorld.getBlockAt(px, py, pz)

      block.setType(placingBlockType)
      block.setData(placingBlockData)
      placedBlockCount += itemConsumptionPerPlacement
    }


    // 建築量を足す
    import cats.effect.implicits._
    IncrementBuildExpWhenBuiltWithSkill[F, Player]
      .of(player, BuildExpAmount(placedBlockCount))
      .runSync[SyncIO]
      .unsafeRunSync()

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
