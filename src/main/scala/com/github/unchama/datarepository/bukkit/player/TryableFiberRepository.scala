package com.github.unchama.datarepository.bukkit.player

import java.util.UUID

import cats.effect.{ConcurrentEffect, ContextShift, IO, SyncEffect}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.generic.effect.{Mutex, TryableFiber}
import org.bukkit.entity.Player

class TryableFiberRepository[
  AsyncContext[_] : ConcurrentEffect : ContextShift,
  SyncContext[_] : SyncEffect : ContextCoercion[*[_], AsyncContext]
](implicit environment: EffectEnvironment) extends PreLoginToQuitPlayerDataRepository[
  AsyncContext,
  SyncContext,
  Mutex[AsyncContext, SyncContext, TryableFiber[AsyncContext, Unit]]
] {

  type MFiber = Mutex[AsyncContext, SyncContext, TryableFiber[AsyncContext, Unit]]

  import cats.effect.implicits._
  import cats.implicits._

  override val loadData: (String, UUID) => SyncContext[Either[Option[String], MFiber]] =
    (_, _) => Mutex.of[AsyncContext, SyncContext, TryableFiber[AsyncContext, Unit]] {
      TryableFiber.unit[AsyncContext]
    }.map(Right.apply)

  override val unloadData: (Player, MFiber) => SyncContext[Unit] = (_, fiberMutex) => {
    fiberMutex.readLatest >>= { fiber =>
      fiber.cancel
        .runAsync(_ => IO.unit)
        .runSync[SyncContext]
    }
  }.as(())

  /**
   * 与えられたプレーヤーに対して、
   *  - 実行中のFiberがあれば停止し、Mutexの中身を停止したFiberで上書きしfalseを返す
   *  - そうでなければ、Mutexの中身を `startNewFiber` により提供されるFiberにて上書きしtrueを返す
   *    ような計算を返す。
   */
  def flipState(p: Player)(startNewFiber: AsyncContext[TryableFiber[AsyncContext, Unit]]): AsyncContext[Boolean] =
    apply(p).lockAndModify { fiber =>
      for {
        wasIncomplete <- fiber.cancelIfIncomplete
        newFiber <- if (wasIncomplete) TryableFiber.unit[AsyncContext].pure[AsyncContext] else startNewFiber
        // Fiberが完了していた <=> 新たなFiberをmutexへ入れた
      } yield (newFiber, !wasIncomplete)
    }

  /**
   * プレーヤーが
   *  - 実行中のFiberを持っていればそれを止めtrueを返し
   *  - そうでなければfalseを返す
   *    ようなIOを返す
   */
  def stopAnyFiber(p: Player): AsyncContext[Boolean] =
    apply(p).lockAndModify { fiber =>
      for {
        wasIncomplete <- fiber.cancelIfIncomplete
      } yield (TryableFiber.unit[AsyncContext], wasIncomplete)
    }
}

