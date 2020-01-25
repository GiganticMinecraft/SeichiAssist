package com.github.unchama.seichiassist.activeskill.effect.breaking

import cats.effect.IO
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.AxisAlignedCuboid
import com.github.unchama.seichiassist.util.BreakUtil
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.util.effect.BukkitResources
import org.bukkit._
import org.bukkit.block.Block
import org.bukkit.entity.{Chicken, Player}
import org.bukkit.inventory.ItemStack
import org.bukkit.material.Wool

import scala.concurrent.ExecutionContext
import scala.util.Random

class MagicTask(private val player: Player,
                private val tool: ItemStack, // 使用するツール
                private val blocks: Set[Block], // 破壊するブロックリスト
                breakArea: AxisAlignedCuboid,
                _skillCenter: Location) extends RoundedTask() {
  import com.github.unchama.seichiassist.data.syntax._

  // スキルが発動される中心位置
  private val itemDropLoc: Location = _skillCenter.clone()

  //破壊するブロックの中心位置
  private val centerBreak: Location = this.itemDropLoc + ((breakArea.begin + breakArea.end) / 2)

  override def firstAction(): Unit = {
    //1回目のrun
    val colors = Array(DyeColor.RED, DyeColor.BLUE, DyeColor.YELLOW, DyeColor.GREEN)
    val randomColor = colors(Random.nextInt(colors.length))

    com.github.unchama.seichiassist.unsafe.runIOAsync(
      "マジックエフェクトの一回目を再生する",
      for {
        _ <- BreakUtil.massBreakBlock(player, blocks, itemDropLoc, tool, shouldPlayBreakSound = false, Material.WOOL)
        _ <- IO {
          blocks.foreach { b =>
            val state = b.getState
            state
              .getData.asInstanceOf[Wool]
              .setColor(randomColor)
            state.update()
          }
        }
      } yield ()
    )
  }

  override def secondAction(): Unit = {
    //2回目のrun

    import cats.implicits._
    import com.github.unchama.concurrent.syntax._

    val sleepUntilChickenDisappears = IO.sleep(100.ticks)(IO.timer(ExecutionContext.global))

    val chickenEffect = SeichiAssist.instance.magicEffectEntityScope
      .trackedForSome(BukkitResources.vanishingEntityResource(centerBreak, classOf[Chicken]))
      .use {
        case Some(e) => IO {
          e.playEffect(EntityEffect.WITCH_MAGIC)
          e.setInvulnerable(true)
        } *> sleepUntilChickenDisappears
        case None => IO.unit
      }

    val soundEffect = FocusedSoundEffect(Sound.ENTITY_WITCH_AMBIENT, 1f, 1.5f).run(player)

    (soundEffect *> chickenEffect).unsafeRunSync()

    blocks.foreach { b =>
      b.setType(Material.AIR)
      b.getWorld.spawnParticle(Particle.NOTE, b.getLocation.add(0.5, 0.5, 0.5), 1)
      SeichiAssist.managedBlocks -= b
    }

    cancel()
  }
}
