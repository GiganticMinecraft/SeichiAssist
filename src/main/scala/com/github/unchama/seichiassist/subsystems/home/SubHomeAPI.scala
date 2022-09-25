package com.github.unchama.seichiassist.subsystems.home

import cats.Functor
import com.github.unchama.seichiassist.subsystems.subhome.domain.{
  OperationResult,
  SubHome,
  SubHomeId,
  SubHomeLocation
}

import java.util.UUID

trait SubHomeReadAPI[F[_]] {

  def list(ownerUuid: UUID): F[Map[SubHomeId, SubHome]]

  def get(ownerUuid: UUID, id: SubHomeId): F[Option[SubHome]]

  final def configured(ownerUuid: UUID, id: SubHomeId)(implicit F: Functor[F]): F[Boolean] =
    F.map(get(ownerUuid, id))(_.nonEmpty)

}

object SubHomeReadAPI {

  def apply[F[_]](implicit ev: SubHomeReadAPI[F]): SubHomeReadAPI[F] = ev

}

trait SubHomeWriteAPI[F[_]] {

  def upsertLocation(ownerUuid: UUID, id: SubHomeId)(location: SubHomeLocation): F[Unit]

  def rename(ownerUuid: UUID, id: SubHomeId)(name: String): F[OperationResult.RenameResult]

  def remove(ownerUuid: UUID, id: SubHomeId): F[Boolean]

}

object SubHomeWriteAPI {

  def apply[F[_]](implicit ev: SubHomeWriteAPI[F]): SubHomeWriteAPI[F] = ev

}

trait SubHomeAPI[F[_]] extends SubHomeReadAPI[F] with SubHomeWriteAPI[F]
