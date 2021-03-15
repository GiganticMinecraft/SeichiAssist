package com.github.unchama.seichiassist.subsystems.gachapoint.bukkit

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.GrantGachaTicketToAPlayer
import org.bukkit.entity.Player

case class GrantBukkitGachaTicketToAPlayer[
  F[_] : Sync
](player: Player) extends GrantGachaTicketToAPlayer[F] {

  override def give(count: Int): F[Unit] = ???

}
