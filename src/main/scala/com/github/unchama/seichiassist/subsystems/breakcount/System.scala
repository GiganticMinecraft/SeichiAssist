package com.github.unchama.seichiassist.subsystems.breakcount

import cats.effect.Sync
import com.github.unchama.bungeesemaphoreresponder.domain.PlayerDataFinalizer
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.application.actions.{ClassifyPlayerWorld, IncrementSeichiExp, NotifyLevelUp}
import com.github.unchama.seichiassist.subsystems.breakcount.bukkit.actions.{SyncBukkitNotifyLevelUp, SyncClassifyBukkitPlayerWorld}
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
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

  def wired[
    F[_],
    G[_] : Sync
  ]: System[F, G] = {
    implicit val classifyPlayerWorld: ClassifyPlayerWorld[G, Player] = SyncClassifyBukkitPlayerWorld[G]
    implicit val notifyLevelUp: NotifyLevelUp[G, Player] = SyncBukkitNotifyLevelUp[G]

    new System[F, G] {
      override val api: BreakCountAPI[F, G, Player] = new BreakCountAPI[F, G, Player] {
        override val breakCountRepository: KeyedDataRepository[Player, ReadOnlyRef[G, SeichiAmountData]] = ???
        override val incrementSeichiExp: IncrementSeichiExp[G, Player] = IncrementSeichiExp.using(???)
        override val breakCountUpdates: fs2.Stream[F, (Player, SeichiAmountData)] = ???
      }
      override val listeners: Seq[Listener] = Seq()
      override val managedFinalizers: Seq[PlayerDataFinalizer[F, Player]] = Seq()
      override val commands: Map[String, TabExecutor] = Map()
    }
  }

}
