package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions

trait FairyRoutine[F[_], Player] {

  def start(player: Player): F[Nothing]

}
