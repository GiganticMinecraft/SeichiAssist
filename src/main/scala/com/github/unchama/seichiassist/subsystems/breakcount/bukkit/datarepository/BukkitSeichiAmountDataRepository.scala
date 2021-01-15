package com.github.unchama.seichiassist.subsystems.breakcount.bukkit.datarepository

import cats.effect.SyncEffect
import com.github.unchama.datarepository.bukkit.player.PreLoginToQuitPlayerDataRepository
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.subsystems.breakcount.domain.{SeichiAmountData, SeichiAmountDataPersistence}
import org.bukkit.entity.Player

import java.util.UUID

class BukkitSeichiAmountDataRepository[F[_] : SyncEffect](implicit persistence: SeichiAmountDataPersistence[F],
                                                          effectEnvironment: EffectEnvironment)
  extends PreLoginToQuitPlayerDataRepository[F, SeichiAmountData] {

  import cats.implicits._

  override protected val loadData: (String, UUID) => F[Either[Option[String], SeichiAmountData]] =
    (_, uuid) => persistence.read(uuid)
      .map(option => Right(option.getOrElse(SeichiAmountData.initial)))
  override protected val finalizeBeforeUnload: (Player, SeichiAmountData) => F[Unit] =
    (player, data) => persistence.write(player.getUniqueId, data)
}
