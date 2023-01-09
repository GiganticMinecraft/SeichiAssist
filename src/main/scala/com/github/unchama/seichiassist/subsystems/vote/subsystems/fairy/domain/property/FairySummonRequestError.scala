package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

sealed trait FairySummonRequestError

/**
 * 妖精の召喚をする際に発生する可能性があるエラーを列挙する
 */
object FairySummonRequestError {

  /**
   * 整地レベルが足りなかった
   */
  case object NotEnoughSeichiLevel extends FairySummonRequestError

  /**
   * 妖精がすでに召喚されている
   */
  case object AlreadyFairySummoned extends FairySummonRequestError

  /**
   * 妖精の召喚ポイントが足りなかった
   */
  case object NotEnoughEffectPoint extends FairySummonRequestError

}
