package com.github.unchama.seichiassist

import com.github.unchama.seichiassist.data.{ActiveSkillData, Coordinate}
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Location, Material}

object ActiveSkillEffect extends Enumeration {

  case class ActiveSkillEffectVal(num: Int,
                                  nameOnDatabase: String,
                                  nameOnUI: String,
                                  explanation: String,
                                  usePoint: Int,
                                  material: Material) extends Val

  val Explosion = ActiveSkillEffectVal(1, s"ef_explosion", "${RED}エクスプロージョン", "単純な爆発", 50, Material.TNT)
  val Blizzard = ActiveSkillEffectVal(2, s"ef_blizzard", "${AQUA}ブリザード", "凍らせる", 70, Material.PACKED_ICE)
  val Meteo = ActiveSkillEffectVal(3, s"ef_meteo", "${DARK_RED}メテオ", "隕石を落とす", 100, Material.FIREBALL)

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

  def getNameByNum(effectNum: Int): String = ActiveSkillEffect.values
    .filter(_.isInstanceOf[ActiveSkillEffectVal])
    .map[ActiveSkillEffectVal](_.asInstanceOf)
    .find(activeSkillEffect => activeSkillEffect.num == effectNum)
    .map(_.nameOnUI)
    .orElse("未設定")

  def fromSqlName(sqlName: String): Option[ActiveSkillEffectVal] = ActiveSkillEffect.values
}


object ActiveSkillEffect {

  def getNamebyNum(effectnum: Int): String = {
    case 1 => ActiveSkillEffect.Explosion.nameOnUI
    case 2 => ActiveSkillEffect.Blizzard.nameOnUI
    case 3 => ActiveSkillEffect.Meteo.nameOnUI
    case _ => "未設定"
  }

  def fromSqlName(sqlName: String): Option[ActiveSkillEffect] = {
    case "ef_explosion" => Some(ActiveSkillEffect.Explosion)
    case "ef_blizzard" => Some(ActiveSkillEffect.Blizzard)
    case "ef_meteo" => Some(ActiveSkillEffect.Meteo)
  }

}
