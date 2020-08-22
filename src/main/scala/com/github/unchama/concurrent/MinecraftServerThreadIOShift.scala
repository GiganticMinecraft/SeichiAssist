package com.github.unchama.concurrent

import cats.effect.{ContextShift, IO}

/**
 * マインクラフトサーバーが走るスレッドへの `ContextShift`
 */
trait MinecraftServerThreadIOShift extends ContextShift[IO]
