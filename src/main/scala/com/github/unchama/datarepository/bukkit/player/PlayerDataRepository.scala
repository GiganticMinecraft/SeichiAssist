package com.github.unchama.datarepository.bukkit.player

import cats.Monad
import com.github.unchama.datarepository.KeyedDataRepository
import org.bukkit.entity.Player

import scala.annotation.tailrec

/**
 * ログイン中の[[Player]]に対して必ず `R` を返せるようなリポジトリ。
 *
 * どの期間についてプレーヤーに対して値を返せるかどうかは実装に依存するが、 遅くても `PlayerJoinEvent` の `EventPriority.HIGH` から、 早くても
 * `PlayerQuitEvent` の `EventPriority.HIGHEST` まではデータが存在することが保証されている。
 *
 * プレーヤーがログインしている間は、[[Player]]を適用して得られる値が不変であることを保証する。
 *
 * @tparam R
 *   レポジトリが [[Player]] に関連付ける値の型
 */
trait PlayerDataRepository[R] extends KeyedDataRepository[Player, R] {

  override def apply(player: Player): R

}

object PlayerDataRepository {

  def unlift[R](f: Player => Option[R]): PlayerDataRepository[R] =
    new PlayerDataRepository[R] {
      override def apply(player: Player): R = f(player).get

      override def isDefinedAt(x: Player): Boolean = f(x).isDefined
    }

  implicit def playerDataRefRepositoryMonad: Monad[PlayerDataRepository] =
    new Monad[PlayerDataRepository] {
      override def pure[A](x: A): PlayerDataRepository[A] = {
        new PlayerDataRepository[A] {
          override def apply(player: Player): A = x

          override def isDefinedAt(x: Player): Boolean = true
        }
      }

      override def flatMap[A, B](
        fa: PlayerDataRepository[A]
      )(f: A => PlayerDataRepository[B]): PlayerDataRepository[B] = {
        new PlayerDataRepository[B] {
          override def apply(player: Player): B = f(fa(player))(player)

          override def isDefinedAt(x: Player): Boolean = fa.isDefinedAt(x)
        }
      }

      override def tailRecM[A, B](
        a: A
      )(f: A => PlayerDataRepository[Either[A, B]]): PlayerDataRepository[B] = {
        @tailrec
        def go(player: Player)(current: A): Option[B] =
          f(current).lift(player) match {
            case Some(Left(nextA))  => go(player)(nextA)
            case Some(Right(value)) => Some(value)
            case None               => None
          }

        new PlayerDataRepository[B] {
          override def apply(player: Player): B = go(player)(a).get

          override def isDefinedAt(x: Player): Boolean = go(x)(a).isDefined
        }
      }
    }

}
