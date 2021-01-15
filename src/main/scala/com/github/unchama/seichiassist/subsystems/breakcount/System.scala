package com.github.unchama.seichiassist.subsystems.breakcount

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, SyncEffect}
import com.github.unchama.bungeesemaphoreresponder.domain.PlayerDataFinalizer
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.Diff
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.application.actions.{ClassifyPlayerWorld, IncrementSeichiExp, NotifyLevelUp}
import com.github.unchama.seichiassist.subsystems.breakcount.bukkit.actions.{SyncBukkitNotifyLevelUp, SyncClassifyBukkitPlayerWorld}
import com.github.unchama.seichiassist.subsystems.breakcount.bukkit.datarepository.BukkitSeichiAmountDataRepository
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiLevel
import com.github.unchama.seichiassist.subsystems.breakcount.domain.{SeichiAmountData, SeichiAmountDataPersistence}
import com.github.unchama.seichiassist.subsystems.breakcount.infrastructure.JdbcSeichiAmountDataPersistence
import fs2.concurrent.Topic
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener

/**
 * 整地量データを管理するシステム。
 * このシステムは次の責務を持つ。
 *
 *  - 整地量データを永続化する
 *  - 整地量データの読み取りとインクリメント操作を他システムへ露出する
 *  - 整地量データの変更を他システムやプレーヤーへ通知する
 */
trait System[F[_], G[_]] extends Subsystem[F] {

  val api: BreakCountAPI[F, G, Player]

}

object System {

  import cats.implicits._

  def wired[
    F[_] : ConcurrentEffect,
    G[_] : SyncEffect
  ](implicit effectEnvironment: EffectEnvironment): F[System[F, G]] = {
    for {
      breakCountTopic <- Topic[F, Option[(Player, Diff[SeichiAmountData])]](None)
      levelTopic <- Topic[F, Option[(Player, Diff[SeichiLevel])]](None)
    } yield {
      implicit val classifyPlayerWorld: ClassifyPlayerWorld[G, Player] = SyncClassifyBukkitPlayerWorld[G]
      implicit val notifyLevelUp: NotifyLevelUp[G, Player] =
        SyncBukkitNotifyLevelUp[G].alsoNotifyTo(levelTopic)

      implicit val persistence: SeichiAmountDataPersistence[G] = new JdbcSeichiAmountDataPersistence[G]
      implicit val _breakCountRepository: KeyedDataRepository[Player, Ref[G, SeichiAmountData]] =
        new BukkitSeichiAmountDataRepository[G]()

      new System[F, G] {
        override val api: BreakCountAPI[F, G, Player] = new BreakCountAPI[F, G, Player] {
          override val breakCountRepository: KeyedDataRepository[Player, ReadOnlyRef[G, SeichiAmountData]] =
            _breakCountRepository.map(ReadOnlyRef.fromRef)
          override val incrementSeichiExp: IncrementSeichiExp[G, Player] =
            IncrementSeichiExp.using(_breakCountRepository, breakCountTopic)
          override val breakCountUpdates: fs2.Stream[F, (Player, Diff[SeichiAmountData])] =
            breakCountTopic.subscribe(1).mapFilter(identity)
          override val levelUpdates: fs2.Stream[F, (Player, Diff[SeichiLevel])] =
            levelTopic.subscribe(1).mapFilter(identity)
        }
        override val listeners: Seq[Listener] = Seq()
        override val managedFinalizers: Seq[PlayerDataFinalizer[F, Player]] = Seq()
        override val commands: Map[String, TabExecutor] = Map()
      }
    }
  }

}
