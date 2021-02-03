package com.github.unchama.datarepository.bukkit.player

import cats.effect.SyncEffect
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import org.bukkit.entity.Player
import org.bukkit.event.player.{AsyncPlayerPreLoginEvent, PlayerQuitEvent}
import org.bukkit.event.{EventPriority, Listener}

import java.util.UUID

/**
 * プレーヤーに値を関連付けるオンメモリデータリポジトリのクラス。
 *
 * [[AsyncPlayerPreLoginEvent]] の [[EventPriority.LOW]] から、
 * [[PlayerQuitEvent]] の [[EventPriority.HIGHEST]] までのデータの取得を保証する。
 *
 * @tparam R プレーヤーに関連付けられるデータの型
 */
@deprecated("Move to BukkitRepositoryControls for compositionality")
abstract class PreLoginToQuitPlayerDataRepository[
  SyncContext[_] : SyncEffect,
  R
](implicit environment: EffectEnvironment)
  extends TwoPhasedPlayerDataRepository[SyncContext, R] with Listener {

  override protected type TemporaryData = R

  protected val loadData: (String, UUID) => SyncContext[Either[Option[String], R]]

  protected final override lazy val loadTemporaryData: (String, UUID) => SyncContext[Either[Option[String], R]] = loadData

  protected final override def initializeValue(player: Player, temporaryData: R): SyncContext[R] = {
    SyncEffect[SyncContext].pure(temporaryData)
  }

  final override def apply(player: Player): R = temporaryState(player.getUniqueId)

}
