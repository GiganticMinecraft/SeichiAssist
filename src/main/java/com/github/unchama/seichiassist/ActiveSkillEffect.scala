package com.github.unchama.seichiassist

enum class ActiveSkillEffect (
    val num: Int,
    val nameOnDatabase: String,
    val nameOnUI: String,
    val explanation: String,
    val usePoint: Int,
    val material: Material) {

  EXPLOSION(1, "ef_explosion", "${ChatColor.RED}エクスプロージョン", "単純な爆発", 50, Material.TNT),
  BLIZZARD(2, "ef_blizzard", "${ChatColor.AQUA}ブリザード", "凍らせる", 70, Material.PACKED_ICE),
  METEO(3, "ef_meteo", "${ChatColor.DARK_RED}メテオ", "隕石を落とす", 100, Material.FIREBALL);

  internal var plugin = SeichiAssist.instance

  //エフェクトの実行処理分岐 範囲破壊と複数範囲破壊
  def runBreakEffect(player: Player,
                     skillData: ActiveSkillData,
                     tool: ItemStack,
                     breaklist: Set[Block],
                     start: Coordinate, end: Coordinate,
                     standard: Location) {
    val skillId = skillData.skillnum
    when (this) {
      EXPLOSION => ExplosionTask(player, skillId <= 2, tool, breaklist, start.toXYZTuple(), end.toXYZTuple(), standard).runTask(plugin)
      BLIZZARD => {
        val effect = BlizzardTask(player, skillData, tool, breaklist, start, end, standard)

        if (skillId < 3) {
          effect.runTaskLater(plugin, 1)
        } else {
          val period = if (SeichiAssist.DEBUG) 100L else 10L
          effect.runTaskTimer(plugin, 0, period)
        }
      }
      METEO => {
        val delay = if (skillId < 3) 1L else 10L

        MeteoTask(player, skillData, tool, breaklist, start, end, standard)
            .runTaskLater(plugin, delay)
      }
    }
  }

  //エフェクトの実行処理分岐
  def runArrowEffect(player: Player) {
    val effect = when (this@ActiveSkillEffect) {
      EXPLOSION => ArrowEffects.singleArrowExplosionEffect
      BLIZZARD => ArrowEffects.singleArrowBlizzardEffect
      METEO => ArrowEffects.singleArrowMeteoEffect
    }

    GlobalScope.launch(Schedulers.async) { effect.runFor(player) }
  }
}

object ActiveSkillEffect {
  def getNamebyNum(effectnum: Int): String = values()
    .find { activeSkillEffect => activeSkillEffect.num == effectnum }
  ?.let { it.nameOnUI } ?: "未設定"

  def fromSqlName(sqlName: String): ActiveSkillEffect? = values()
    .find { effect => sqlName == effect.nameOnDatabase }
}
