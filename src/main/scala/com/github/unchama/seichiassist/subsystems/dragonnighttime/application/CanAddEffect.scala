package com.github.unchama.seichiassist.subsystems.dragonnighttime.application

import simulacrum.typeclass

@typeclass trait CanAddEffect[F[_]] extends AnyRef {
  val addEffect: F[Unit]
}
