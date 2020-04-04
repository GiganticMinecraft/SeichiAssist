package com.github.unchama

import java.util.UUID

import cats.effect.{IO, SyncIO}
import cats.effect.concurrent.Ref
import org.bukkit.entity.Player
import org.bukkit.event.player.{AsyncPlayerPreLoginEvent, PlayerQuitEvent}
import org.bukkit.event.{EventHandler, Listener}

/**
 * ログインしているプレーヤーに追加のデータを関連付けるオンメモリデータリポジトリのクラス。
 *
 * @tparam D プレーヤーに関連付けられるデータの型
 */
abstract class PlayerDataOnMemoryRepository[D] extends Listener {
  import scala.collection.mutable

  private val state: mutable.HashMap[UUID, Ref[IO, D]] = mutable.HashMap()

  /**
   * 名前が[[String]]、UUIDが[[UUID]]にて識別されるプレーヤーがサーバーに参加したときに、
   * リポジトリに格納するデータを計算する。
   *
   * 計算は `Either[String, Data]` を返し、`Left[Option[String]]` は
   * 読み込みに失敗したことをエラーメッセージ付きで、
   * `Right[Data]` は[[D]]の読み込みに成功したことを示す。
   *
   * 読み込み処理が失敗した、つまり`Left[Option[String]]`が計算結果として返った場合は、
   * エラーメッセージをキックメッセージとして参加したプレーヤーをキックする。
   *
   * この計算は必ず同期的に実行される。
   * 何故なら、プレーヤーのjoin処理が終了した時点で
   * このリポジトリはそのプレーヤーに関する[[D]]を格納している必要があるからである。
   */
  abstract val loadData: (String, UUID) => SyncIO[Either[Option[String], D]]

  /**
   * プレーヤーが退出したときに、格納されたデータをもとに終了処理を行う。
   */
  abstract val unloadData: (Player, D) => IO[Unit]

  /**
   * ログイン中の [[Player]] に対して関連付けられた [[D]] への参照を取得する。
   */
  def apply(player: Player): Ref[IO, D] = state(player.getUniqueId)

  @EventHandler def onPlayerJoin(event: AsyncPlayerPreLoginEvent): Unit = {
    loadData(event.getName, event.getUniqueId)
      .unsafeRunSync() match {
      case Left(errorMessageOption) =>
        errorMessageOption.foreach(event.setKickMessage)
        event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER)
      case Right(data) =>
        state(event.getUniqueId) = Ref.unsafe(data)
    }
  }

  @EventHandler def onPlayerLeave(event: PlayerQuitEvent): Unit = {
    val player = event.getPlayer
    val uuid = player.getUniqueId

    state(uuid).get
      .flatMap(unloadData(player, _))
      .unsafeRunSync()

    state.remove(uuid)
  }
}
