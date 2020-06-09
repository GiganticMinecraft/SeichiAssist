package com.github.unchama.seichiassist.seichiskill.assault

import cats.effect.{ExitCase, IO}
import com.github.unchama.concurrent.{BukkitSyncIOShift, RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.seichiassist.MaterialSets.BreakTool
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.Mana
import com.github.unchama.seichiassist.seichiskill.SeichiSkillUsageMode.Disabled
import com.github.unchama.seichiassist.seichiskill.{AssaultSkill, AssaultSkillRange, BlockSearching, BreakArea}
import com.github.unchama.seichiassist.util.{BreakUtil, Util}
import org.bukkit.ChatColor._
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.{GameMode, Location, Material}

object AssaultRoutine {
  private case class IterationState(previousLocation: Location, idleIterationCount: Int)

  private def locationsFarEnough(l1: Location, l2: Location): Boolean = {
    val projections: Seq[Location => Int] = Seq(_.getBlockX, _.getBlockY, _.getBlockZ)
    projections.exists(p => (p(l1) - p(l2)).abs >= 10)
  }

  def apply(player: Player, toolToBeUsed: BreakTool, skill: AssaultSkill)
           (implicit syncShift: BukkitSyncIOShift, ctx: RepeatingTaskContext): IO[Unit] = {
    val idleCountLimit = 20

    val playerData = SeichiAssist.playermap(player.getUniqueId)

    def routineAction(state: IterationState): Option[IterationState] = {
      val skillState = playerData.skillState.get.unsafeRunSync()
      val assaultSkill = skillState.assaultSkill.getOrElse(return None)

      if (skillState.usageMode == Disabled) return None

      if (player.getGameMode != GameMode.SURVIVAL) {
        player.sendMessage(s"${GREEN}ゲームモードをサバイバルに変更してください。")
        return None
      }

      if (!Util.seichiSkillsAllowedIn(player.getWorld)) {
        player.sendMessage(s"${GREEN}スキルは整地ワールドでのみ使用可能です。")
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

      //プレイヤーの足のy座標
      val playerLocY = player.getLocation.getBlockY - 1

      val block = player.getLocation.getBlock

      //最初に登録したツールと今のツールが違う場合
      if (toolToBeUsed != player.getInventory.getItemInOffHand) return None

      val skillArea = BreakArea(assaultSkill, skillState.usageMode)
      val breakArea = skillArea.makeBreakArea(player).unsafeRunSync().head

      val breakLength = skillArea.breakLength
      val areaTotalBlockCount = breakLength.x * breakLength.y * breakLength.z

      val (shouldBreakAllBlocks, shouldRemoveOrCondenseWater, shouldRemoveOrCondenseLava) =
        assaultSkill.range match {
          case AssaultSkillRange.Armor(_) => (true, true, true)
          case AssaultSkillRange.Water(_) => (false, true, false)
          case AssaultSkillRange.Lava(_) => (false, false, true)
          case AssaultSkillRange.Liquid(_) => (false, true, true)
        }

      //重力値計算
      val gravity = BreakUtil.getGravity(player, block, isAssault = true)

      //重力値の判定
      if (gravity > 15) {
        player.sendMessage(s"${RED}スキルを使用するには上から掘ってください。")
        return None
      }

      import com.github.unchama.seichiassist.data.syntax._
      val BlockSearching.Result(foundBlocks, foundWaters, foundLavas) =
        BlockSearching.searchForBlocksBreakableWithSkill(player, breakArea.gridPoints(), block)
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
      val manaUsage = breakTargets.toDouble * (gravity + 1) * assaultSkill.manaCost / areaTotalBlockCount

      //減る耐久値の計算
      val durability =
        (toolToBeUsed.getDurability +
          BreakUtil.calcDurability(toolToBeUsed.getEnchantmentLevel(Enchantment.DURABILITY), breakTargets)).toShort

      val playerMana: Mana = playerData.manaState

      //実際に経験値を減らせるか判定
      if (!playerMana.has(manaUsage)) return None

      // 実際に耐久値を減らせるか判定
      if (toolToBeUsed.getType.getMaxDurability <= durability && !toolToBeUsed.getItemMeta.isUnbreakable) return None

      // 経験値を減らす
      playerMana.decrease(manaUsage, player, playerData.level)

      // 耐久値を減らす
      if (!toolToBeUsed.getItemMeta.isUnbreakable) toolToBeUsed.setDurability(durability)

      // ブロックを書き換える
      if (shouldBreakAllBlocks) {
        (foundWaters ++ foundLavas).foreach(_.setType(Material.AIR))
        com.github.unchama.seichiassist.unsafe.runIOAsync(
          "ブロックを大量破壊する",
          BreakUtil.massBreakBlock(player, foundBlocks, player.getLocation, toolToBeUsed, shouldPlayBreakSound = false)
        )
      } else {
        if (shouldRemoveOrCondenseWater) foundWaters.foreach(_.setType(Material.PACKED_ICE))
        if (shouldRemoveOrCondenseLava) foundLavas.foreach(_.setType(Material.MAGMA))
      }

      Some(newState)
    }

    import cats.implicits._

    import scala.concurrent.duration._
    for {
      _ <- IO { player.sendMessage(s"${GOLD}アサルトスキル：${skill.name} ON") }
      currentLoc <- IO { player.getLocation }
      _ <- RepeatingRoutine.recMTask(IterationState(currentLoc, 0))(s =>
        syncShift.shift >> IO(routineAction(s))
      )(IO.pure(500.millis)).guaranteeCase {
        case ExitCase.Error(_) | ExitCase.Completed =>
          IO {
            // 継続条件が満たされなかった場合の表示
            player.sendMessage(s"${YELLOW}アサルトスキルがOFFになりました")
          }
        case ExitCase.Canceled =>
          IO {
            // 明示的にプレーヤーが切り替えた場合
            player.sendMessage(s"${GOLD}アサルトスキル：${skill.name} OFF")
          }
      }
    } yield ()
  }
}
