package com.github.unchama.seichiassist.activeskill.effect.breaking

import cats.effect.IO
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.XYZTuple
import com.github.unchama.seichiassist.task.AsyncEntityRemover
import com.github.unchama.seichiassist.util.BreakUtil
import org.bukkit._
import org.bukkit.block.Block
import org.bukkit.entity.{Chicken, EntityType, Player}
import org.bukkit.inventory.ItemStack
import org.bukkit.material.Wool

import scala.util.Random

class MagicTask(private val player: Player,
                private val tool: ItemStack, // 使用するツール
                private val blocks: Set[Block], // 破壊するブロックリスト
                start: XYZTuple, end: XYZTuple, _skillCenter: Location) extends RoundedTask() {
  // スキルが発動される中心位置
  private val skillCenter: Location = _skillCenter.clone()

  // 破壊するブロックの中心位置
  private val centerBreak: Location =
    this.skillCenter.add(relativeAverage(start.x, end.x), relativeAverage(start.y, end.y), relativeAverage(start.z, end.z))

  override def firstAction(): Unit = {
    //1回目のrun
    val colors = Array(DyeColor.RED, DyeColor.BLUE, DyeColor.YELLOW, DyeColor.GREEN)
    val randomColor = colors(Random.nextInt(colors.length))

    com.github.unchama.seichiassist.unsafe.runIOAsync(
      "マジックエフェクトの一回目を再生する",
      for {
        _ <- BreakUtil.massBreakBlock(player, blocks, skillCenter, tool, shouldPlayBreakSound = false, Material.WOOL)
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
    if (SeichiAssist.managedEntities.isEmpty) {
      val e = player.getWorld.spawnEntity(centerBreak, EntityType.CHICKEN).asInstanceOf[Chicken]
      SeichiAssist.managedEntities += e
      e.playEffect(EntityEffect.WITCH_MAGIC)
      e.setInvulnerable(true)
      new AsyncEntityRemover(e).runTaskLater(SeichiAssist.instance, 100)
      player.getWorld.playSound(player.getLocation, Sound.ENTITY_WITCH_AMBIENT, 1f, 1.5f)
    }

    blocks.foreach { b =>
      b.setType(Material.AIR)
      b.getWorld.spawnParticle(Particle.NOTE, b.getLocation.add(0.5, 0.5, 0.5), 1)
      SeichiAssist.managedBlocks -= b
    }
    cancel()
  }

  private def relativeAverage(i1: Int, i2: Int): Double = (i1 + (i2 - i1) / 2).toDouble
}
