package com.github.unchama.seichiassist.subsystems.dragonnighttime.application

trait CanBroadcast[F[_]] {
  def broadcast(message: String): F[Unit]
}

object CanBroadcast {
  def apply[F[_]: CanBroadcast]: CanBroadcast[F] = implicitly
}
