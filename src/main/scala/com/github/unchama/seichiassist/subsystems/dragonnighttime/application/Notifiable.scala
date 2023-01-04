package com.github.unchama.seichiassist.subsystems.dragonnighttime.application

trait Notifiable[F[_]] {
  def notify(message: String): F[Unit]
}

object Notifiable {
  def apply[F[_]: Notifiable]: Notifiable[F] = implicitly
}
