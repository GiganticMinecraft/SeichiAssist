package com.github.unchama.seichiassist.activeskill.effect

import cats.effect.{IO, Timer}
import com.github.unchama.seichiassist.ActiveSkill
import com.github.unchama.seichiassist.activeskill.effect.ActiveSkillNormalEffect.{Blizzard, Explosion, Meteo}
import com.github.unchama.seichiassist.activeskill.effect.arrow.ArrowEffects
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.data.{ActiveSkillData_Legacy, AxisAlignedCuboid, XYZTuple}
import com.github.unchama.seichiassist.util.BreakUtil
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import enumeratum._
import org.bukkit.ChatColor._
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Effect, Location, Material, Particle, Sound}

import scala.util.Random

sealed abstract class ActiveSkillNormalEffect(val num: Int,
                                              val nameOnDatabase: String,
                                              val nameOnUI: String,
                                              val explanation: String,
                                              val usePoint: Int,
                                              val material: Material) extends EnumEntry with ActiveSkillEffect {

  override def runBreakEffect(player: Player,
                              skillData: ActiveSkillData_Legacy,
                              tool: ItemStack,
                              breakBlocks: Set[Block],
                              breakArea: AxisAlignedCuboid,
                              standard: Location): IO[Unit] = {
    import PluginExecutionContexts.{asyncShift, cachedThreadPool, syncShift}
    import com.github.unchama.concurrent.syntax._
    import com.github.unchama.seichiassist.data.syntax._

    implicit val timer: Timer[IO] = IO.timer(cachedThreadPool)

    val skillId = skillData.skillnum

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
          _ <- BreakUtil.massBreakBlock(player, breakBlocks, standard, tool, skillId <= 2)
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

            breakBlocks.foreach { b =>
              b.setType(Material.AIR)

              if (skillData.skilltype == ActiveSkill.BREAK.gettypenum())
                b.getWorld.playEffect(b.getLocation, Effect.STEP_SOUND, Material.PACKED_ICE, 5)
              else
                b.getWorld.playEffect(b.getLocation, Effect.STEP_SOUND, Material.PACKED_ICE)
            }
          }
        } yield ()

      case Meteo =>
        val delay = if (skillId < 3) 1L else 10L

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
          _ <- BreakUtil.massBreakBlock(player, breakBlocks, standard, tool, skillData.skillnum <= 2)
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
      case Meteo => ArrowEffects.singleArrowMeteoEffect
    }
}

object ActiveSkillNormalEffect extends Enum[ActiveSkillNormalEffect] {

  val values: IndexedSeq[ActiveSkillNormalEffect] = findValues
  /**
   * @deprecated for interop purpose only
   */
  @Deprecated() val arrayValues: Array[ActiveSkillNormalEffect] = values.toArray

  def getNameByNum(effectNum: Int): String = ActiveSkillNormalEffect.values
    .filter(_.isInstanceOf[ActiveSkillNormalEffect])
    .find(activeSkillEffect => activeSkillEffect.num == effectNum)
    .map(_.nameOnUI)
    .getOrElse("未設定")

  def fromSqlName(sqlName: String): Option[ActiveSkillNormalEffect] = ActiveSkillNormalEffect.values.find(_.nameOnDatabase == sqlName)

  case object Explosion extends ActiveSkillNormalEffect(1, s"ef_explosion", s"${RED}エクスプロージョン", "単純な爆発", 50, Material.TNT)

  case object Blizzard extends ActiveSkillNormalEffect(2, s"ef_blizzard", s"${AQUA}ブリザード", "凍らせる", 70, Material.PACKED_ICE)

  case object Meteo extends ActiveSkillNormalEffect(3, s"ef_meteo", s"${DARK_RED}メテオ", "隕石を落とす", 100, Material.FIREBALL)

}
