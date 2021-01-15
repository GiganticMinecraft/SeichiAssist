package com.github.unchama.seichiassist.subsystems.breakcount.bukkit.datarepository

import cats.effect.SyncEffect
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.bukkit.player.PreLoginToQuitPlayerDataRepository
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.subsystems.breakcount.domain.{SeichiAmountData, SeichiAmountDataPersistence}
import org.bukkit.entity.Player

import java.util.UUID

class BukkitSeichiAmountDataRepository[F[_] : SyncEffect](implicit persistence: SeichiAmountDataPersistence[F],
                                                          effectEnvironment: EffectEnvironment)
  extends PreLoginToQuitPlayerDataRepository[F, Ref[F, SeichiAmountData]] {

  import cats.implicits._

  override protected val loadData: (String, UUID) => F[Either[Option[String], Ref[F, SeichiAmountData]]] =
    (_, uuid) => for {
      readValue <- persistence.read(uuid)
      ref <- Ref.of(readValue.getOrElse(SeichiAmountData.initial))
    } yield Right(ref)

  override protected val finalizeBeforeUnload: (Player, Ref[F, SeichiAmountData]) => F[Unit] =
    (player, data) => data.get.flatMap(persistence.write(player.getUniqueId, _))
}
