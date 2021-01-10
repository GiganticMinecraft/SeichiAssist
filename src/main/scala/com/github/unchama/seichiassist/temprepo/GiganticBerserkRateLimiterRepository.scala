package com.github.unchama.seichiassist.temprepo

import cats.Applicative
import cats.implicits._
import cats.effect.{ConcurrentEffect, SyncEffect, Timer}
import com.github.unchama.datarepository.bukkit.player.TwoPhasedPlayerDataRepository
import com.github.unchama.generic.ratelimiting.{FixedWindowRateLimiter, RateLimiter}
import com.github.unchama.seichiassist.SeichiAssist
import org.bukkit.entity.Player

import java.util.UUID
import scala.concurrent.duration._

class GiganticBerserkRateLimiterRepository[
  F[_] : ConcurrentEffect : Timer,
  G[_] : SyncEffect
] extends TwoPhasedPlayerDataRepository[G, RateLimiter[G, GiganticBerserkRateLimitValue]] {
  /**
   * 中間データの型
   */
  override protected type TemporaryData = RateLimiter[G, GiganticBerserkRateLimitValue]
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
  override protected val loadTemporaryData: (String, UUID) => G[Either[Option[String], TemporaryData]] =
    (_, _) =>
      FixedWindowRateLimiter.in[F, G, GiganticBerserkRateLimitValue](
        GiganticBerserkRateLimitValue.ofNonNegative(SeichiAssist.seichiAssistConfig.getGiganticBerserkLimitRatePerMinute),
        1.minute
      ).map(Right(_))

  /**
   * [[Player]] がサーバーに参加し終えた時、レポジトリが持つべきデータを計算する。
   *
   * このメソッドは[[onPlayerJoin()]]により同期的に実行されるため、
   * ここで重い処理を行うとサーバーのパフォーマンスに悪影響を及ぼす。
   *
   * DBアクセス等の処理を行う必要がある場合 [[loadTemporaryData]] で[[TemporaryData]]をロードすることを検討せよ。
   */
  override protected def initializeValue(player: Player, temporaryData: TemporaryData): G[TemporaryData] =
    Applicative[G].pure(temporaryData)

  /**
   * 与えられたプレーヤーに紐づいたデータを、データリポジトリが削除する前に必要な終了処理を行う作用。
   */
  override protected val finalizeBeforeUnload: (Player, TemporaryData) => G[Unit] =
    (_, _) => Applicative[G].unit
}
