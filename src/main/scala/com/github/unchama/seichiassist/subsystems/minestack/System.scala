package com.github.unchama.seichiassist.subsystems.minestack

import cats.effect.SyncEffect
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.generic.ListExtra
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.minestack.application.repository.MineStackObjectRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.minestack.bukkit.MineStackObjectList
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.{
  MineStackObject,
  MineStackObjectPersistence,
  MineStackObjectWithAmount
}
import com.github.unchama.seichiassist.subsystems.minestack.infrastructure.JdbcMineStackObjectPersistence
import org.bukkit.entity.Player

trait System[F[_], Player] extends Subsystem[F] {

  val api: MineStackAPI[F, Player]

}

object System {

  import cats.implicits._

  def wired[F[_]: SyncEffect]: F[System[F, Player]] = {
    // FIXME: ここでunsafeRunSync()を呼び出さないように修正する
    val allMineStackObjects: List[MineStackObject] =
      MineStackObjectList.getAllMineStackObjects.unsafeRunSync()
    implicit val mineStackObjectPersistence: MineStackObjectPersistence[F] =
      new JdbcMineStackObjectPersistence[F](allMineStackObjects)
    for {
      mineStackObjectRepositoryControls <- BukkitRepositoryControls.createHandles(
        MineStackObjectRepositoryDefinition.withContext[F, Player]
      )
    } yield {
      val mineStackObjectRepository = mineStackObjectRepositoryControls.repository
      new System[F, Player] {
        override val api: MineStackAPI[F, Player] = new MineStackAPI[F, Player] {
          override def addStackedAmountOf(
            player: Player,
            mineStackObject: MineStackObject,
            amount: Int
          ): F[Unit] =
            mineStackObjectRepository(player).update { mineStackObjects =>
              ListExtra.rePrependOrAdd(mineStackObjects)(
                _.mineStackObject == mineStackObject,
                {
                  case Some(value) => value.increase(amount)
                  case None        => MineStackObjectWithAmount(mineStackObject, amount)
                }
              )
            }

          override def trySubtractStackedAmountOf(
            player: Player,
            mineStackObject: MineStackObject,
            amount: Int
          ): F[Int] = {
            for {
              oldMineStackObjects <- mineStackObjectRepository(player).get
              updatedMineStackObjects <- mineStackObjectRepository(player).updateAndGet {
                mineStackObjects =>
                  ListExtra.rePrepend(mineStackObjects)(
                    _.mineStackObject == mineStackObject,
                    _.decrease(amount)
                  )
              }
            } yield {
              if (oldMineStackObjects.head != updatedMineStackObjects.head) {
                Math.abs(oldMineStackObjects.head.amount - updatedMineStackObjects.head.amount)
              } else {
                0
              }
            }
          }
        }
      }

    }
  }
}
