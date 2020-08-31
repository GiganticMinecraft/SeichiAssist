package com.github.unchama.concurrent

import cats.effect.ContextShift

/**
 * マインクラフトサーバーが走るスレッドへの `ContextShift`
 */
trait MinecraftServerThreadShift[F[_]] extends ContextShift[F]
