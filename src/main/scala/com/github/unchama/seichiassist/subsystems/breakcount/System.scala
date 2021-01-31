package com.github.unchama.seichiassist.subsystems.breakcount

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, SyncEffect}
import com.github.unchama.bungeesemaphoreresponder.domain.PlayerDataFinalizer
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.minecraft.actions.MinecraftServerThreadShift
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.application.BreakCountRepositoryDefinitions
import com.github.unchama.seichiassist.subsystems.breakcount.application.actions.{ClassifyPlayerWorld, IncrementSeichiExp}
import com.github.unchama.seichiassist.subsystems.breakcount.bukkit.actions.SyncClassifyBukkitPlayerWorld
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

  import cats.effect.implicits._
  import cats.implicits._

  def wired[
    F[_] : ConcurrentEffect : MinecraftServerThreadShift,
    G[_] : SyncEffect : ContextCoercion[*[_], F]
  ](implicit effectEnvironment: EffectEnvironment): F[System[F, G]] = {
    implicit val persistence: SeichiAmountDataPersistence[G] = new JdbcSeichiAmountDataPersistence[G]

    val createSystem: F[System[F, G]] = for {
      breakCountTopic <- Topic[F, Option[(Player, SeichiAmountData)]](None)
      /*
       * NOTE:
       *
       * SignallingRepositoryInitializationを用いてプレーヤーで添え字付いたトピックを通知する場合、
       * 必然的にリポジトリがTwoPhasedになってしまい他システムの初期化に支障が出る。
       * TODO TwoPhased/SinglePhasedの違いにより支障が出ることは型により表明されるべき
       *
       * このシステムのAPIによると、Playerのインスタンスが取れてからのみ整地量の加算が行われる。
       * そこで、SinglePhasedなリポジトリを作り、加算が行われる際に明示的にトピックに通知する
       * (レポジトリに通知機構を埋め込まない)ような設計にした。
       */
      breakCountRepositoryControls <-
        ContextCoercion(
          BukkitRepositoryControls.createTappingSinglePhasedRepositoryAndHandles[G, Ref[G, SeichiAmountData]](
            BreakCountRepositoryDefinitions.initialization(persistence),
            BreakCountRepositoryDefinitions.tappingAction(breakCountTopic),
            BreakCountRepositoryDefinitions.finalization(persistence)
          )
        )
    } yield {
      implicit val classifyPlayerWorld: ClassifyPlayerWorld[G, Player] = SyncClassifyBukkitPlayerWorld[G]

      val breakCountRepository = breakCountRepositoryControls.repository

      new System[F, G] {
        override val api: BreakCountAPI[F, G, Player] = new BreakCountAPI[F, G, Player] {
          override val seichiAmountDataRepository: KeyedDataRepository[Player, ReadOnlyRef[G, SeichiAmountData]] =
            breakCountRepository.map(ReadOnlyRef.fromRef)
          override val incrementSeichiExp: IncrementSeichiExp[G, Player] =
            IncrementSeichiExp.using(breakCountRepository, breakCountTopic)
          override val seichiAmountUpdates: fs2.Stream[F, (Player, SeichiAmountData)] =
            breakCountTopic.subscribe(1).mapFilter(identity)
        }
        override val listeners: Seq[Listener] = Seq(
          breakCountRepositoryControls.initializer
        )
        override val managedFinalizers: Seq[PlayerDataFinalizer[F, Player]] = Seq(
          breakCountRepositoryControls.finalizer.coerceContextTo[F]
        )
        override val commands: Map[String, TabExecutor] = Map()
      }
    }

    createSystem.flatTap { system =>
      subsystems.notification.System
        .backgroundProcess(system.api)
        .start
    }
  }

}
