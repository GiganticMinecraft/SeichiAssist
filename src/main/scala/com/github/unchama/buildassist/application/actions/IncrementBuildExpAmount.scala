package com.github.unchama.buildassist.application.actions

import cats.Monad
import cats.effect.concurrent.Ref
import com.github.unchama.buildassist.domain.explevel.BuildExpAmount
import com.github.unchama.buildassist.domain.playerdata.BuildAmountData
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.ratelimiting.RateLimiter
import simulacrum.typeclass

import java.util.UUID

@typeclass trait IncrementBuildExpAmount[F[_]] {

  def of(uuid: UUID): F[Unit] = of(uuid, BuildExpAmount.ofNonNegative(1))

  def of(uuid: UUID, by: BuildExpAmount): F[Unit]

}

object IncrementBuildExpAmount {

  import cats.implicits._

  def using[
    F[_] : Monad
  ](rateLimiterRepository: KeyedDataRepository[UUID, RateLimiter[F]])
   (implicit repository: KeyedDataRepository[UUID, Ref[F, BuildAmountData]]): IncrementBuildExpAmount[F] =
    (uuid: UUID, by: BuildExpAmount) =>
      for {
        amountToIncrement <- rateLimiterRepository(uuid).requestPermissionN(by.floor)
        _ <- repository(uuid).update(_.modifyExpAmount(_.incrementBy(amountToIncrement)))
      } yield ()

}
