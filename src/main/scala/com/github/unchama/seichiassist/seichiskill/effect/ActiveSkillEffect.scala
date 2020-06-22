package com.github.unchama.seichiassist.seichiskill.effect

import cats.effect.{IO, Timer}
import com.github.unchama.seichiassist.MaterialSets.{BlockBreakableBySkill, BreakTool}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.data.{AxisAlignedCuboid, XYZTuple}
import com.github.unchama.seichiassist.seichiskill.SeichiSkill.{DualBreak, TrialBreak}
import com.github.unchama.seichiassist.seichiskill.effect.ActiveSkillNormalEffect.{Blizzard, Explosion, Meteor}
import com.github.unchama.seichiassist.seichiskill.effect.arrow.ArrowEffects
import com.github.unchama.seichiassist.seichiskill.{ActiveSkill, ActiveSkillRange}
import com.github.unchama.seichiassist.util.BreakUtil
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.util.effect.BukkitResources
import enumeratum.{Enum, EnumEntry}
import org.bukkit.ChatColor._
import org.bukkit._
import org.bukkit.entity.{Chicken, Player}
import org.bukkit.material.Wool

import scala.util.Random

sealed trait ActiveSkillEffect {
  val nameOnUI: String

  val arrowEffect: TargetedEffect[Player]

  def runBreakEffect(player: Player,
                     usedSkill: ActiveSkill,
                     tool: BreakTool,
                     breakBlocks: Set[BlockBreakableBySkill],
                     breakArea: AxisAlignedCuboid,
                     standard: Location): IO[Unit]
}

object ActiveSkillEffect {
  object NoEffect extends ActiveSkillEffect {
    override val nameOnUI: String = "未設定"

    override val arrowEffect: TargetedEffect[Player] = ArrowEffects.normalArrowEffect

    override def runBreakEffect(player: Player,
                                usedSkill: ActiveSkill,
                                tool: BreakTool,
                                breakBlocks: Set[BlockBreakableBySkill],
                                breakArea: AxisAlignedCuboid,
                                standard: Location): IO[Unit] =
      BreakUtil.massBreakBlock(player, breakBlocks, player.getLocation, tool, shouldPlayBreakSound = false, Material.AIR)
  }
}

// TODO usePointはInt型で抽象されるべきではない。実際、Normal/PremiumEffectの間で型が異なる。
sealed trait UnlockableActiveSkillEffect extends ActiveSkillEffect with EnumEntry {
  val materialOnUI: Material
  val explanation: String
  val usePoint: Int
}

object UnlockableActiveSkillEffect extends Enum[UnlockableActiveSkillEffect] {
  override def values: IndexedSeq[UnlockableActiveSkillEffect] =
    ActiveSkillNormalEffect.values ++ ActiveSkillPremiumEffect.values
}

sealed abstract class ActiveSkillNormalEffect(stringId: String,
                                              override val nameOnUI: String,
                                              override val explanation: String,
                                              override val usePoint: Int,
                                              override val materialOnUI: Material)
  extends UnlockableActiveSkillEffect {

  override val entryName: String = stringId

  override def runBreakEffect(player: Player,
                              usedSkill: ActiveSkill,
                              tool: BreakTool,
                              breakBlocks: Set[BlockBreakableBySkill],
                              breakArea: AxisAlignedCuboid,
                              standard: Location): IO[Unit] = {
    import PluginExecutionContexts.{asyncShift, cachedThreadPool, syncShift}
    import com.github.unchama.concurrent.syntax._
    import com.github.unchama.seichiassist.data.syntax._

    implicit val timer: Timer[IO] = IO.timer(cachedThreadPool)

    val isSkillDualBreakOrTrialBreak = Seq(DualBreak, TrialBreak).contains(usedSkill)

    this match {
      case Explosion =>
        val blockPositions = breakBlocks.map(_.getLocation).map(XYZTuple.of)
        val world = player.getWorld

        for {
          _ <- asyncShift.shift

          explosionLocations <- IO {
            breakArea
              .gridPoints(2)
              .map(XYZTuple.of(standard) + _)
              .filter(PositionSearching.containsOneOfPositionsAround(_, 1, blockPositions))
          }
          _ <- BreakUtil.massBreakBlock(player, breakBlocks, standard, tool, isSkillDualBreakOrTrialBreak)
          _ <- IO {
            explosionLocations.foreach(coordinates =>
              world.createExplosion(coordinates.toLocation(world), 0f, false)
            )
          }
        } yield ()

      case Blizzard =>
        for {
          _ <-
            BreakUtil.massBreakBlock(
              player, breakBlocks, standard, tool,
              shouldPlayBreakSound = false, Material.PACKED_ICE
            )
          _ <- IO.sleep(10.ticks)
          _ <- syncShift.shift
          _ <- IO {
            breakBlocks
              .map(_.getLocation)
              .foreach(location =>
                player.getWorld.spawnParticle(Particle.SNOWBALL, location, 1))

            val setEffectRadius = usedSkill.range match {
              case ActiveSkillRange.MultiArea(_, areaCount) => areaCount == 1
              case ActiveSkillRange.RemoteArea(_) => false
            }

            breakBlocks.foreach { b =>
              b.setType(Material.AIR)

              if (setEffectRadius)
                b.getWorld.playEffect(b.getLocation, Effect.STEP_SOUND, Material.PACKED_ICE, 5)
              else
                b.getWorld.playEffect(b.getLocation, Effect.STEP_SOUND, Material.PACKED_ICE)
            }
          }
        } yield ()

      case Meteor =>
        val delay = if (isSkillDualBreakOrTrialBreak) 1L else 10L

        import com.github.unchama.seichiassist.data.syntax._

        val blockPositions = breakBlocks.map(_.getLocation).map(XYZTuple.of)
        val world = player.getWorld

        for {
          _ <- IO.sleep(delay.ticks)
          _ <- syncShift.shift
          _ <- IO {
            breakArea.gridPoints(2).foreach { xyzTuple =>
              val effectCoordinate = XYZTuple.of(standard) + xyzTuple
              val effectLocation = effectCoordinate.toLocation(world)

              if (PositionSearching.containsOneOfPositionsAround(effectCoordinate, 1, blockPositions)) {
                world.spawnParticle(Particle.EXPLOSION_HUGE, effectLocation, 1)
              }
            }
          }
          // [0.8, 1.2)
          vol <- IO { new Random().nextFloat() * 0.4f + 0.8f }
          _ <- FocusedSoundEffect(Sound.ENTITY_WITHER_BREAK_BLOCK, 1.0f, vol).run(player)
          _ <- BreakUtil.massBreakBlock(player, breakBlocks, standard, tool, isSkillDualBreakOrTrialBreak)
        } yield ()
    }
  }

  /**
   * エフェクト選択時の遠距離エフェクト
   */
  lazy val arrowEffect: TargetedEffect[Player] =
    this match {
      case Explosion => ArrowEffects.singleArrowExplosionEffect
      case Blizzard => ArrowEffects.singleArrowBlizzardEffect
      case Meteor => ArrowEffects.singleArrowMeteoEffect
    }
}

object ActiveSkillNormalEffect extends Enum[ActiveSkillNormalEffect] {

  val values: IndexedSeq[ActiveSkillNormalEffect] = findValues

  case object Explosion extends ActiveSkillNormalEffect(
    "ef_explosion",
    s"${RED}エクスプロージョン", "単純な爆発", 50,
    Material.TNT
  )

  case object Blizzard extends ActiveSkillNormalEffect(
    "ef_blizzard",
    s"${AQUA}ブリザード", "凍らせる", 70,
    Material.PACKED_ICE
  )

  case object Meteor extends ActiveSkillNormalEffect(
    "ef_meteor",
    s"${DARK_RED}メテオ", "隕石を落とす", 100,
    Material.FIREBALL
  )

}

sealed abstract class ActiveSkillPremiumEffect(stringId: String,
                                               override val nameOnUI: String,
                                               override val explanation: String,
                                               override val usePoint: Int,
                                               override val materialOnUI: Material)
  extends UnlockableActiveSkillEffect {

  override val entryName: String = stringId

  def runBreakEffect(player: Player,
                     usedSkill: ActiveSkill,
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

  case object MAGIC extends ActiveSkillPremiumEffect(
    "ef_magic",
    s"$RED$UNDERLINE${BOLD}マジック", "鶏が出る手品", 10,
    Material.RED_ROSE
  )

}
