package com.github.unchama.datarepository.bukkit.player

import cats.effect.{ConcurrentEffect, ContextShift, Sync, SyncEffect, SyncIO}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import org.bukkit.entity.Player
import org.bukkit.event.player.{AsyncPlayerPreLoginEvent, PlayerJoinEvent, PlayerQuitEvent}
import org.bukkit.event.{EventHandler, EventPriority, Listener}

import java.util.UUID

/**
 * プレーヤーに値を関連付けるオンメモリデータリポジトリのクラス。
 *
 * このレポジトリは、 `R` の値を用意するために、
 * [[AsyncPlayerPreLoginEvent]] にて非同期に「中間データ」`T` を準備することができる。
 * この中間データは、[[PlayerJoinEvent]] にて `R` の値を初期化する際に利用することができる。
 *
 * [[PlayerJoinEvent]] の [[EventPriority.LOW]] から、
 * [[PlayerQuitEvent]] の [[EventPriority.HIGHEST]] までのデータの取得を保証する。
 *
 * @tparam R プレーヤーに関連付けられるデータの型
 */
abstract class TwoPhasedPlayerDataRepository[
  AsyncContext[_] : ConcurrentEffect : ContextShift,
  SyncContext[_] : SyncEffect : ContextCoercion[*[_], AsyncContext],
  R
](implicit environment: EffectEnvironment) extends PlayerDataRepository[R] with Listener {

  import scala.collection.mutable

  /**
   * 中間データの型
   */
  protected type TemporaryData

  /**
   * 名前が[[String]]、UUIDが[[UUID]]にて識別されるプレーヤーがサーバーに参加したときに、
   * リポジトリに一時的に格納するデータを計算する。
   *
   * 計算は `Either[String, Data]` を返し、`Left[Option[String]]` は
   * 読み込みに失敗したことをエラーメッセージ付きで、
   * `Right[Data]` は[[TemporaryData]]の読み込みに成功したことを示す。
   *
   * 読み込み処理が失敗した、つまり`Left[Option[String]]`が計算結果として返った場合は、
   * エラーメッセージをキックメッセージとして参加したプレーヤーをキックする。
   *
   * この計算は必ず同期的に実行される。
   * 何故なら、プレーヤーのjoin処理が終了した時点で
   * このリポジトリはそのプレーヤーに関する[[TemporaryData]]を格納している必要があるからである。
   */
  protected val loadTemporaryData: (String, UUID) => SyncContext[Either[Option[String], TemporaryData]]

  /**
   * [[Player]] がサーバーに参加し終えた時、レポジトリが持つべきデータを計算する。
   *
   * このメソッドは[[onPlayerJoin()]]により同期的に実行されるため、
   * ここで重い処理を行うとサーバーのパフォーマンスに悪影響を及ぼす。
   *
   * DBアクセス等の処理を行う必要がある場合 [[loadTemporaryData]] で[[TemporaryData]]をロードすることを検討せよ。
   */
  protected def initializeValue(player: Player, temporaryData: TemporaryData): SyncContext[R]

  /**
   * プレーヤーが退出したときに、格納されたデータをもとに終了処理を行う。
   */
  protected val unloadData: (Player, R) => SyncContext[Unit]

  private val state: mutable.HashMap[UUID, R] = mutable.HashMap()

  protected val temporaryState: mutable.HashMap[UUID, TemporaryData] = mutable.HashMap()

  def apply(player: Player): R = state(player.getUniqueId)

  import cats.effect.implicits._

  @EventHandler(priority = EventPriority.LOWEST)
  final def onPlayerPreLogin(event: AsyncPlayerPreLoginEvent): Unit = {
    loadTemporaryData(event.getName, event.getUniqueId)
      .runSync[SyncIO]
      .unsafeRunSync() match {
      case Left(errorMessageOption) =>
        errorMessageOption.foreach(event.setKickMessage)
        event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER)
      case Right(data) =>
        temporaryState(event.getUniqueId) = data
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  final def onPlayerJoin(event: PlayerJoinEvent): Unit = {
    val player = event.getPlayer

    temporaryState.get(player.getUniqueId) match {
      case Some(temporaryData) =>
        state(player.getUniqueId) = initializeValue(player, temporaryData).runSync[SyncIO].unsafeRunSync()

      case None =>
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

  /**
   * プレーヤーの退出処理。この作用は2度以上呼び出した場合の動作が未定義である。
   */
  final def finalizationAction(key: Player): SyncContext[Unit] = {
    import cats.implicits._

    Sync[SyncContext].delay {
      state.remove(key.getUniqueId).get
    }.flatMap(unloadData(key, _))
  }
}
