package com.github.unchama.bungeesemaphoreresponder.domain

import cats.effect.ConcurrentEffect
import cats.{Applicative, ApplicativeError, ~>}
import com.github.unchama.generic.ContextCoercion

/**
 * The type of a pure callback object that should be invoked when a player quits the Minecraft server.
 *
 * This is an interface to a controller object;
 * within `onQuitOf` the object should do "finalization" of
 * any data that is associated with player's login session.
 */
trait PlayerDataFinalizer[F[_], Player] {

  /**
   * A finalizing action that must be run when the given [[Player]] quits the server.
   *
   * The action returned by this function can be run
   * either on the server main thread or asynchronously at any other thread.
   *
   * Therefore the implementation of this method must not assume the execution on the server main thread.
   * If required, the action may shift the execution context.
   */
  def onQuitOf(player: Player): F[Unit]

  def transformContext[G[_]](trans: F ~> G): PlayerDataFinalizer[G, Player] = {
    PlayerDataFinalizer(player => trans.apply(onQuitOf(player)))
  }

  def coerceContextTo[G[_] : ContextCoercion[F, *[_]]]: PlayerDataFinalizer[G, Player] = transformContext[G](implicitly)

}

object PlayerDataFinalizer {

  def apply[F[_], Player](f: Player => F[Unit]): PlayerDataFinalizer[F, Player] = (player: Player) => f(player)

  import cats.effect.implicits._
  import cats.implicits._

  def sequence[
    F[_] : Applicative,
    Player
  ](finalizers: List[PlayerDataFinalizer[F, Player]]): PlayerDataFinalizer[F, Player] =
    PlayerDataFinalizer {
      player => finalizers.traverse(_.onQuitOf(player)).as(())
    }

  def concurrently[
    F[_] : ConcurrentEffect,
    Player
  ](finalizers: List[PlayerDataFinalizer[F, Player]]): PlayerDataFinalizer[F, Player] =
    PlayerDataFinalizer { player =>
      for {
        fibers <- finalizers.traverse(_.onQuitOf(player).attempt.start)
        results <- fibers.traverse(_.join)
        _ <-
          results.collectFirst { case Left(error) => error } match {
            case Some(error) =>
              ApplicativeError[F, Throwable].raiseError(error)
            case None =>
              Applicative[F].unit
          }
      } yield ()
    }
}
