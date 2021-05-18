package com.github.unchama.seichiassist.subsystems.subhome

import com.github.unchama.seichiassist.subsystems.subhome.domain.SubHome
import simulacrum.typeclass

import java.util.UUID

trait SubHomeReadAPI[F[_]] {

  def get(player: UUID, number: SubHome.ID): F[Option[SubHome]]

  def list(player: UUID): F[Map[SubHome.ID, SubHome]]

}

object SubHomeReadAPI {

  def apply[F[_]](implicit ev: SubHomeReadAPI[F]): SubHomeReadAPI[F] = ev

}
