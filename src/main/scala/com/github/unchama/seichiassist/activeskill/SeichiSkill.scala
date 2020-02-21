package com.github.unchama.seichiassist.activeskill

import enumeratum._

sealed abstract class SeichiSkill(val name: String,
                                  val range: SkillRange,
                                  val coolDownSeconds: Option[Double],
                                  val manaCost: Int,
                                  val requiredActiveSkillPoint: Int) extends EnumEntry

sealed abstract class ActiveSkill(name: String,
                                  range: SkillRange,
                                  coolDownSeconds: Option[Double],
                                  manaCost: Int,
                                  requiredActiveSkillPoint: Int) extends
  SeichiSkill(name, range, coolDownSeconds, manaCost, requiredActiveSkillPoint)

sealed abstract class AssaultSkill(name: String,
                                   range: AssaultSkillRange,
                                   manaCost: Int,
                                   requiredActiveSkillPoint: Int) extends
  SeichiSkill(name, range, None, manaCost, requiredActiveSkillPoint)

object SeichiSkill extends Enum[SeichiSkill] {
  import ActiveSkillRange._
  import AssaultSkillRange._

  case object DualBreak extends ActiveSkill("デュアル・ブレイク", singleArea(1, 2, 1), None, 1, 10)
  case object TrialBreak extends ActiveSkill("トリアル・ブレイク", singleArea(3, 2, 1), None, 3, 20)
  case object Explosion extends ActiveSkill("エクスプロージョン", singleArea(3, 3, 3), None, 12, 30)
  case object MirageFlare extends ActiveSkill("ミラージュ・フレア", singleArea(5, 5, 3), Some(0.7), 30, 40)
  case object Dockarn extends ActiveSkill("ドッ・カーン", singleArea(7, 7, 5), Some(1.5), 70, 50)
  case object GiganticBomb extends ActiveSkill("ギガンティック・ボム", singleArea(9, 9, 7), Some(2.5), 100, 60)
  case object BrilliantDetonation extends ActiveSkill("ブリリアント・デトネーション", singleArea(11, 11, 9), Some(3.5), 200, 70)
  case object LemuriaImpact extends ActiveSkill("レムリア・インパクト", singleArea(13, 13, 11), Some(5.0), 350, 80)
  case object EternalVice extends ActiveSkill("エターナル・ヴァイス", singleArea(15, 15, 13), Some(7.0), 500, 90)

  case object TomBoy extends ActiveSkill("トム・ボウイ", MultiArea(3, 3, 3)(3), Some(0.6), 28, 40)
  case object Thunderstorm extends ActiveSkill("サンダーストーム", MultiArea(3, 3, 3)(7), Some(1.4), 65, 50)
  case object StarlightBreaker extends ActiveSkill("スターライト・ブレイカー", MultiArea(5, 5, 5)(3), Some(2.4), 90, 60)
  case object EarthDivide extends ActiveSkill("アース・ディバイド", MultiArea(5, 5, 5)(5), Some(3.4), 185, 70)
  case object HeavenGaeBolg extends ActiveSkill("ヘヴン・ゲイボルグ", MultiArea(7, 7, 7)(3), Some(4.8), 330, 80)
  case object Decision extends ActiveSkill("ディシジョン", MultiArea(7, 7, 7)(7), Some(6.8), 480, 90)

  case object EbifriDrive extends ActiveSkill("エビフライ・ドライブ", RemoteArea(3, 3, 3), Some(0.2), 18, 40)
  case object HolyShot extends ActiveSkill("ホーリー・ショット", RemoteArea(5, 5, 3), Some(1.3), 35, 50)
  case object TsarBomba extends ActiveSkill("ツァーリ・ボンバ", RemoteArea(7, 7, 5), Some(1.6), 80, 60)
  case object ArcBlast extends ActiveSkill("アーク・ブラスト", RemoteArea(9, 9, 7), Some(2.7), 110, 70)
  case object PhantasmRay extends ActiveSkill("ファンタズム・レイ", RemoteArea(11, 11, 9), Some(3.8), 220, 80)
  case object Supernova extends ActiveSkill("スーパー・ノヴァ", RemoteArea(13, 13, 11), Some(5.5), 380, 90)

  case object WhiteBreath extends AssaultSkill("ホワイト・ブレス", condenseWater(7, 7, 7), 30, 70)
  case object AbsoluteZero extends AssaultSkill("アブソリュート・ゼロ", condenseWater(11, 11, 11), 80, 80)
  case object DiamondDust extends AssaultSkill("ダイヤモンドダスト", condenseWater(15, 15, 15), 160, 90)

  case object LavaCondensation extends AssaultSkill("ラヴァ・コンデンセーション", condenseLava(7, 7, 7), 20, 70)
  case object MoerakiBoulders extends AssaultSkill("モエラキ・ボールダーズ", condenseLava(11, 11, 11), 60, 80)
  case object Eldfell extends AssaultSkill("エルト・フェットル", condenseLava(13, 13, 13), 150, 90)

  case object VenderBlizzard extends AssaultSkill("ヴェンダー・ブリザード", condenseLiquid(11, 11, 11), 170, 110)

  case object AssaultArmor extends AssaultSkill("アサルト・アーマー", armor(11, 11, 11), 600, 0)

  override def values: IndexedSeq[SeichiSkill] = findValues
}
