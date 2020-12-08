package com.github.unchama.bungeesemaphoreresponder.domain

case class PlayerFinalizerList[F[_], Player](callbacks: List[PlayerFinalizer[F, Player]]) {

  /**
   * Appends another [[PlayerFinalizer]] to this object
   */
  def withAnotherFinalizer(f: PlayerFinalizer[F, Player]): PlayerFinalizerList[F, Player] =
    PlayerFinalizerList(f :: callbacks)

  /**
   * Get list of all actions, known to this object, that must be run when a given [[Player]] quits the server.
   */
  def allActionsOnQuitOf(player: Player): List[F[Unit]] = callbacks.map(_.onQuitOf(player))

}
