package com.github.unchama.seichiassist.subsystems.home

import cats.Functor
import com.github.unchama.seichiassist.subsystems.home.domain.{
  Home,
  HomeId,
  HomeLocation,
  OperationResult
}

import java.util.UUID

trait HomeReadAPI[F[_]] {

  def list(ownerUuid: UUID): F[Map[HomeId, Home]]

  def get(ownerUuid: UUID, id: HomeId): F[Option[Home]]

  final def configured(ownerUuid: UUID, id: HomeId)(implicit F: Functor[F]): F[Boolean] =
    F.map(get(ownerUuid, id))(_.nonEmpty)

}

object HomeReadAPI {

  def apply[F[_]](implicit ev: HomeReadAPI[F]): HomeReadAPI[F] = ev

}

trait HomeWriteAPI[F[_]] {

  def upsertLocation(ownerUuid: UUID, id: HomeId)(location: HomeLocation): F[Unit]

  def rename(ownerUuid: UUID, id: HomeId)(name: String): F[OperationResult.RenameResult]

  def remove(ownerUuid: UUID, id: HomeId): F[Boolean]

}

object HomeWriteAPI {

  def apply[F[_]](implicit ev: HomeWriteAPI[F]): HomeWriteAPI[F] = ev

}

trait HomeAPI[F[_]] extends HomeReadAPI[F] with HomeWriteAPI[F]
