package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

trait FairySpawnRequest[F[_], Player] {

  /**
   * 妖精の召喚をリクエストする
   * 召喚に失敗した場合はエラーを返す
   * 成功した場合は召喚する作用を返す
   */
  def spawnRequest(player: Player): F[FairySpawnRequestErrorOrSpawn[F]]

}
