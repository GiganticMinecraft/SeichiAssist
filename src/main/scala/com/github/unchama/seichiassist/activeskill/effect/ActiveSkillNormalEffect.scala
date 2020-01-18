package com.github.unchama.seichiassist.activeskill.effect

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.activeskill.effect.ActiveSkillNormalEffect.{Blizzard, Explosion, Meteo}
import com.github.unchama.seichiassist.activeskill.effect.arrow.ArrowEffects
import com.github.unchama.seichiassist.activeskill.effect.breaking.{BlizzardTask, ExplosionTask, MeteoTask}
import com.github.unchama.seichiassist.data.{ActiveSkillData, AxisAlignedCuboid}
import com.github.unchama.targetedeffect.TargetedEffect
import enumeratum._
import org.bukkit.ChatColor._
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Location, Material}

sealed abstract class ActiveSkillNormalEffect(val num: Int,
                                              val nameOnDatabase: String,
                                              val nameOnUI: String,
                                              val explanation: String,
                                              val usePoint: Int,
                                              val material: Material) extends EnumEntry with ActiveSkillEffect {

  override def runBreakEffect(player: Player,
                              skillData: ActiveSkillData,
                              tool: ItemStack,
                              breakBlocks: Set[Block],
                              breakArea: AxisAlignedCuboid,
                              standard: Location): Unit = {
    val plugin = SeichiAssist.instance
    val skillId = skillData.skillnum

    this match {
      case Explosion => new ExplosionTask(player, skillId <= 2, tool, breakBlocks, breakArea, standard).runTask(plugin)
      case Blizzard =>
        val effect = new BlizzardTask(player, skillData, tool, breakBlocks, standard)

        if (skillId < 3) {
          effect.runTaskLater(plugin, 1)
        } else {
          val period = if (SeichiAssist.DEBUG) 100L else 10L
          effect.runTaskTimer(plugin, 0, period)
        }
      case Meteo =>
        val delay = if (skillId < 3) 1L else 10L

        new MeteoTask(player, skillData, tool, breakBlocks, breakArea, standard).runTaskLater(plugin, delay)
    }
  }

  //エフェクトの実行処理分岐
  def arrowEffect(player: Player): TargetedEffect[Player] =
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
