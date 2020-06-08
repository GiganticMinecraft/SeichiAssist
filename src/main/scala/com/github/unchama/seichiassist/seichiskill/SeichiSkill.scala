package com.github.unchama.seichiassist.seichiskill

import enumeratum._

sealed trait SeichiSkill extends EnumEntry {
  val name: String
  val range: SkillRange
  val maxCoolDownTicks: Option[Int]
  val manaCost: Int
  val requiredActiveSkillPoint: Int
}

sealed abstract case class ActiveSkill(stringId: String,
                                       override val name: String,
                                       override val range: ActiveSkillRange,
                                       override val maxCoolDownTicks: Option[Int],
                                       override val manaCost: Int,
                                       override val requiredActiveSkillPoint: Int) extends SeichiSkill {
  override val entryName: String = stringId
}

sealed abstract case class AssaultSkill(stringId: String,
                                        override val name: String,
                                        override val range: AssaultSkillRange,
                                        override val manaCost: Int,
                                        override val requiredActiveSkillPoint: Int) extends SeichiSkill {
  override val entryName: String = stringId
  override val maxCoolDownTicks: None.type = None
}

object SeichiSkill extends Enum[SeichiSkill] {
  import ActiveSkillRange._
  import AssaultSkillRange._

  object DualBreak extends ActiveSkill("dual_break", "デュアル・ブレイク", singleArea(1, 2, 1), None, 1, 10)
  object TrialBreak extends ActiveSkill("trial_break", "トリアル・ブレイク", singleArea(3, 2, 1), None, 3, 20)
  object Explosion extends ActiveSkill("explosion", "エクスプロージョン", singleArea(3, 3, 3), None, 12, 30)
  object MirageFlare extends ActiveSkill("mirage_flare", "ミラージュ・フレア", singleArea(5, 3, 5), Some(14), 30, 40)
  object Dockarn extends ActiveSkill("dockarn", "ドッ・カーン", singleArea(7, 5, 7), Some(30), 70, 50)
  object GiganticBomb extends ActiveSkill("gigantic_bomb", "ギガンティック・ボム", singleArea(9, 7, 9), Some(50), 100, 60)
  object BrilliantDetonation extends ActiveSkill("brilliant_detonation", "ブリリアント・デトネーション", singleArea(11, 9, 11), Some(70), 200, 70)
  object LemuriaImpact extends ActiveSkill("lemuria_impact", "レムリア・インパクト", singleArea(13, 11, 13), Some(100), 350, 80)
  object EternalVice extends ActiveSkill("eternal_vice", "エターナル・ヴァイス", singleArea(15, 13, 15), Some(140), 500, 90)

  object TomBoy extends ActiveSkill("tomboy", "トム・ボウイ", MultiArea(3, 3, 3)(3), Some(12), 28, 40)
  object Thunderstorm extends ActiveSkill("thunderstorm", "サンダーストーム", MultiArea(3, 3, 3)(7), Some(28), 65, 50)
  object StarlightBreaker extends ActiveSkill("starlight_breaker", "スターライト・ブレイカー", MultiArea(5, 5, 5)(3), Some(48), 90, 60)
  object EarthDivide extends ActiveSkill("earth_divide", "アース・ディバイド", MultiArea(5, 5, 5)(5), Some(68), 185, 70)
  object HeavenGaeBolg extends ActiveSkill("heaven_gaebolg", "ヘヴン・ゲイボルグ", MultiArea(7, 7, 7)(3), Some(96), 330, 80)
  object Decision extends ActiveSkill("decision", "ディシジョン", MultiArea(7, 7, 7)(7), Some(136), 480, 90)

  object EbifriDrive extends ActiveSkill("ebifri_drive", "エビフライ・ドライブ", RemoteArea(3, 3, 3), Some(4), 18, 40)
  object HolyShot extends ActiveSkill("holy_shot", "ホーリー・ショット", RemoteArea(5, 3, 5), Some(26), 35, 50)
  object TsarBomba extends ActiveSkill("tsar_bomba", "ツァーリ・ボンバ", RemoteArea(7, 5, 7), Some(32), 80, 60)
  object ArcBlast extends ActiveSkill("arc_blast", "アーク・ブラスト", RemoteArea(9, 7, 9), Some(54), 110, 70)
  object PhantasmRay extends ActiveSkill("phantasm_ray", "ファンタズム・レイ", RemoteArea(11, 9, 11), Some(76), 220, 80)
  object Supernova extends ActiveSkill("supernova", "スーパー・ノヴァ", RemoteArea(13, 11, 13), Some(110), 380, 90)

  object WhiteBreath extends AssaultSkill("white_breath", "ホワイト・ブレス", condenseWater(7, 7, 7), 30, 70)
  object AbsoluteZero extends AssaultSkill("absolute_zero", "アブソリュート・ゼロ", condenseWater(11, 11, 11), 80, 80)
  object DiamondDust extends AssaultSkill("diamond_dust", "ダイヤモンドダスト", condenseWater(15, 15, 15), 160, 90)

  object LavaCondensation extends AssaultSkill("lava_condensation", "ラヴァ・コンデンセーション", condenseLava(7, 7, 7), 20, 70)
  object MoerakiBoulders extends AssaultSkill("moeraki_boulders", "モエラキ・ボールダーズ", condenseLava(11, 11, 11), 60, 80)
  object Eldfell extends AssaultSkill("eldfell", "エルト・フェットル", condenseLava(13, 13, 13), 150, 90)

  object VenderBlizzard extends AssaultSkill("vender_blizzard", "ヴェンダー・ブリザード", condenseLiquid(11, 11, 11), 170, 110)

  object AssaultArmor extends AssaultSkill("assault_armor", "アサルト・アーマー", armor(11, 11, 11), 600, 0)

  override def values: IndexedSeq[SeichiSkill] = findValues
}
