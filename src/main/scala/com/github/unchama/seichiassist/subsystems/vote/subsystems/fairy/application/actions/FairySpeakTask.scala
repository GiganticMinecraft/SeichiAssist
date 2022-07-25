package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions

trait FairySpeakTask[F[_], Player] {

  def speak(player: Player): F[Unit]

}

object FairySpeakTask {

  def apply[F[_], Player](implicit ev: FairySpeakTask[F, Player]): FairySpeakTask[F, Player] =
    ev

}
