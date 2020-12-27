package com.github.unchama.minecraft.actions

import cats.Applicative

trait SendMinecraftMessage[F[_], Target] {

  def string(player: Target, s: String): F[Unit]

  import cats.implicits._

  def list(player: Target, strings: List[String])(implicit F: Applicative[F]): F[Unit] =
    strings.traverse(string(player, _)).as(())

}

object SendMinecraftMessage {

  def apply[F[_], Player](implicit ev: SendMinecraftMessage[F, Player]): SendMinecraftMessage[F, Player] = implicitly

}
