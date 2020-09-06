package com.github.unchama.datarepository.bukkit.player

import java.util.UUID

import cats.effect.{ConcurrentEffect, ContextShift, SyncEffect}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import org.bukkit.entity.Player
import org.bukkit.event.player.{AsyncPlayerPreLoginEvent, PlayerQuitEvent}
import org.bukkit.event.{EventPriority, Listener}

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
](implicit environment: EffectEnvironment)
  extends TwoPhasedPlayerDataRepository[AsyncContext, SyncContext, R, R] with Listener {

  protected val loadData: (String, UUID) => SyncContext[Either[Option[String], R]]

  protected final override val loadTemporaryData: (String, UUID) => SyncContext[Either[Option[String], R]] = loadData

  protected final override def initialValue(player: Player, temporaryData: R): R = temporaryData

  final override def apply(player: Player): R = temporaryState(player.getUniqueId)

}
