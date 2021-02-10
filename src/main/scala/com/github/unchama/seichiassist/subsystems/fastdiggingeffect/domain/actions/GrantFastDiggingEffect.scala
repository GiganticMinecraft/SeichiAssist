package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.actions

trait GrantFastDiggingEffect[F[_], Player] {

  def forASecond(player: Player)(amount: Int): F[Unit]

}

object GrantFastDiggingEffect {

  def apply[F[_], Player](implicit ev: GrantFastDiggingEffect[F, Player]): GrantFastDiggingEffect[F, Player] = ev

}
