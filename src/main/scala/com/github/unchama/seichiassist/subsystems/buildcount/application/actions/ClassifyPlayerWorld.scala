package com.github.unchama.seichiassist.subsystems.buildcount.application.actions

trait ClassifyPlayerWorld[F[_], Player] {

  /**
   * 整地量が加算されるワールドにプレーヤーが居るかどうかを判別する。
   */
  def isInSeichiWorld(player: Player): F[Boolean]

  /**
   * 建築量が加算されるワールドにプレーヤーが居るかどうかを判別する。
   */
  def isInBuildWorld(player: Player): F[Boolean]

}

object ClassifyPlayerWorld {

  def apply[F[_], Player](
    implicit ev: ClassifyPlayerWorld[F, Player]
  ): ClassifyPlayerWorld[F, Player] = ev

}
