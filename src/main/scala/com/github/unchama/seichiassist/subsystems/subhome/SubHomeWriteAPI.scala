package com.github.unchama.seichiassist.subsystems.subhome

import com.github.unchama.seichiassist.subsystems.subhome.domain.SubHome
import org.bukkit.Location
import simulacrum.typeclass

import java.util.UUID

trait SubHomeWriteAPI[F[_]] {
  def updateLocation(player: UUID, id: SubHome.ID, location: Location): F[Unit]

  def updateName(player: UUID, id: SubHome.ID, name: String): F[Unit]
}

object SubHomeWriteAPI {

  def apply[F[_]](implicit ev: SubHomeWriteAPI[F]): SubHomeWriteAPI[F] = ev

}
