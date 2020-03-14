package com.github.unchama.seichiassist.activeskill

import com.github.unchama.generic.CachedFunction

object SkillDependency {
  import SeichiSkill._
  
  type Dependency = (SeichiSkill, SeichiSkill)

  val dependency: Seq[Dependency] = {
    val intermediate = Seq(
      DualBreak -> TrialBreak,
      TrialBreak -> Explosion,
      Explosion -> MirageFlare,
      MirageFlare -> Dockarn,
      Dockarn -> GiganticBomb,
      GiganticBomb -> BrilliantDetonation,
      BrilliantDetonation -> LemuriaImpact,
      LemuriaImpact -> EternalVice,

      Explosion -> TomBoy,
      TomBoy -> Thunderstorm,
      Thunderstorm -> StarlightBreaker,
      StarlightBreaker -> EarthDivide,
      EarthDivide -> HeavenGaeBolg,
      HeavenGaeBolg -> Decision,

      Explosion -> EbifriDrive,
      EbifriDrive -> HolyShot,
      HolyShot -> TsarBomba,
      TsarBomba -> ArcBlast,
      ArcBlast -> PhantasmRay,
      PhantasmRay -> Supernova,

      Explosion -> WhiteBreath,
      WhiteBreath -> AbsoluteZero,
      AbsoluteZero -> DiamondDust,

      Explosion -> LavaCondensation,
      LavaCondensation -> MoerakiBoulders,
      MoerakiBoulders -> Eldfell,

      Eldfell -> VenderBlizzard,
      Eldfell -> VenderBlizzard,
    )

    val assaultArmorDependency = for {
      skill <- SeichiSkill.values if Seq(VenderBlizzard, AssaultArmor).contains(skill)
    } yield skill -> AssaultArmor

    intermediate ++ assaultArmorDependency
  }

  val prerequisites: SeichiSkill => Seq[SeichiSkill] =
    CachedFunction { skill => dependency.filter(_._2 == skill).map(_._1) }
}
