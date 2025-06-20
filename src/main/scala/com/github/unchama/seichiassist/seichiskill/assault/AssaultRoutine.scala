package com.github.unchama.seichiassist.seichiskill.assault

import cats.effect.{ExitCase, IO, SyncIO, Timer}
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.ManagedWorld._
import com.github.unchama.seichiassist.MaterialSets.BreakTool
import com.github.unchama.seichiassist.seichiskill.{
  AssaultSkill,
  AssaultSkillRange,
  BlockSearching,
  BreakArea
}
import com.github.unchama.seichiassist.subsystems.mana.ManaWriteApi
import com.github.unchama.seichiassist.subsystems.mana.domain.ManaAmount
import com.github.unchama.seichiassist.util.BreakUtil
import com.github.unchama.seichiassist.{DefaultEffectEnvironment, MaterialSets, SeichiAssist}
import org.bukkit.ChatColor._
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.Damageable
import org.bukkit.{GameMode, Location, Material, Sound}

object AssaultRoutine {

  private case class IterationState(previousLocation: Location, idleIterationCount: Int)

  private def locationsFarEnough(l1: Location, l2: Location): Boolean = {
    val projections: Seq[Location => Int] = Seq(_.getBlockX, _.getBlockY, _.getBlockZ)
    projections.exists(p => (p(l1) - p(l2)).abs >= 10)
  }

  def tryStart(player: Player, skill: AssaultSkill)(
    implicit ioOnMainThread: OnMinecraftServerThread[IO],
    ctx: RepeatingTaskContext,
    manaApi: ManaWriteApi[SyncIO, Player]
  ): IO[Unit] = {
    for {
      offHandTool <- IO {
        player.getInventory.getItemInOffHand
      }
      refinedTool = MaterialSets.refineItemStack(offHandTool, MaterialSets.breakToolMaterials)
      _ <- refinedTool match {
        case Some(tool) => AssaultRoutine(player, tool, skill)
        case None       =>
          IO {
            player.sendMessage(s"${GREEN}使うツールをオフハンドにセット(fキー)してください")
          }
      }
    } yield ()
  }

  def apply(player: Player, toolToBeUsed: BreakTool, skill: AssaultSkill)(
    implicit ioOnMainThread: OnMinecraftServerThread[IO],
    ctx: RepeatingTaskContext,
    manaApi: ManaWriteApi[SyncIO, Player]
  ): IO[Unit] = {
    val idleCountLimit = 20

    val playerData = SeichiAssist.playermap(player.getUniqueId)

    def routineAction(state: IterationState): Option[IterationState] = {
      val skillState = playerData.skillState.get.unsafeRunSync()
      val assaultSkill = skillState.assaultSkill.getOrElse(return None)

      if (player.getGameMode != GameMode.SURVIVAL) {
        player.sendMessage(s"${GREEN}ゲームモードをサバイバルに変更してください。")
        return None
      }

      if (!player.getWorld.isSeichiSkillAllowed) {
        player.sendMessage(s"${GREEN}スキルは整地・建築ワールドでのみ使用可能です。")
        return None
      }

      // 放置判定
      // 一定以上動いてなかったら処理終了
      val newState = {
        val IterationState(prevLocation, idleCount) = state
        val currentLocation = player.getLocation

        if (locationsFarEnough(prevLocation, currentLocation)) {
          IterationState(currentLocation, 0)
        } else if (idleCount < idleCountLimit) {
          IterationState(prevLocation, idleCount + 1)
        } else {
          return None
        }
      }

      // プレイヤーの足のy座標
      val playerLocY = player.getLocation.getBlockY - 1

      val block = player.getLocation.getBlock

      // 最初に登録したツールと今のツールが違う場合
      if (toolToBeUsed != player.getInventory.getItemInOffHand) return None

      val skillArea = BreakArea(assaultSkill, skillState.usageMode)
      val breakArea = skillArea.makeBreakArea(player).unsafeRunSync().head

      val breakLength = skillArea.breakLength
      val areaTotalBlockCount = breakLength.x * breakLength.y * breakLength.z

      val (shouldBreakAllBlocks, shouldRemoveOrCondenseWater, shouldRemoveOrCondenseLava) =
        assaultSkill.range match {
          case AssaultSkillRange.Armor(_)  => (true, true, true)
          case AssaultSkillRange.Water(_)  => (false, true, false)
          case AssaultSkillRange.Lava(_)   => (false, false, true)
          case AssaultSkillRange.Liquid(_) => (false, true, true)
        }

      // 重力値計算
      val gravity = BreakUtil.getGravity(player, block, isAssault = true)

      // 重力値の判定
      if (gravity > 15) {
        player.sendMessage(s"${RED}スキルを使用するには上から掘ってください。")
        return None
      }

      import com.github.unchama.seichiassist.data.syntax._
      val BlockSearching.Result(foundBlocks, foundWaters, foundLavas) =
        BlockSearching
          .searchForBlocksBreakableWithSkill(player, breakArea.gridPoints(), block)
          .unsafeRunSync()
          .filterAll(targetBlock =>
            player.isSneaking || !shouldBreakAllBlocks ||
              targetBlock.getLocation.getBlockY > playerLocY || targetBlock == block
          )

      // 実際に破壊するブロック数の計算
      val breakTargets =
        (if (shouldRemoveOrCondenseWater) foundWaters.size else 0) +
          (if (shouldRemoveOrCondenseLava) foundLavas.size else 0) +
          (if (shouldBreakAllBlocks) foundBlocks.size else 0)

      // 減るマナ計算
      // 実際に破壊するブロック数 * 全てのブロックを破壊したときの消費経験値÷すべての破壊するブロック数 * 重力
      val manaUsage =
        breakTargets.toDouble * (gravity + 1) * assaultSkill.manaCost / areaTotalBlockCount

      // 減る耐久値の計算
      val durability =
        (toolToBeUsed.getItemMeta.asInstanceOf[Damageable].getDamage +
          BreakUtil.calcDurability(
            toolToBeUsed.getEnchantmentLevel(Enchantment.DURABILITY),
            breakTargets
          )).toShort

      // 実際に耐久値を減らせるか判定
      if (
        toolToBeUsed.getType.getMaxDurability <= durability && !toolToBeUsed
          .getItemMeta
          .isUnbreakable
      ) return None

      // マナを消費する
      manaApi.manaAmount(player).tryAcquire(ManaAmount(manaUsage)).unsafeRunSync() match {
        case Some(_) =>
        case None    => return None
      }

      // 耐久値を減らす
      if (!toolToBeUsed.getItemMeta.isUnbreakable) {
        val meta = toolToBeUsed.getItemMeta
        meta.asInstanceOf[Damageable].setDamage(durability)
        toolToBeUsed.setItemMeta(meta)
      }

      // ブロックを書き換える
      if (shouldBreakAllBlocks) {
        (foundWaters ++ foundLavas).foreach(_.setType(Material.AIR))
        DefaultEffectEnvironment.unsafeRunEffectAsync(
          "ブロックを大量破壊する",
          BreakUtil.massBreakBlock(
            player,
            foundBlocks,
            player.getLocation,
            toolToBeUsed,
            shouldPlayBreakSound = false
          )
        )
      } else {
        if (shouldRemoveOrCondenseWater) foundWaters.foreach(_.setType(Material.PACKED_ICE))
        if (shouldRemoveOrCondenseLava) foundLavas.foreach(_.setType(Material.MAGMA_BLOCK))
      }

      Some(newState)
    }

    implicit val timer: Timer[IO] = IO.timer(ctx)

    import scala.concurrent.duration._

    for {
      _ <- IO {
        player.sendMessage(s"${GREEN}アサルトスキル：${skill.name} ON")
      }
      currentLoc <- IO {
        player.getLocation
      }
      _ <- RepeatingRoutine
        .recMTask(IterationState(currentLoc, 0))(s =>
          ioOnMainThread.runAction(SyncIO(routineAction(s)))
        )(IO.pure(500.millis))
        .guaranteeCase {
          case ExitCase.Error(_) | ExitCase.Completed =>
            IO {
              // 継続条件が満たされなかった場合の表示
              player.sendMessage(s"${YELLOW}アサルトスキルがOFFになりました")
              player.playSound(currentLoc, Sound.BLOCK_LEVER_CLICK, 1f, 0.7f)
            }
          case ExitCase.Canceled =>
            IO {
              // 明示的にプレーヤーが切り替えた場合
              player.sendMessage(s"${GREEN}アサルトスキル：${skill.name} OFF")
            }
        }
    } yield ()
  }
}
