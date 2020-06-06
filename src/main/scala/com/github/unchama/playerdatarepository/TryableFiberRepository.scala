package com.github.unchama.playerdatarepository

import java.util.UUID

import cats.effect.{ContextShift, IO, SyncIO}
import com.github.unchama.generic.effect.{Mutex, TryableFiber}
import com.github.unchama.playerdatarepository.TryableFiberRepository.MFiber
import org.bukkit.entity.Player

class TryableFiberRepository(implicit shift: ContextShift[IO]) extends PlayerDataOnMemoryRepository[MFiber] {

  override val loadData: (String, UUID) => SyncIO[Either[Option[String], MFiber]] =
    (_, _) => Mutex.of[SyncIO, IO, TryableFiber[IO, Unit]](TryableFiber.unit[IO]).map(Right.apply)

  override val unloadData: (Player, MFiber) => IO[Unit] = (_, mf) =>
      mf.lockAndModify { fiber =>
        for {
          _ <- fiber.cancel
        } yield (fiber, ())
      }

  /**
   * 与えられたプレーヤーに対して、
   *  - 実行中のFiberがあれば停止し、Mutexの中身を停止したFiberで上書きしfalseを返す
   *  - そうでなければ、Mutexの中身を `startNewFiber` により提供されるFiberにて上書きしtrueを返す
   * ような計算を返す。
   */
  def flipState(p: Player)(startNewFiber: IO[TryableFiber[IO, Unit]]): IO[Boolean] =
    apply(p).lockAndModify { fiber =>
      for {
        wasIncomplete <- fiber.cancelIfIncomplete
        newFiber <- if (wasIncomplete) IO.pure(TryableFiber.unit[IO]) else startNewFiber
        // Fiberが完了していた <=> 新たなFiberをmutexへ入れた
      } yield (newFiber, !wasIncomplete)
    }
}

object TryableFiberRepository {
  type MFiber = Mutex[IO, TryableFiber[IO, Unit]]
}
