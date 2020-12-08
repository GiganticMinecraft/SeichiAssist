package com.github.unchama.bungeesemaphoreresponder.domain

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

}
