package com.github.unchama.bungeesemaphoreresponder.infrastructure.redis

import com.github.unchama.bungeesemaphoreresponder.domain.PlayerName

object SignalFormat {

  final val lockKeyPrefix = "bungee_semaphore_"

  def lockKeyOf(playerName: PlayerName): String = s"$lockKeyPrefix${playerName.value}"

  sealed trait BungeeSemaphoreMessage

  case class ReleaseDataLock(playerName: PlayerName) extends BungeeSemaphoreMessage

  case class DataSaveFailed(playerName: PlayerName) extends BungeeSemaphoreMessage

}
