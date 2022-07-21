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

  /**
   * `GachaPrize`を追加する。
   * `GachaPrizeId`を与えなかった場合は最大`GachaPrizeId`の次の値が指定されます
   */
  def addGachaPrize(gachaPrize: GachaPrizeId => GachaPrize): F[Unit] = for {
    prizes <- gachaAPI.list
    newList = prizes ++ Vector(gachaPrize(GachaPrizeId(prizes.map(_.id.id).max + 1)))
    _ <- gachaAPI.replace(newList)
  } yield ()

}
