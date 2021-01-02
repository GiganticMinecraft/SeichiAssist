package com.github.unchama.bungeesemaphoreresponder.domain.actions

trait BungeeSemaphoreSynchronization[Action, Player] {

  def confirmSaveCompletionOf(player: Player): Action

  def notifySaveFailureOf(player: Player): Action

}
