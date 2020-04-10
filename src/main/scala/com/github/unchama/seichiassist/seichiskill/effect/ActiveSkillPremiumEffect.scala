package com.github.unchama.seichiassist.seichiskill.effect

import cats.effect.{IO, Timer}
import com.github.unchama.seichiassist.MaterialSets.{BlockBreakableBySkill, BreakTool}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.data.{ActiveSkillData_Legacy, AxisAlignedCuboid}
import com.github.unchama.seichiassist.seichiskill.effect.arrow.ArrowEffects
import com.github.unchama.seichiassist.util.BreakUtil
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.util.effect.BukkitResources
import enumeratum._
import org.bukkit.ChatColor._
import org.bukkit._
import org.bukkit.entity.{Chicken, Player}
import org.bukkit.material.Wool

import scala.util.Random

sealed abstract class ActiveSkillPremiumEffect(val num: Int,
                                               val nameOnDatabase: String,
                                               val nameOnUI: String,
                                               val explanation: String,
                                               val usePoint: Int,
                                               val material: Material) extends EnumEntry with ActiveSkillEffect {
  @Deprecated
  def getsqlName: String = this.nameOnDatabase

  def runBreakEffect(player: Player,
                     skillData: ActiveSkillData_Legacy,
                     tool: BreakTool,
                     breakBlocks: Set[BlockBreakableBySkill],
                     breakArea: AxisAlignedCuboid,
                     standard: Location): IO[Unit] = {
    import PluginExecutionContexts.{cachedThreadPool, syncShift}
    import com.github.unchama.concurrent.syntax._
    import com.github.unchama.seichiassist.data.syntax._

    implicit val timer: Timer[IO] = IO.timer(cachedThreadPool)

    this match {
      case ActiveSkillPremiumEffect.MAGIC =>
        val colors = Array(DyeColor.RED, DyeColor.BLUE, DyeColor.YELLOW, DyeColor.GREEN)

        //破壊するブロックの中心位置
        val centerBreak: Location = standard + ((breakArea.begin + breakArea.end) / 2)

        for {
          randomColor <- IO { colors(Random.nextInt(colors.length)) }
          _ <- BreakUtil.massBreakBlock(player, breakBlocks, standard, tool, shouldPlayBreakSound = false, Material.WOOL)
          _ <- IO {
            breakBlocks.foreach { b =>
              val state = b.getState
              state
                .getData.asInstanceOf[Wool]
                .setColor(randomColor)
              state.update()
            }
          }

          period <- IO { if (SeichiAssist.DEBUG) 100 else 10 }
          _ <- IO.sleep(period.ticks)

          _ <- syncShift.shift

          _ <- SeichiAssist.instance.magicEffectEntityScope
            .useTrackedForSome(BukkitResources.vanishingEntityResource(centerBreak, classOf[Chicken])) { e =>
              for {
                _ <- IO {
                  e.playEffect(EntityEffect.WITCH_MAGIC)
                  e.setInvulnerable(true)
                }
                _ <- IO.sleep(100.ticks)
                _ <- FocusedSoundEffect(Sound.ENTITY_WITCH_AMBIENT, 1f, 1.5f).run(player)
              } yield ()
            }
            .start(syncShift)

          _ <- IO {
            breakBlocks.foreach { b =>
              b.getWorld.spawnParticle(Particle.NOTE, b.getLocation.add(0.5, 0.5, 0.5), 1)
            }
          }
        } yield ()
    }
  }

  /**
   * エフェクト選択時の遠距離エフェクト
   */
  lazy val arrowEffect: TargetedEffect[Player] =
    this match {
      case ActiveSkillPremiumEffect.MAGIC => ArrowEffects.singleArrowMagicEffect
    }
}

case object ActiveSkillPremiumEffect extends Enum[ActiveSkillPremiumEffect] {

  val values: IndexedSeq[ActiveSkillPremiumEffect] = findValues
  /**
   * @deprecated for interop purpose only
   */
  @Deprecated()
  val arrayValues: Array[ActiveSkillPremiumEffect] = values.toArray

  def fromSqlName(sqlName: String): Option[ActiveSkillPremiumEffect] = values.find(sqlName == _.nameOnDatabase)

  case object MAGIC extends ActiveSkillPremiumEffect(1, "ef_magic", s"$RED$UNDERLINE${BOLD}マジック", "鶏が出る手品", 10, Material.RED_ROSE)
}
