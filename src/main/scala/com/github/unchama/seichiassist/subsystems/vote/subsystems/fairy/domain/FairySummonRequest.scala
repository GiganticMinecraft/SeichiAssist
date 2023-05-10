package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

trait FairySummonRequest[F[_], Player] {

  /**
   * @return 妖精の召喚をリクエストする作用
   *         失敗した場合は失敗理由
   */
  def summonRequest(player: Player): F[FairySpawnRequestErrorOrSpawn[F]]

}
