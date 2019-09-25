package com.github.unchama.seichiassist

import com.github.unchama.seichiassist.ActiveSkillEffect.{Blizzard, Explosion, Meteo}
import com.github.unchama.seichiassist.data.{ActiveSkillData, Coordinate}
import com.github.unchama.seichiassist.effect.arrow.ArrowEffects
import enumeratum._
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Location, Material}

sealed abstract case class ActiveSkillEffect(num: Int,
                                             nameOnDatabase: String,
                                             nameOnUI: String,
                                             explanation: String,
                                             usePoint: Int,
                                             material: Material) extends EnumEntry {
  def runBreakEffect(player: Player,
                     skillData: ActiveSkillData,
                     tool: ItemStack,
                     breakList: Set[Block],
                     start: Coordinate,
                     end: Coordinate,
                     standard: Location) = {
    case Explosion => ExplosionTask(player, skillId <= 2, tool, breaklist, start.toXYZTuple(), end.toXYZTuple(), standard).runTask(plugin)
    case Blizzard => {
      val effect = BlizzardTask(player, skillData, tool, breaklist, start, end, standard)

      if (skillId < 3) {
        effect.runTaskLater(plugin, 1)
      } else {
        val period = if (SeichiAssist.DEBUG) 100L else 10L
        effect.runTaskTimer(plugin, 0, period)
      }
    }
    case Meteo => {
      val delay = if (skillId < 3) 1L else 10L

      MeteoTask(player, skillData, tool, breaklist, start, end, standard)
        .runTaskLater(plugin, delay)
    }
  }

  //エフェクトの実行処理分岐
  def runArrowEffect(player: Player) {
    val effect = when(this
    @ActiveSkillEffect
    )
    {
      EXPLOSION =>
        ArrowEffects.singleArrowExplosionEffect
        BLIZZARD =>
          ArrowEffects.singleArrowBlizzardEffect
          METEO => ArrowEffects.singleArrowMeteoEffect
    }

    GlobalScope.launch(Schedulers.async) {
      effect.runFor(player)
    }
  }
}

object ActiveSkillEffect extends Enum[ActiveSkillEffect] {
  case object Explosion extends ActiveSkillEffect(1, s"ef_explosion", "${RED}エクスプロージョン", "単純な爆発", 50, Material.TNT)
  case object Blizzard extends ActiveSkillEffect(2, s"ef_blizzard", "${AQUA}ブリザード", "凍らせる", 70, Material.PACKED_ICE)
  case object Meteo extends ActiveSkillEffect(3, s"ef_meteo", "${DARK_RED}メテオ", "隕石を落とす", 100, Material.FIREBALL)

  val values: IndexedSeq[ActiveSkillEffect] = findValues

  @Deprecated("for interop purpose only")
  val arrayValues: Array[ActiveSkillEffect] = values.toArray

  def getNameByNum(effectNum: Int): String = ActiveSkillEffect.values
    .filter(_.isInstanceOf[ActiveSkillEffect])
    .map[ActiveSkillEffect](_.asInstanceOf)
    .find(activeSkillEffect => activeSkillEffect.num == effectNum)
    .map(_.nameOnUI)
    .orElseGet("未設定")

  def fromSqlName(sqlName: String): Option[ActiveSkillEffect] = ActiveSkillEffect.values
}
