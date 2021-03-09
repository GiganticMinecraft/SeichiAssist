package com.github.unchama.datarepository.bukkit.player

import cats.Monad
import com.github.unchama.datarepository.KeyedDataRepository
import org.bukkit.entity.Player

import scala.annotation.tailrec

/**
 * ログイン中の[[Player]]に対して必ず `R` を返せるようなリポジトリ。
 *
 * どの期間についてプレーヤーに対して値を返せるかどうかは実装に依存するが、
 * 遅くても `PlayerJoinEvent` の `EventPriority.HIGH` から、
 * 早くても `PlayerQuitEvent` の `EventPriority.HIGHEST` まではデータが存在することが保証されている。
 *
 * プレーヤーがログインしている間は、[[Player]]を適用して得られる値が不変であることを保証する。
 *
 * @tparam R レポジトリが [[Player]] に関連付ける値の型
 */
trait PlayerDataRepository[R] extends KeyedDataRepository[Player, R] {

  override def apply(player: Player): R

}

object PlayerDataRepository {

  def fromFunction[R](f: Player => R): PlayerDataRepository[R] = (player: Player) => f(player)

  implicit def playerDataRefRepositoryMonad: Monad[PlayerDataRepository] = new Monad[PlayerDataRepository] {
    override def pure[A](x: A): PlayerDataRepository[A] = (_: Player) => x

    override def flatMap[A, B](fa: PlayerDataRepository[A])(f: A => PlayerDataRepository[B]): PlayerDataRepository[B] =
      player => f(fa(player))(player)

    override def tailRecM[A, B](a: A)(f: A => PlayerDataRepository[Either[A, B]]): PlayerDataRepository[B] = {
      player => {
        @tailrec
        def go(current: A): B = f(current)(player) match {
          case Left(nextA) => go(nextA)
          case Right(value) => value
        }

        go(a)
      }
    }
  }

}
