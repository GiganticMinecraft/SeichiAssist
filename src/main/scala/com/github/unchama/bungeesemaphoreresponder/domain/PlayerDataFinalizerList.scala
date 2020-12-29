package com.github.unchama.bungeesemaphoreresponder.domain

case class PlayerDataFinalizerList[F[_], Player](callbacks: Seq[PlayerDataFinalizer[F, Player]]) {

  /**
   * Appends another [[PlayerDataFinalizer]] to this object
   */
  def withAnotherFinalizer(f: PlayerDataFinalizer[F, Player]): PlayerDataFinalizerList[F, Player] =
    PlayerDataFinalizerList(f +: callbacks)

  /**
   * Appends list of [[PlayerDataFinalizer]] to this object
   */
  def withFinalizers(fs: PlayerDataFinalizerList[F, Player]): PlayerDataFinalizerList[F, Player] =
    PlayerDataFinalizerList(callbacks ++ fs.callbacks)

  /**
   * Get list of all actions, known to this object, that must be run when a given [[Player]] quits the server.
   */
  def allActionsOnQuitOf(player: Player): Seq[F[Unit]] = callbacks.map(_.onQuitOf(player))

}
