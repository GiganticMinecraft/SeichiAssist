package com.github.unchama.seichiassist.subsystems.dragonnighttime

trait DragonNightTimeApi[F[_]] {
  val isInDragonNightTime: F[Boolean]
}

object DragonNightTimeApi {
  def apply[F[_]](implicit ev: DragonNightTimeApi[F]): DragonNightTimeApi[F] = ev
}
