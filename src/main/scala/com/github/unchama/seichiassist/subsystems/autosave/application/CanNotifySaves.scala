package com.github.unchama.seichiassist.subsystems.autosave.application

trait CanNotifySaves[F[_]] {

  def notify(message: String): F[Unit]

}

object CanNotifySaves {
  def apply[F[_]: CanNotifySaves]: CanNotifySaves[F] = implicitly
}
