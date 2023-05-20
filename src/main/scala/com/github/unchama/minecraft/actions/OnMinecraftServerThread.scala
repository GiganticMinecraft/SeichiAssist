package com.github.unchama.minecraft.actions

import cats.effect.SyncEffect

trait OnMinecraftServerThread[F[_]] {

  /**
   * マインクラフトサーバーが走るスレッド上でアクションを実行する。
   */
  def runAction[G[_]: SyncEffect, A](ga: G[A]): F[A]

}

object OnMinecraftServerThread {

  def apply[F[_]](implicit ev: OnMinecraftServerThread[F]): OnMinecraftServerThread[F] = ev

}
