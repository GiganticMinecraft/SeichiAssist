package com.github.unchama.seichiassist.subsystems.subhome

import com.github.unchama.seichiassist.subsystems.subhome.domain.{SubHomeId, SubHome}
import org.bukkit.Location

import java.util.UUID

trait SubHomeReadAPI[F[_]] {

  def list(ownerUuid: UUID): F[Map[SubHomeId, SubHome]]

  def get(ownerUuid: UUID, id: SubHomeId): F[Option[SubHome]]

}

object SubHomeReadAPI {

  def apply[F[_]](implicit ev: SubHomeReadAPI[F]): SubHomeReadAPI[F] = ev

}

trait SubHomeWriteAPI[F[_]] {

  def updateLocation(ownerUuid: UUID, id: SubHomeId, location: Location): F[Unit]

  def updateName(ownerUuid: UUID, id: SubHomeId, name: String): F[Unit]

}

object SubHomeWriteAPI {

  def apply[F[_]](implicit ev: SubHomeWriteAPI[F]): SubHomeWriteAPI[F] = ev

}

trait SubHomeAPI[F[_]] extends SubHomeReadAPI[F] with SubHomeWriteAPI[F]
