package com.github.unchama.playerdatarepository

import java.util.UUID

import cats.effect.{Fiber, IO, SyncIO}
import com.github.unchama.generic.effect.TryableFiber
import org.bukkit.entity.Player

class TryableFiberRepository extends PlayerDataOnMemoryRepository[Option[TryableFiber[IO, Unit]]] {
  override val loadData: (String, UUID) => SyncIO[Either[Option[String], Option[TryableFiber[IO, Unit]]]] =
    (_, _) => SyncIO.pure(Right(None))

  override val unloadData: (Player, Option[Fiber[IO, Unit]]) => IO[Unit] = (_, o) => o match {
    case Some(f) => f.cancel
    case None => IO.unit
  }

  import cats.implicits._

  /**
   * 与えられたプレーヤーのアサルトスキルの処理を実行するFiberがあれば止めるIOを返す
   */
  def stopFiber(p: Player): IO[Unit] =
    apply(p).getAndSet(None) >>= {
      case Some(fiber) => fiber.cancel
      case None => IO.unit
    }

  /**
   * 与えられたプレーヤーに対して、
   *  - 実行中のFiberがあれば停止しfalseを返す
   *  - そうでなければ `startNewFiber` により提供されるFiberにてRefを上書きしtrueを返す
   * ような計算を返す。
   */
  def flipState(p: Player)(startNewFiber: IO[TryableFiber[IO, Unit]]): IO[Boolean] = {
    val reference = apply(p)
    val overwriteFiber = startNewFiber >>= (f => reference.set(Some(f)))

    reference.get flatMap {
      case Some(fiber) =>
        for {
          cancelled <- fiber.cancelIfIncomplete
          _ <- if (cancelled) overwriteFiber else IO.unit
        } yield cancelled
      case None => overwriteFiber.as(true)
    }
  }
}
