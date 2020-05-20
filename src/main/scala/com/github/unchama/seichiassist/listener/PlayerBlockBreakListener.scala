package com.github.unchama.seichiassist.listener

import cats.effect.{Fiber, IO}
import com.github.unchama.seichiassist.MaterialSets.{BlockBreakableBySkill, BreakTool}
import com.github.unchama.seichiassist.seichiskill.ActiveSkillRange.MultiArea
import com.github.unchama.seichiassist.seichiskill.ActiveSkillUsageMode.Disabled
import com.github.unchama.seichiassist.seichiskill.{BlockSearching, BreakArea}
import com.github.unchama.seichiassist.task.CoolDownTask
import com.github.unchama.seichiassist.util.{BreakUtil, Util}
import com.github.unchama.seichiassist.{MaterialSets, SeichiAssist}
import com.github.unchama.util.effect.BukkitResources
import com.github.unchama.util.external.ExternalPlugins
import org.bukkit.ChatColor.RED
import org.bukkit._
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}
import org.bukkit.inventory.ItemStack

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks

class PlayerBlockBreakListener extends Listener {
  private val plugin = SeichiAssist.instance

  @EventHandler(priority = EventPriority.LOW)
  def onPlayerBlockBreak(event: BlockBreakEvent): Unit = {
    val block = event.getBlock

    //他人の保護がかかっている場合は処理を終了
    if (!ExternalPlugins.getWorldGuard.canBuild(event.getPlayer, block.getLocation)) return

    // 保護と重力値に問題無く、ブロックタイプがmateriallistに登録されていたらMebiusListenerを呼び出す
    if (MaterialSets.materials.contains(event.getBlock.getType))
      MebiusListener.onBlockBreak(event)
  }

  //アクティブスキルの実行
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  def onPlayerActiveSkillEvent(event: BlockBreakEvent): Unit = {
    val player = event.getPlayer

    val block = MaterialSets.refineBlock(
      event.getBlock,
      MaterialSets.materials
    ).getOrElse(return)

    //UUIDを基にプレイヤーデータ取得
    val playerData = SeichiAssist.playermap(player.getUniqueId)

    //重力値によるキャンセル判定(スキル判定より先に判定させること)
    if (!MaterialSets.gravityMaterials.contains(block.getType) && !MaterialSets.cancelledMaterials.contains(block.getType))
      if (BreakUtil.getGravity(player, block, isAssault = false) > 15) {
        player.sendMessage(RED + "整地ワールドでは必ず上から掘ってください。")
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
    val tool = MaterialSets.refineItemStack(
      player.getInventory.getItemInMainHand,
      MaterialSets.breakToolMaterials
    ).getOrElse(return)

    //耐久値がマイナスかつ耐久無限ツールでない時処理を終了
    if (tool.getDurability > tool.getType.getMaxDurability && !tool.getItemMeta.isUnbreakable) return

    //もしサバイバルでなければ処理を終了
    //もしフライ中なら終了
    if ((player.getGameMode ne GameMode.SURVIVAL) || player.isFlying) {
      if (SeichiAssist.DEBUG) player.sendMessage(s"${RED}fly中の破壊")
      return
    }

    val skillState = playerData.skillState

    //クールダウンタイム中は処理を終了
    if (!skillState.isActiveSkillAvailable) {
      //SEを再生
      player.playSound(player.getLocation, Sound.BLOCK_DISPENSER_FAIL, 0.5f, 1)
      return
    }

    // 追加マナ獲得
    playerData.manaState.increase(BreakUtil.calcManaDrop(playerData), player, playerData.level)

    // これ以降の終了処理はマナが回復します
    val selectedSkill = skillState.activeSkill.getOrElse(return)

    if (!selectedSkill.range.isInstanceOf[MultiArea] || skillState.usageMode == Disabled) return

    event.setCancelled(true)

    runMultiSkill(player, block, tool)
  }

  //複数範囲破壊
  private def runMultiSkill(player: Player, block: BlockBreakableBySkill, tool: BreakTool): Unit = {
    val playerData = SeichiAssist.playermap(player.getUniqueId)

    val skillState = playerData.skillState
    val skill = skillState.activeSkill.get
    val mana = playerData.manaState

    //プレイヤーの足のy座標を取得
    val playerLocY = player.getLocation.getBlockY - 1
    val centerOfBlock = block.getLocation.add(0.5, 0.5, 0.5)

    val skillArea = BreakArea(skill, skillState.usageMode)
    val breakAreaList = skillArea.makeBreakArea(player).unsafeRunSync()

    //エフェクト用に壊されるブロック全てのリストデータ
    val multiBreakList = new ArrayBuffer[Set[BlockBreakableBySkill]]
    //壊される溶岩の全てのリストデータ
    val multiLavaList = new ArrayBuffer[Set[Block]]

    // 一回の破壊の範囲
    val breakLength = skillArea.breakLength
    // 全て破壊したときのブロック数
    val totalBreakRangeVolume = breakLength.x * breakLength.y * breakLength.z * skillArea.breakNum

    // 全てのマナ消費量
    var manaConsumption = 0.0
    // 全ての耐久消費量
    var toolDamageToSet = tool.getDurability.toInt

    // 重力値計算
    val gravity = BreakUtil.getGravity(player, block, isAssault = false)

    val isMultiTypeBreakingSkillEnabled = {
      val playerData = SeichiAssist.playermap(player.getUniqueId)

      import com.github.unchama.seichiassist.ManagedWorld._
      playerData.level >= SeichiAssist.seichiAssistConfig.getMultipleIDBlockBreaklevel &&
        (player.getWorld.isSeichi || playerData.settings.multipleidbreakflag)
    }

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
        manaConsumption += (gravity + 1) * skill.manaCost * (breakBlocks.size + 1).toDouble / totalBreakRangeVolume

        //減る耐久値の計算
        toolDamageToSet += BreakUtil.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY), breakBlocks.size)

        //１マス溶岩を破壊するのにはブロック１０個分の耐久が必要
        toolDamageToSet += BreakUtil.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY), 10 * lavaBlocks.size)

        //重力値の判定
        if (gravity > 15) {
          player.sendMessage(RED + "スキルを使用するには上から掘ってください。")
          b.break()
        }

        //実際に経験値を減らせるか判定
        if (!mana.has(manaConsumption)) {
          if (SeichiAssist.DEBUG) player.sendMessage(RED + "アクティブスキル発動に必要なマナが足りません")
          b.break()
        }

        //実際に耐久値を減らせるか判定
        if (tool.getType.getMaxDurability <= toolDamageToSet && !tool.getItemMeta.spigot.isUnbreakable) {
          if (SeichiAssist.DEBUG) player.sendMessage(RED + "アクティブスキル発動に必要なツールの耐久値が足りません")
          b.break()
        }

        //選択されたブロックを破壊せずに保存する処理
        multiBreakList.addOne(breakBlocks.toSet)
        multiLavaList.addOne(lavaBlocks.toSet)
      }
    }

    if (multiBreakList.size == 1) {
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
                player, playerData.activeskilldata, tool, blocks,
                breakAreaList(chunkIndex), block.getLocation.add(0.5, 0.5, 0.5)
              )
          } yield ()
        }.start(asyncShift)
      }

      com.github.unchama.seichiassist.unsafe.runIOAsync(
        "複数破壊エフェクトを実行する",
        effectPrograms.toList.sequence[IO, Fiber[IO, Unit]]
      )

      //経験値を減らす
      mana.decrease(manaConsumption, player, playerData.level)

      //耐久値を減らす
      if (!tool.getItemMeta.isUnbreakable) tool.setDurability(toolDamageToSet.toShort)

      //壊したブロック数に応じてクールダウンを発生させる
      val breakBlockNum = multiBreakList.map(_.size).sum
      val coolDownTime = skill.maxCoolDownTicks.getOrElse(0) * breakBlockNum / totalBreakRangeVolume
      if (coolDownTime >= 5) new CoolDownTask(player, false, true, false).runTaskLater(plugin, coolDownTime)
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
