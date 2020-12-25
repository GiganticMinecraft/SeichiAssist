package com.github.unchama.buildassist.domain.actions

import com.github.unchama.buildassist.domain.explevel.BuildExpAmount
import simulacrum.typeclass

import java.util.UUID

@typeclass trait IncrementBuildExpAmount[F[_]] {

  def of(uuid: UUID): F[Unit] = of(uuid, BuildExpAmount.ofNonNegative(1))

  def of(uuid: UUID, by: BuildExpAmount): F[Unit]

}
