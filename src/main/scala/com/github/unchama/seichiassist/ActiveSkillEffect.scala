package com.github.unchama.seichiassist

import com.github.unchama.seichiassist.ActiveSkillEffect.{Blizzard, Explosion, Meteo}
import com.github.unchama.seichiassist.data.{ActiveSkillData, Coordinate}
import com.github.unchama.seichiassist.effect.arrow.ArrowEffects
import com.github.unchama.seichiassist.effect.breaking.{BlizzardTask, ExplosionTask, MeteoTask}
import enumeratum._
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import org.bukkit.{Location, Material}

sealed abstract class ActiveSkillEffect(val num: Int,
                                        val nameOnDatabase: String,
                                        val nameOnUI: String,
                                        val explanation: String,
                                        val usePoint: Int,
                                        val material: Material) extends EnumEntry {

  import com.github.unchama.seichiassist.effect.XYZTuple._

  def runBreakEffect(player: Player,
                     skillData: ActiveSkillData,
                     tool: ItemStack,
                     breakList: Set[Block],
                     start: Coordinate,
                     end: Coordinate,
                     standard: Location): BukkitTask = {
    val plugin = SeichiAssist.instance
    val skillId = skillData.skillnum

    this match {
      case Explosion => new ExplosionTask(player, skillId <= 2, tool, breakList, start.toXYZTuple(), end.toXYZTuple(), standard).runTask(plugin)
      case Blizzard =>
        val effect = new BlizzardTask(player, skillData, tool, breakList, start, end, standard)

        if (skillId < 3) {
          effect.runTaskLater(plugin, 1)
        } else {
          val period = if (SeichiAssist.DEBUG) 100L else 10L
          effect.runTaskTimer(plugin, 0, period)
        }
      case Meteo =>
        val delay = if (skillId < 3) 1L else 10L

        new MeteoTask(player, skillData, tool, breakList, start, end, standard).runTaskLater(plugin, delay)
    }
  }

  //エフェクトの実行処理分岐
  def runArrowEffect(player: Player): Unit = {
    val effect = this match {
      case Explosion => ArrowEffects.singleArrowExplosionEffect
      case Blizzard => ArrowEffects.singleArrowBlizzardEffect
      case Meteo => ArrowEffects.singleArrowMeteoEffect
    }

    effect(player).unsafeRunAsync {
      case Left(error) =>
        println("Caught exception while running arrow-effect asynchronously")
        error.printStackTrace()
      case Right(_) =>
    }
  }
}

object ActiveSkillEffect extends Enum[ActiveSkillEffect] {

  val values: IndexedSeq[ActiveSkillEffect] = findValues
  /**
   * @deprecated for interop purpose only
   */
  @Deprecated() val arrayValues: Array[ActiveSkillEffect] = values.toArray

  def getNameByNum(effectNum: Int): String = ActiveSkillEffect.values
    .filter(_.isInstanceOf[ActiveSkillEffect])
    .map[ActiveSkillEffect](_.asInstanceOf)
    .find(activeSkillEffect => activeSkillEffect.num == effectNum)
    .map(_.nameOnUI)
    .getOrElse("未設定")

  def fromSqlName(sqlName: String): Option[ActiveSkillEffect] = ActiveSkillEffect.values.find(_.nameOnDatabase == sqlName)

  case object Explosion extends ActiveSkillEffect(1, s"ef_explosion", "${RED}エクスプロージョン", "単純な爆発", 50, Material.TNT)

  case object Blizzard extends ActiveSkillEffect(2, s"ef_blizzard", "${AQUA}ブリザード", "凍らせる", 70, Material.PACKED_ICE)

  case object Meteo extends ActiveSkillEffect(3, s"ef_meteo", "${DARK_RED}メテオ", "隕石を落とす", 100, Material.FIREBALL)
}
