package com.github.unchama.concurrent

import cats.effect.ContextShift

/**
 * マインクラフトサーバーが走るスレッドへの `ContextShift`
 */
trait MinecraftServerThreadShift[F[_]] extends ContextShift[F]

object MinecraftServerThreadShift {

  def apply[F[_]](implicit ev: MinecraftServerThreadShift[F]): MinecraftServerThreadShift[F] = ev

}
