package com.github.unchama.seichiassist.listener

import cats.effect.{Fiber, IO, SyncIO}
import com.github.unchama.generic.ApplicativeExtra.whenAOrElse
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.ManagedWorld._
import com.github.unchama.seichiassist.MaterialSets.{BlockBreakableBySkill, BreakTool}
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.seichiskill.ActiveSkillRange.MultiArea
import com.github.unchama.seichiassist.seichiskill.SeichiSkillUsageMode.Disabled
import com.github.unchama.seichiassist.seichiskill.{BlockSearching, BreakArea}
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.mana.domain.ManaAmount
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.util.BreakUtil
import com.github.unchama.seichiassist.{MaterialSets, SeichiAssist}
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.util.effect.BukkitResources
import com.github.unchama.util.external.WorldGuardWrapper
import org.bukkit.ChatColor.RED
import org.bukkit._
import org.bukkit.block.Block
import org.bukkit.block.data.`type`.Slab
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.control.Breaks

class PlayerBlockBreakListener(
  implicit effectEnvironment: EffectEnvironment,
  ioOnMainThread: OnMinecraftServerThread[IO],
  manaApi: ManaApi[IO, SyncIO, Player],
  mineStackAPI: MineStackAPI[IO, Player, ItemStack]
) extends Listener {
  private val plugin = SeichiAssist.instance

  import cats.implicits._
  import plugin.activeSkillAvailability

  // アクティブスキルの実行
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  def onPlayerActiveSkillEvent(event: BlockBreakEvent): Unit = {
    val player = event.getPlayer

    val block = MaterialSets
      .refineBlock(event.getBlock, MaterialSets.materials)
      .getOrElse(
        return
      )

    // 重力値によるキャンセル判定(スキル判定より先に判定させること)
    val gravity = BreakUtil.getGravity(player, block, isAssault = false)

    if (
      !MaterialSets.gravityMaterials.contains(block.getType) &&
      !MaterialSets.cancelledMaterials.contains(block.getType) && gravity > 15
    ) {

      player.playSound(player.getLocation, Sound.BLOCK_ANVIL_FALL, 0.0f, -1.0f)
      player.sendMessage(s"${RED}整地ワールドでは必ず上から掘ってください。")
      event.setCancelled(true)
      return
    }

    if (!player.getWorld.isSeichiSkillAllowed) return

    // 破壊不可能ブロックの時処理を終了
    if (!BreakUtil.canBreakWithSkill(player, block)) {
      event.setCancelled(true)
      return
    }

    // 実際に使用するツール
    val tool: BreakTool = MaterialSets
      .refineItemStack(player.getInventory.getItemInMainHand, MaterialSets.breakToolMaterials)
      .getOrElse(
        return
      )

    // 耐久値がマイナスかつ耐久無限ツールでない時処理を終了
    if (
      tool.getItemMeta.asInstanceOf[Damageable].getDamage > tool
        .getType
        .getMaxDurability && !tool.getItemMeta.isUnbreakable
    )
      return

    // もしサバイバルでなければ、またはフライ中なら終了
    if (player.getGameMode != GameMode.SURVIVAL || player.isFlying) return

    val playerData = SeichiAssist.playermap(player.getUniqueId)
    val skillState = playerData.skillState.get.unsafeRunSync()

    if (!player.getWorld.isSeichiSkillAllowed) return

    // クールダウンタイム中は処理を終了
    if (!activeSkillAvailability(player).get.unsafeRunSync()) {
      // SEを再生
      player.playSound(player.getLocation, Sound.BLOCK_DISPENSER_FAIL, 0.5f, 1)
      return
    }

    // 追加マナ獲得
    manaApi
      .manaAmount(player)
      .restoreAbsolute(ManaAmount(BreakUtil.calcManaDrop(player)))
      .unsafeRunSync()

    val selectedSkill = skillState
      .activeSkill
      .getOrElse(
        return
      )

    if (!selectedSkill.range.isInstanceOf[MultiArea] || skillState.usageMode == Disabled) return

    event.setCancelled(true)

    {
      // プレイヤーの足のy座標を取得
      val playerLocY = player.getLocation.getBlockY - 1

      val skillArea = BreakArea(selectedSkill, skillState.usageMode)
      val breakAreaList = skillArea.makeBreakArea(player).unsafeRunSync()

      val isMultiTypeBreakingSkillEnabled =
        BreakUtil.performsMultipleIDBlockBreakWhenUsingSkills(player).unsafeRunSync()

      val totalBreakRangeVolume = {
        val breakLength = skillArea.breakLength
        breakLength.x * breakLength.y * breakLength.z * skillArea.breakNum
      }

      // エフェクト用に壊されるブロック全てのリストデータ
      val multiBreakList = new ArrayBuffer[Set[BlockBreakableBySkill]]
      // 壊される溶岩の全てのリストデータ
      val multiLavaList = new ArrayBuffer[Set[Block]]
      // 壊される水ブロックの全てのリストデータ
      val multiWaterList = new ArrayBuffer[Set[Block]]
      // 全ての耐久消費量
      var toolDamageToSet = tool.getItemMeta.asInstanceOf[Damageable].getDamage

      // 消費が予約されたマナ
      val reservedMana = new ArrayBuffer[ManaAmount]

      // 繰り返し回数だけ繰り返す
      val b = new Breaks
      b.breakable {
        breakAreaList.foreach { breakArea =>
          import com.github.unchama.seichiassist.data.syntax._

          val BlockSearching.Result(breakBlocks, waterBlocks, lavaBlocks) =
            BlockSearching
              .searchForBlocksBreakableWithSkill(player, breakArea.gridPoints(), block)
              .unsafeRunSync()
              .filterSolids(targetBlock =>
                isMultiTypeBreakingSkillEnabled || BlockSearching
                  .multiTypeBreakingFilterPredicate(block)(targetBlock)
              )
              .filterAll(targetBlock =>
                player.isSneaking || targetBlock
                  .getLocation
                  .getBlockY > playerLocY || targetBlock == block
              )

          // このチャンクで消費されるマナ
          val manaToConsumeOnThisChunk = ManaAmount {
            (gravity + 1) * selectedSkill.manaCost * (breakBlocks.size + 1).toDouble / totalBreakRangeVolume
          }

          // マナを消費する
          manaApi
            .manaAmount(player)
            .tryAcquire(manaToConsumeOnThisChunk)
            .unsafeRunSync() match {
            case Some(value) => reservedMana.addOne(value)
            case None        => b.break()
          }

          // 減る耐久値の計算(溶岩及び水を破壊するとブロック１０個分の耐久値減少判定を行う)
          toolDamageToSet += BreakUtil.calcDurability(
            tool.getEnchantmentLevel(Enchantment.DURABILITY),
            breakBlocks.size + 10 * (lavaBlocks.size + waterBlocks.size)
          )

          // 実際に耐久値を減らせるか判定
          if (
            tool.getType.getMaxDurability <= toolDamageToSet && !tool.getItemMeta.isUnbreakable
          )
            b.break()

          multiBreakList.addOne(breakBlocks.toSet)
          multiLavaList.addOne(lavaBlocks.toSet)
          multiWaterList.addOne(waterBlocks.toSet)
        }
      }

      if (multiBreakList.headOption.forall(_.size == 1)) {
        // 破壊するブロックがプレーヤーが最初に破壊を試みたブロックだけの場合
        event.setCancelled(false)
        reservedMana.toList.traverse(manaApi.manaAmount(player).restoreAbsolute).unsafeRunSync()
      } else {
        // スキルの処理
        import cats.implicits._
        import com.github.unchama.concurrent.syntax._
        import com.github.unchama.generic.ContextCoercion._
        import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{
          asyncShift,
          cachedThreadPool
        }

        val effectPrograms = for {
          ((blocks, lavas, waters), chunkIndex) <-
            (multiBreakList lazyZip
              multiLavaList lazyZip
              multiWaterList).zipWithIndex.toList
          blockChunk = BukkitResources.vanishingBlockSetResource[IO, BlockBreakableBySkill](
            blocks
          )
        } yield {
          SeichiAssist
            .instance
            .lockedBlockChunkScope
            .useTracked(blockChunk) { blocks =>
              for {
                _ <- IO.sleep((chunkIndex * 4).ticks)(IO.timer(cachedThreadPool))
                _ <- ioOnMainThread.runAction(SyncIO {
                  (lavas ++ waters).foreach(_.setType(Material.AIR))
                })
                _ <- playerData
                  .skillEffectState
                  .selection
                  .runBreakEffect(
                    player,
                    selectedSkill,
                    tool,
                    blocks,
                    breakAreaList(chunkIndex),
                    block.getLocation.add(0.5, 0.5, 0.5)
                  )
              } yield ()
            }
            .start(asyncShift)
        }

        // 壊したブロック数に応じてクールダウンを発生させる
        val availabilityFlagManipulation = {
          val brokenBlockNum = multiBreakList.map(_.size).sum
          val coolDownTicks =
            (selectedSkill
              .maxCoolDownTicks
              .getOrElse(0)
              .toDouble * brokenBlockNum / totalBreakRangeVolume).ceil.toInt

          val reference = SeichiAssist.instance.activeSkillAvailability(player)

          if (coolDownTicks != 0) {
            for {
              _ <- reference.set(false).coerceTo[IO]
              _ <- IO
                .timer(PluginExecutionContexts.sleepAndRoutineContext)
                .sleep(coolDownTicks.ticks)
              _ <- reference.set(true).coerceTo[IO]
              _ <- FocusedSoundEffect(Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 0.1f).run(player)
            } yield ()
          } else {
            IO.unit
          }
        }

        // ツールの耐久値を減らす
        val adjustManaAndDurability = IO {
          if (!tool.getItemMeta.isUnbreakable) {
            val meta = tool.getItemMeta
            meta.asInstanceOf[Damageable].setDamage(toolDamageToSet)
            tool.setItemMeta(meta)
          }
        }

        effectEnvironment.unsafeRunEffectAsync(
          "複数破壊エフェクトを実行する",
          effectPrograms.sequence[IO, Fiber[IO, Unit]]
        )
        effectEnvironment.unsafeRunEffectAsync(
          "複数破壊エフェクトの後処理を実行する",
          adjustManaAndDurability >> availabilityFlagManipulation
        )
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def onPlayerBreakBlockFinally(event: BlockBreakEvent): Unit = {
    val player = event.getPlayer
    val block = event.getBlock
    val amount = SeichiExpAmount.ofNonNegative {
      BreakUtil
        .blockCountWeight[IO](event.getPlayer.getWorld)
        .map(multiplier => BreakUtil.totalBreakCount(Seq(block.getType)) * multiplier)
        .unsafeRunSync()
    }
    effectEnvironment.unsafeRunEffectAsync(
      "通常破壊されたブロックを整地量に計上する",
      SeichiAssist.instance.breakCountSystem.api.incrementSeichiExp.of(player, amount).toIO
    )

    val program = for {
      currentAutoMineStackState <- mineStackAPI.autoMineStack(player)
      isSucceedTryIntoMineStack <- whenAOrElse(currentAutoMineStackState)(
        event
          .getBlock
          .getDrops(player.getInventory.getItemInMainHand)
          .asScala
          .toList
          .traverse { itemStack =>
            mineStackAPI
              .mineStackRepository
              .tryIntoMineStack(player, itemStack, itemStack.getAmount)
          }
          .map(_.forall(_ == true)),
        false
      )
    } yield {
      if (isSucceedTryIntoMineStack) event.setDropItems(false)
      else ()
    }
    program.unsafeRunSync()

  }

  /**
   * y-59ハーフブロック破壊抑制
   *
   * @param event
   *   BlockBreakEvent
   */
  @EventHandler(priority = EventPriority.LOWEST)
  @SuppressWarnings(Array("deprecation"))
  def onPlayerBlockHalf(event: BlockBreakEvent): Unit = {
    val player = event.getPlayer
    val block = event.getBlock
    val world = player.getWorld
    // そもそも自分の保護じゃなきゃ処理かけない
    if (!WorldGuardWrapper.canBuild(player, block.getLocation)) return
    block.getBlockData match {
      case slab: Slab if slab.getType == Slab.Type.DOUBLE =>
        val location = block.getLocation
        world.dropItemNaturally(location, new ItemStack(block.getType))
      case _: Slab =>
      case _       => return
    }
    if (block.getY > -59) return
    if (block.getBlockData.asInstanceOf[Slab].getType != Slab.Type.BOTTOM) return
    if (!world.isSeichi) return
    event.setCancelled(true)
    player.sendMessage(s"${RED}Y-59以下に敷かれたハーフブロックは破壊不可能です。")
  }
}
