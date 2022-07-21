package com.github.unchama.seichiassist.subsystems.gacha.domain

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gacha.GachaAPI
import com.github.unchama.seichiassist.subsystems.gacha.domain.bukkit.GachaPrize

final class GachaPrizesDataOperations[F[_]: Sync](implicit gachaAPI: GachaAPI[F]) {

  import cats.implicits._

  /**
   * 指定された`GachaPrize`が存在するかどうか
   */
  def existsGachaPrize(gachaPrizeId: GachaPrizeId): F[Boolean] = for {
    gachaPrizes <- gachaAPI.list
  } yield gachaPrizes.exists(_.id == gachaPrizeId)

}
