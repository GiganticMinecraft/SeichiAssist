package com.github.unchama.seichiassist.subsystems.subhome.infrastructure

import com.github.unchama.seichiassist.subsystems.subhome.domain.SubHome
import org.bukkit.Location
import simulacrum.typeclass

import java.util.UUID

@typeclass trait SubHomeWriteAPI[F[_]] {
  def updateLocation(player: UUID, id: SubHome.ID, location: Location): F[Unit]

  def updateName(player: UUID, id: SubHome.ID, name: String): F[Unit]
}
