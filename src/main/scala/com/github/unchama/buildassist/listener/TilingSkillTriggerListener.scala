package com.github.unchama.buildassist.listener

import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, SyncEffect, SyncIO}
import com.github.unchama.buildassist.{BuildAssist, Util}
import com.github.unchama.seichiassist.subsystems.buildcount.application.actions.IncrementBuildExpWhenBuiltWithSkill
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import org.bukkit.ChatColor.RED
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack
import org.bukkit.{Location, Material}

import scala.util.chaining._
import scala.util.control.Breaks

class TilingSkillTriggerListener[G[_]: ConcurrentEffect, F[
  _
]: IncrementBuildExpWhenBuiltWithSkill[*[_], Player]: SyncEffect](
  implicit mineStackAPI: MineStackAPI[G, Player, ItemStack]
) extends Listener {

  // 範囲設置スキルの発動を担うハンドラメソッド
  @EventHandler
  def onTilingSkillTrigger(event: PlayerInteractEvent): Unit = {
    val player = event.getPlayer
    val playerUuid = player.getUniqueId

    val playerWorld = player.getWorld

    val buildAssistPlayerData = BuildAssist.instance.temporaryData(playerUuid)

    val playerInventory = player.getInventory
    val offHandItem = playerInventory.getItemInOffHand

    event.getAction match {
      case Action.LEFT_CLICK_BLOCK =>
      case _                       => return
    }

    if (
      !(player.isSneaking &&
        BuildAssist.materiallist.contains(offHandItem.getType) &&
        buildAssistPlayerData.ZoneSetSkillFlag)
    ) return

    val clickedBlock = event.getClickedBlock
    val offHandItemSelector = offHandItem.getData.getData
    if (
      !(offHandItem.getType == clickedBlock.getType && offHandItemSelector == clickedBlock.getData)
    ) {
      player.sendMessage(s"$RED「オフハンドと同じブロック」をクリックしてください。(基準になります)")
      return
    }

    // スキルの範囲設定
    val areaInt = buildAssistPlayerData.AREAint

    // 設置範囲の基準となる座標
    val centerX = clickedBlock.getX
    val surfaceY = clickedBlock.getY
    val centerZ = clickedBlock.getZ

    var itemSourceSearchInventoryIndex = 9

    var placementCount = 0

    val minestackObjectToUse =
      mineStackAPI
        .mineStackObjectList
        .findBySignedItemStack(offHandItem, player)
        .toIO
        .unsafeRunSync()
        .filter(_ => buildAssistPlayerData.zs_minestack_flag)

    val replaceableMaterials = Set(
      Material.AIR,
      Material.SNOW,
      Material.LONG_GRASS,
      Material.DEAD_BUSH,
      Material.YELLOW_FLOWER,
      Material.RED_ROSE,
      Material.RED_MUSHROOM,
      Material.BROWN_MUSHROOM
    )

    val fillTargetMaterials = Set(
      Material.AIR,
      Material.LAVA,
      Material.STATIONARY_LAVA,
      Material.WATER,
      Material.STATIONARY_WATER
    )

    val b1 = new Breaks
    val rawDataModifier: Byte => Byte = offHandItem.getType match {
      case Material.LEAVES | Material.LEAVES_2 =>
        val noDecayBit: Byte = 8
        x => (x | noDecayBit).asInstanceOf[Byte]
      case _ => identity
    }

    b1.breakable {
      val targetXValues = centerX - areaInt to centerX + areaInt
      val targetZValues = centerZ - areaInt to centerZ + areaInt

      targetZValues.foreach { targetZ =>
        targetXValues.foreach { targetX =>
          val b2 = new Breaks
          b2.breakable {
            val targetSurfaceLocation = new Location(playerWorld, targetX, surfaceY, targetZ)
            val targetSurfaceBlock = targetSurfaceLocation.getBlock

            def fillBelowSurfaceWithDirt(): Unit = {
              (1 to 5).foreach { setBlockYOffsetBelow =>
                val fillLocation =
                  new Location(playerWorld, targetX, surfaceY - setBlockYOffsetBelow, targetZ)
                val blockToBeReplaced = fillLocation.getBlock

                if (fillTargetMaterials.contains(blockToBeReplaced.getType)) {
                  if (Util.getWorldGuard.canBuild(player, fillLocation)) {
                    blockToBeReplaced.setType(Material.DIRT)
                  } else {
                    // 他人の保護がかかっている場合は通知を行う
                    player.sendMessage(s"${RED}付近に誰かの保護がかかっているようです")
                  }
                }
              }
            }

            def commitPlacement(): Unit = {
              if (buildAssistPlayerData.zsSkillDirtFlag) {
                fillBelowSurfaceWithDirt()
              }

              targetSurfaceBlock.setType(offHandItem.getType)
              targetSurfaceBlock.setData(rawDataModifier(offHandItemSelector))

              placementCount += 1
            }

            def consumeOnePlacementItemFromInventory(): Option[Unit] = {
              @scala.annotation.tailrec
              def forever(block: => Unit): Nothing = {
                block; forever(block)
              }

              // インベントリの左上から一つずつ確認する。
              // 一度「該当アイテムなし」と判断したスロットは次回以降スキップする
              forever {
                val consumptionSource = playerInventory.getItem(itemSourceSearchInventoryIndex)

                if (consumptionSource != null && consumptionSource.isSimilar(offHandItem)) {
                  val sourceStackAmount = consumptionSource.getAmount

                  // 取得したインベントリデータから数量を1ひき、インベントリに反映する
                  val updatedItem =
                    if (sourceStackAmount == 1)
                      new ItemStack(Material.AIR)
                    else
                      consumptionSource.clone().tap(_.setAmount(sourceStackAmount - 1))
                  playerInventory.setItem(itemSourceSearchInventoryIndex, updatedItem)

                  return Some(())
                } else {
                  if (itemSourceSearchInventoryIndex == 35) {
                    itemSourceSearchInventoryIndex = 0
                  } else if (itemSourceSearchInventoryIndex == 8) {
                    return None
                  } else {
                    itemSourceSearchInventoryIndex += 1
                  }
                }
              }
            }

            if (replaceableMaterials.contains(targetSurfaceBlock.getType)) {
              // 他人の保護がかかっている場合は処理を終了
              if (!Util.getWorldGuard.canBuild(player, targetSurfaceLocation)) {
                player.sendMessage(s"${RED}付近に誰かの保護がかかっているようです")
                b1.break()
              }

              minestackObjectToUse match {
                case Some(mineStackObject) =>
                  if (
                    mineStackAPI
                      .mineStackRepository
                      .getStackedAmountOf(player, mineStackObject)
                      .toIO
                      .unsafeRunSync() > 0
                  ) {
                    mineStackAPI
                      .mineStackRepository
                      .subtractStackedAmountOf(player, mineStackObject, 1)
                      .toIO
                      .unsafeRunSync()

                    commitPlacement()
                    b2.break()
                  }
                case None =>
              }

              consumeOnePlacementItemFromInventory() match {
                case Some(_) =>
                  commitPlacement()
                case None =>
                  player.sendMessage(s"${RED}アイテムが不足しています!")
                  b1.break()
              }
            }
          }
        }
      }
    }

    import cats.effect.implicits._
    IncrementBuildExpWhenBuiltWithSkill[F, Player]
      .of(player, BuildExpAmount(placementCount))
      .runSync[SyncIO]
      .unsafeRunSync()
  }

}
