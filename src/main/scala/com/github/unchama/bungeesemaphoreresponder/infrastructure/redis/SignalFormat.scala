package com.github.unchama.bungeesemaphoreresponder.infrastructure.redis

import com.github.unchama.bungeesemaphoreresponder.domain.PlayerName

object SignalFormat {

  final val signalingChannel = "BungeeSemaphore"

  object MessagePrefix {
    final val releaseDataLock = "confirm_player_data_saved"
    final val dataSaveFailed = "failed_saving_some_player_data"
  }

  sealed trait BungeeSemaphoreMessage

  case class ReleaseDataLock(playerName: PlayerName) extends BungeeSemaphoreMessage {
    override def toString: String = s"${MessagePrefix.releaseDataLock} ${playerName.value}"
  }

  case class DataSaveFailed(playerName: PlayerName) extends BungeeSemaphoreMessage {
    override def toString: String = s"${MessagePrefix.dataSaveFailed} ${playerName.value}"
  }

}
