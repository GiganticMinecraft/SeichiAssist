package com.github.unchama.datarepository.bukkit.player

import cats.effect.IO
import org.bukkit.entity.Player
import org.bukkit.event.player.{PlayerJoinEvent, PlayerQuitEvent}
import org.bukkit.event.{EventHandler, EventPriority, Listener}

/**
 * プレーヤーに値を関連付けるオンメモリデータリポジトリのクラス。
 *
 * [[PlayerJoinEvent]] の [[EventPriority.LOW]] から、
 * [[PlayerQuitEvent]] の [[EventPriority.HIGHEST]] までのデータの取得を保証する。
 *
 * @tparam R プレーヤーに関連付けられるデータの型
 */
abstract class JoinToQuitPlayerDataRepository[R] extends PlayerDataRepository[R] with Listener {

  import scala.collection.mutable

  private val state: mutable.HashMap[Player, R] = mutable.HashMap()

  /**
   * [[Player]] がサーバーに参加したときに、レポジトリが持つべきデータを計算する。
   *
   * このメソッドは[[onPlayerJoin()]]により同期的に実行されるため、
   * ここで重い処理を行うとサーバーのパフォーマンスに悪影響を及ぼす。
   *
   * DBアクセス等の処理を行う必要がある場合 [[PreLoginToQuitPlayerDataRepository]] の使用を検討せよ。
   */
  protected def initialValue(player: Player): R

  /**
   * プレーヤーが退出したときに、格納されたデータをもとに終了処理を行う。
   */
  protected def unloadData(player: Player, r: R): IO[Unit]

  override def apply(player: Player): R = state(player)

  @EventHandler(priority = EventPriority.LOWEST)
  final def onPlayerJoin(event: PlayerJoinEvent): Unit = {
    val jointPlayer = event.getPlayer

    state(jointPlayer) = initialValue(jointPlayer)
  }

  @EventHandler(priority = EventPriority.MONITOR)
  final def onPlayerLeave(event: PlayerQuitEvent): Unit = {
    val player = event.getPlayer
    val storedValue = state.remove(player).get

    unloadData(player, storedValue).unsafeRunAsync {
      case Left(error) => error.printStackTrace()
      case Right(_) => ()
    }
  }

}
