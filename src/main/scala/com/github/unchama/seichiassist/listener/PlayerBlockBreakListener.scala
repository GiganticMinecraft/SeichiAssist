package com.github.unchama.seichiassist.listener

import cats.effect.{Fiber, IO}
import com.github.unchama.seichiassist.MaterialSets.{BlockBreakableBySkill, BreakTool}
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.seichiskill.ActiveSkillRange.MultiArea
import com.github.unchama.seichiassist.seichiskill.SeichiSkillUsageMode.Disabled
import com.github.unchama.seichiassist.seichiskill.{BlockSearching, BreakArea}
import com.github.unchama.seichiassist.util.{BreakUtil, Util}
import com.github.unchama.seichiassist.{MaterialSets, SeichiAssist}
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.util.effect.BukkitResources
import com.github.unchama.util.external.ExternalPlugins
import org.bukkit.ChatColor.RED
import org.bukkit._
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}
import org.bukkit.inventory.ItemStack

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks

class PlayerBlockBreakListener extends Listener {
  private val plugin = SeichiAssist.instance

  import plugin.activeSkillAvailability

  //アクティブスキルの実行
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  def onPlayerActiveSkillEvent(event: BlockBreakEvent): Unit = {
    val player = event.getPlayer

    val block = MaterialSets.refineBlock(
      event.getBlock,
      MaterialSets.materials
    ).getOrElse(return)

    //重力値によるキャンセル判定(スキル判定より先に判定させること)
    val gravity = BreakUtil.getGravity(player, block, isAssault = false)

    if (!MaterialSets.gravityMaterials.contains(block.getType) &&
      !MaterialSets.cancelledMaterials.contains(block.getType) && gravity > 15) {

      player.sendMessage(s"${RED}整地ワールドでは必ず上から掘ってください。")
      event.setCancelled(true)
      return
    }

    if (!Util.seichiSkillsAllowedIn(player.getWorld)) return

    //破壊不可能ブロックの時処理を終了
    if (!BreakUtil.canBreak(player, block)) {
      event.setCancelled(true)
      return
    }

    //実際に使用するツール
    val tool: BreakTool = MaterialSets.refineItemStack(
      player.getInventory.getItemInMainHand,
      MaterialSets.breakToolMaterials
    ).getOrElse(return)

    // 耐久値がマイナスかつ耐久無限ツールでない時処理を終了
    if (tool.getDurability > tool.getType.getMaxDurability && !tool.getItemMeta.isUnbreakable) return

    // もしサバイバルでなければ、またはフライ中なら終了
    if (player.getGameMode != GameMode.SURVIVAL || player.isFlying) return

    val playerData = SeichiAssist.playermap(player.getUniqueId)
    val skillState = playerData.skillState.get.unsafeRunSync()
    val playerLevel = playerData.level

    //クールダウンタイム中は処理を終了
    if (!activeSkillAvailability(player).get.unsafeRunSync()) {
      //SEを再生
      player.playSound(player.getLocation, Sound.BLOCK_DISPENSER_FAIL, 0.5f, 1)
      return
    }

    val manaState = playerData.manaState

    // 追加マナ獲得
    manaState.increase(BreakUtil.calcManaDrop(player), player, playerLevel)

    val selectedSkill = skillState.activeSkill.getOrElse(return)

    if (!selectedSkill.range.isInstanceOf[MultiArea] || skillState.usageMode == Disabled) return

    event.setCancelled(true)

    {
      //プレイヤーの足のy座標を取得
      val playerLocY = player.getLocation.getBlockY - 1
      val centerOfBlock = block.getLocation.add(0.5, 0.5, 0.5)

      val skillArea = BreakArea(selectedSkill, skillState.usageMode)
      val breakAreaList = skillArea.makeBreakArea(player).unsafeRunSync()

      val isMultiTypeBreakingSkillEnabled = {
        import com.github.unchama.seichiassist.ManagedWorld._
        playerLevel >= SeichiAssist.seichiAssistConfig.getMultipleIDBlockBreaklevel &&
          (player.getWorld.isSeichi || playerData.settings.multipleidbreakflag)
      }

      val totalBreakRangeVolume = {
        val breakLength = skillArea.breakLength
        breakLength.x * breakLength.y * breakLength.z * skillArea.breakNum
      }

      //エフェクト用に壊されるブロック全てのリストデータ
      val multiBreakList = new ArrayBuffer[Set[BlockBreakableBySkill]]
      //壊される溶岩の全てのリストデータ
      val multiLavaList = new ArrayBuffer[Set[Block]]
      // 全てのマナ消費量
      var manaConsumption = 0.0
      // 全ての耐久消費量
      var toolDamageToSet = tool.getDurability.toInt

      //繰り返し回数だけ繰り返す
      val b = new Breaks
      b.breakable {
        breakAreaList.foreach { breakArea =>
          import com.github.unchama.seichiassist.data.syntax._

          val BlockSearching.Result(breakBlocks, _, lavaBlocks) =
            BlockSearching
              .searchForBlocksBreakableWithSkill(player, breakArea.gridPoints(), block)
              .unsafeRunSync()
              .filterSolids(targetBlock =>
                isMultiTypeBreakingSkillEnabled || BlockSearching.multiTypeBreakingFilterPredicate(block)(targetBlock)
              )
              .filterAll(targetBlock =>
                player.isSneaking || targetBlock.getLocation.getBlockY > playerLocY || targetBlock == block
              )

          //減る経験値計算
          manaConsumption += (gravity + 1) * selectedSkill.manaCost * (breakBlocks.size + 1).toDouble / totalBreakRangeVolume

          //減る耐久値の計算(１マス溶岩を破壊するのにはブロック１０個分の耐久が必要)
          toolDamageToSet += BreakUtil.calcDurability(
            tool.getEnchantmentLevel(Enchantment.DURABILITY), breakBlocks.size + 10 * lavaBlocks.size
          )

          //実際に経験値を減らせるか判定
          if (!manaState.has(manaConsumption))
            b.break()

          //実際に耐久値を減らせるか判定
          if (tool.getType.getMaxDurability <= toolDamageToSet && !tool.getItemMeta.isUnbreakable)
            b.break()

          multiBreakList.addOne(breakBlocks.toSet)
          multiLavaList.addOne(lavaBlocks.toSet)
        }
      }

      if (multiBreakList.headOption.forall(_.size == 1)) {
        // 破壊するブロックがプレーヤーが最初に破壊を試みたブロックだけの場合
        BreakUtil.breakBlock(player, block, centerOfBlock, tool, shouldPlayBreakSound = true)
      } else {
        // スキルの処理
        import cats.implicits._
        import com.github.unchama.concurrent.syntax._
        import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{asyncShift, cachedThreadPool, syncShift}

        val effectPrograms = for {
          ((blocks, lavas), chunkIndex) <- multiBreakList.zip(multiLavaList).zipWithIndex
          blockChunk = BukkitResources.vanishingBlockSetResource(blocks)
        } yield {
          SeichiAssist.instance.lockedBlockChunkScope.useTracked(blockChunk) { blocks =>
            for {
              _ <- IO.sleep((chunkIndex * 4).ticks)(IO.timer(cachedThreadPool))
              _ <- syncShift.shift
              _ <- IO { lavas.foreach(_.setType(Material.AIR)) }
              _ <-
                playerData.skillEffectState.selection.runBreakEffect(
                  player, selectedSkill, tool, blocks,
                  breakAreaList(chunkIndex), block.getLocation.add(0.5, 0.5, 0.5)
                )
            } yield ()
          }.start(asyncShift)
        }

        //壊したブロック数に応じてクールダウンを発生させる
        val availabilityFlagManipulation = {
          val brokenBlockNum = multiBreakList.map(_.size).sum
          val coolDownTicks =
            (selectedSkill.maxCoolDownTicks.getOrElse(0).toDouble * brokenBlockNum / totalBreakRangeVolume)
              .ceil
              .toInt

          val reference = SeichiAssist.instance.activeSkillAvailability(player)

          for {
            _ <- reference.set(false)
            _ <- IO.timer(PluginExecutionContexts.sleepAndRoutineContext).sleep(coolDownTicks.ticks)
            _ <- reference.set(true)
            _ <- FocusedSoundEffect(Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 0.1f).run(player)
          } yield ()
        }

        // マナやツールの耐久値を減らす
        val adjustManaAndDurability = IO {
          manaState.decrease(manaConsumption, player, playerLevel)
          if (!tool.getItemMeta.isUnbreakable) tool.setDurability(toolDamageToSet.toShort)
        }

        com.github.unchama.seichiassist.unsafe.runIOAsync(
          "複数破壊エフェクトを実行する",
          effectPrograms.toList.sequence[IO, Fiber[IO, Unit]]
        )
        com.github.unchama.seichiassist.unsafe.runIOAsync(
          "複数破壊エフェクトの後処理を実行する",
          adjustManaAndDurability >> availabilityFlagManipulation
        )
      }
    }
  }

  /**
   * y5ハーフブロック破壊抑制
   *
   * @param event BlockBreakEvent
   */
  @EventHandler(priority = EventPriority.LOWEST)
  @SuppressWarnings(Array("deprecation"))
  def onPlayerBlockHalf(event: BlockBreakEvent): Unit = {
    val p = event.getPlayer
    val b = event.getBlock
    val world = p.getWorld
    val data = SeichiAssist.playermap.apply(p.getUniqueId)
    //そもそも自分の保護じゃなきゃ処理かけない
    if (!ExternalPlugins.getWorldGuard.canBuild(p, b.getLocation)) return
    if ((b.getType eq Material.DOUBLE_STEP) && b.getData == 0) {
      b.setType(Material.STEP)
      b.setData(0.toByte)
      val location = b.getLocation
      world.dropItemNaturally(location, new ItemStack(Material.STEP))
    }
    if (b.getType ne Material.STEP) return
    if (b.getY != 5) return
    if (b.getData != 0) return
    if (!world.getName.toLowerCase.startsWith(SeichiAssist.SEICHIWORLDNAME)) return
    if (data.canBreakHalfBlock) return
    event.setCancelled(true)
    p.sendMessage(s"${RED}Y5に敷かれたハーフブロックは破壊不可能です.")
  }
}
