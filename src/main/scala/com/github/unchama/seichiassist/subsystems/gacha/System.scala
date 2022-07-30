package com.github.unchama.seichiassist.subsystems.gacha

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.LotteryOfGachaItems
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.command.GachaCommand
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.listeners.PlayerPullGachaListener
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaPrizeId
import com.github.unchama.seichiassist.subsystems.gacha.domain.bukkit.GachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.infrastructure.bukkit.JdbcGachaPrizeListPersistence
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.infrastructure.JdbcGachaTicketFromAdminTeamGateway
import org.bukkit.command.TabExecutor
import org.bukkit.event.Listener

trait System[F[_]] extends Subsystem[F] {
  val api: GachaAPI[F]
}

object System {

  def wired[F[_]: OnMinecraftServerThread: NonServerThreadContextShift: ConcurrentEffect]
    : System[F] = {
    implicit val gachaPersistence: JdbcGachaPrizeListPersistence[F] =
      new JdbcGachaPrizeListPersistence[F]()
    implicit val gachaTicketPersistence: JdbcGachaTicketFromAdminTeamGateway[F] =
      new JdbcGachaTicketFromAdminTeamGateway[F]

    new System[F] {
      override implicit val api: GachaAPI[F] = new GachaAPI[F] {

        import cats.implicits._

        override protected implicit val _FSync: Sync[F] = implicitly[ConcurrentEffect[F]]

        override def load: F[Unit] = gachaPersistence.list.flatMap { gachaPrizes =>
          gachaPrizesListRepository.set(gachaPrizes)
        }

        override def replace(gachaPrizesList: Vector[GachaPrize]): F[Unit] =
          gachaPrizesListRepository.set(gachaPrizesList)

        override def runLottery(amount: Int): F[Vector[GachaPrize]] =
          new LotteryOfGachaItems[F].runLottery(amount, gachaPrizesListRepository)

        override def removeByGachaPrizeId(gachaPrizeId: GachaPrizeId): F[Unit] = for {
          prizes <- list
          targetPrize = prizes.filter(_.id == gachaPrizeId)
          _ <- replace(prizes.diff(targetPrize))
        } yield ()

        /**
         * `GachaPrize`を追加する。
         * `GachaPrizeId`を与えなかった場合は最大`GachaPrizeId`の次の値が指定されます
         */
        override def addGachaPrize(gachaPrize: GachaPrizeId => GachaPrize): F[Unit] = for {
          prizes <- list
          newList = prizes ++ Vector(
            gachaPrize(GachaPrizeId(if (prizes.nonEmpty) prizes.map(_.id.id).max + 1 else 1))
          )
          _ <- replace(newList)
        } yield ()

        override protected val gachaPrizesListRepository: Ref[F, Vector[GachaPrize]] =
          Ref.unsafe[F, Vector[GachaPrize]](Vector.empty)
      }
      override val commands: Map[String, TabExecutor] = Map(
        "gacha" -> new GachaCommand[F]().executor
      )
      override val listeners: Seq[Listener] = Seq(new PlayerPullGachaListener[F]())
    }
  }

}
