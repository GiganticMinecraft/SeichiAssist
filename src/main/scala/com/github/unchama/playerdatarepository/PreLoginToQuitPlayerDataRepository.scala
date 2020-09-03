package com.github.unchama.playerdatarepository

import java.util.UUID

import cats.effect.{ConcurrentEffect, ContextShift, SyncEffect, SyncIO}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import org.bukkit.entity.Player
import org.bukkit.event.player.{AsyncPlayerPreLoginEvent, PlayerJoinEvent, PlayerQuitEvent}
import org.bukkit.event.{EventHandler, EventPriority, Listener}

/**
 * プレーヤーに値を関連付けるオンメモリデータリポジトリのクラス。
 *
 * [[AsyncPlayerPreLoginEvent]] の [[EventPriority.LOW]] から、
 * [[PlayerQuitEvent]] の [[EventPriority.HIGHEST]] までのデータの取得を保証する。
 *
 * @tparam R プレーヤーに関連付けられるデータの型
 */
abstract class PreLoginToQuitPlayerDataRepository[
  AsyncContext[_] : ConcurrentEffect : ContextShift,
  SyncContext[_] : SyncEffect : ContextCoercion[*[_], AsyncContext],
  R
](implicit environment: EffectEnvironment) extends PlayerDataRepository[R] with Listener {

  import scala.collection.mutable

  private val state: mutable.HashMap[UUID, R] = mutable.HashMap()

  /**
   * 名前が[[String]]、UUIDが[[UUID]]にて識別されるプレーヤーがサーバーに参加したときに、
   * リポジトリに格納するデータを計算する。
   *
   * 計算は `Either[String, Data]` を返し、`Left[Option[String]]` は
   * 読み込みに失敗したことをエラーメッセージ付きで、
   * `Right[Data]` は[[R]]の読み込みに成功したことを示す。
   *
   * 読み込み処理が失敗した、つまり`Left[Option[String]]`が計算結果として返った場合は、
   * エラーメッセージをキックメッセージとして参加したプレーヤーをキックする。
   *
   * この計算は必ず同期的に実行される。
   * 何故なら、プレーヤーのjoin処理が終了した時点で
   * このリポジトリはそのプレーヤーに関する[[R]]を格納している必要があるからである。
   *
   * [[Player]] を使って初期化する必要があるケースでは [[PreLoginToQuitPlayerDataRepository]] の使用を検討せよ。
   */
  protected val loadData: (String, UUID) => SyncContext[Either[Option[String], R]]

  /**
   * プレーヤーが退出したときに、格納されたデータをもとに終了処理を行う。
   */
  protected val unloadData: (Player, R) => SyncContext[Unit]

  final def apply(player: Player): R = state(player.getUniqueId)

  import ContextCoercion._
  import cats.effect.implicits._
  import cats.implicits._

  @EventHandler(priority = EventPriority.LOWEST)
  final def onPlayerPreLogin(event: AsyncPlayerPreLoginEvent): Unit = {
    loadData(event.getName, event.getUniqueId).runSync[SyncIO].unsafeRunSync() match {
      case Left(errorMessageOption) =>
        errorMessageOption.foreach(event.setKickMessage)
        event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER)
      case Right(data) =>
        state(event.getUniqueId) = data
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  final def onPlayerJoin(event: PlayerJoinEvent): Unit = {
    val player = event.getPlayer

    if (!state.contains(player.getUniqueId)) {
      val message =
        s"""
           |データの読み込みに失敗しました。
           |再接続しても改善されない場合は、
           |整地鯖公式Discordサーバーからお知らせ下さい。
           |
           |エラー： $getClass の初期化に失敗しています。
           |""".stripMargin

      player.kickPlayer(message)
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  final def onPlayerLeave(event: PlayerQuitEvent): Unit = {
    val player = event.getPlayer
    val uuid = player.getUniqueId
    val storedValue = state.remove(uuid).get

    environment.runEffectAsync(
      s"プレーヤー退出時にデータをアンロードする(${getClass.getName})",
      ContextShift[AsyncContext].shift >> unloadData(player, storedValue).coerceTo[AsyncContext]
    )
  }

}
