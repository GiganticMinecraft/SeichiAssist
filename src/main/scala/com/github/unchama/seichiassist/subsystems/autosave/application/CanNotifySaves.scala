package com.github.unchama.seichiassist.subsystems.autosave.application

import simulacrum.typeclass

@typeclass trait CanNotifySaves[F[_]] {

  def notify(message: String): F[Unit]

}
