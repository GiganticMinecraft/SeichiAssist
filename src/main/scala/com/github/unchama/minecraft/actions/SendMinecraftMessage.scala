package com.github.unchama.minecraft.actions

import cats.{Applicative, ~>}
import com.github.unchama.generic.ContextCoercion
import org.bukkit.entity.Player

trait SendMinecraftMessage[F[_], Target] {

  def string(player: Target, s: String): F[Unit]

  import cats.implicits._

  def list(player: Target, strings: List[String])(implicit F: Applicative[F]): F[Unit] =
    strings.traverse(string(player, _)).as(())

  def mapK[G[_]](fk: F ~> G): SendMinecraftMessage[G, Target] =
    (player: Target, s: String) => fk(SendMinecraftMessage.this.string(player, s))
}

object SendMinecraftMessage {

  def apply[F[_], Player](
    implicit ev: SendMinecraftMessage[F, Player]
  ): SendMinecraftMessage[F, Player] = implicitly

  implicit def coercion[F[_], G[_]](
    implicit ev: SendMinecraftMessage[F, Player],
    fg: ContextCoercion[F, G]
  ): SendMinecraftMessage[G, Player] =
    ev.mapK(fg)

}
