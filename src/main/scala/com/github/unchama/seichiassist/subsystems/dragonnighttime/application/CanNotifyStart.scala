package com.github.unchama.seichiassist.subsystems.dragonnighttime.application

import simulacrum.typeclass

@typeclass trait CanNotifyStart[F[_]] {
  def notify(message: String): F[Unit]
}
