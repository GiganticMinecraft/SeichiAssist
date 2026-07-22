package com.github.unchama.minecraft.actions

import cats.effect.SyncIO

trait OnMinecraftServerThread[F[_]] {

  /**
   * マインクラフトサーバーが走るスレッド上でアクションを実行する。
   */
  def runAction[A](action: SyncIO[A]): F[A]

}

object OnMinecraftServerThread {

  def apply[F[_]](implicit ev: OnMinecraftServerThread[F]): OnMinecraftServerThread[F] = ev

}
