package com.github.unchama.seichiassist.subsystems.subhome

import com.github.unchama.seichiassist.subsystems.subhome.domain.SubHome
import simulacrum.typeclass

import java.util.UUID

@typeclass trait SubHomeReadAPI[F[_]] {
  def get(player: UUID, number: SubHome.ID): F[Option[SubHome]]

  def list(player: UUID): F[Map[SubHome.ID, SubHome]]
}
