package com.github.unchama.seichiassist.subsystems.gacha.application.actions

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gacha.domain.{
  GachaPrizeId,
  GachaPrizesDataOperations
}
import com.github.unchama.seichiassist.subsystems.gacha.domain.bukkit.GachaPrize
import com.github.unchama.seichiassist.util.StaticGachaPrizeFactory

trait DoGachaDrawing[F[_], Player] {

  def draw(player: Player): F[GachaPrize]

}

object DoGachaDrawing {

  def apply[F[_], Player](implicit ev: DoGachaDrawing[F, Player]): DoGachaDrawing[F, Player] =
    ev

  import cats.implicits._

  def draw[F[_]: Sync, Player](
    implicit gachaPrizesDataOperations: GachaPrizesDataOperations[F]
  ): DoGachaDrawing[F, Player] = (player: Player) =>
    for {
      gachaPrizes <- gachaPrizesDataOperations.getGachaPrizesList
    } yield {
      val random = Math.random()

      def getGachaPrize: GachaPrize = {
        gachaPrizes.foldLeft(1.0) { (sum, gachaPrize) =>
          val nowSum = sum - random
          if (nowSum <= random) return gachaPrize
          else nowSum
        }
        GachaPrize(
          StaticGachaPrizeFactory.getGachaRingo,
          1.0,
          isAppendOwner = false,
          GachaPrizeId(0)
        )
      }

      getGachaPrize
    }
}
