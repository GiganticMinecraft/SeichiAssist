package com.github.unchama.seichiassist.subsystems.breakcount

import cats.Monad
import cats.effect.{ConcurrentEffect, SyncEffect}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.fs2.workaround.fs3.Fs3Topic
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.application.BreakCountRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.breakcount.application.actions.{
  ClassifyPlayerWorld,
  IncrementSeichiExp
}
import com.github.unchama.seichiassist.subsystems.breakcount.bukkit.actions.SyncClassifyBukkitPlayerWorld
import com.github.unchama.seichiassist.subsystems.breakcount.domain.{
  SeichiAmountData,
  SeichiAmountDataPersistence
}
import com.github.unchama.seichiassist.subsystems.breakcount.infrastructure.JdbcSeichiAmountDataPersistence
import com.github.unchama.seichiassist.subsystems.discordnotification.DiscordNotificationAPI
import io.chrisdavenport.log4cats.ErrorLogger
import org.bukkit.entity.Player

import java.util.UUID

/**
 * 整地量データを管理するシステム。 このシステムは次の責務を持つ。
 *
 *   - 整地量データを永続化する
 *   - 整地量データの読み取りとインクリメント操作を他システムへ露出する
 *   - 整地量データの変更を他システムやプレーヤーへ通知する
 */
trait System[F[_], G[_]] extends Subsystem[F] {

  val api: BreakCountAPI[F, G, Player]

}

object System {

  import cats.effect.implicits._
  import cats.implicits._

  def wired[F[
    _
  ]: ConcurrentEffect: OnMinecraftServerThread: ErrorLogger: DiscordNotificationAPI, G[
    _
  ]: SyncEffect: ContextCoercion[*[_], F]](
  ): F[System[F, G]] = {
    implicit val persistence: SeichiAmountDataPersistence[G] =
      new JdbcSeichiAmountDataPersistence[G]

    val createSystem: F[System[F, G]] = for {
      breakCountTopic <- Fs3Topic[F, Option[(Player, SeichiAmountData)]]
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
          BukkitRepositoryControls.createHandles(
            BreakCountRepositoryDefinition
              .withContext[F, G, Player](breakCountTopic, persistence)
          )
        )
    } yield {
      implicit val classifyPlayerWorld: ClassifyPlayerWorld[G, Player] =
        SyncClassifyBukkitPlayerWorld[G]

      val breakCountRepository = breakCountRepositoryControls.repository

      new System[F, G] {
        override val api: BreakCountAPI[F, G, Player] = new BreakCountAPI[F, G, Player] {
          override protected implicit val _GMonad: Monad[G] = implicitly[SyncEffect[G]]
          override val seichiAmountDataRepository
            : KeyedDataRepository[Player, ReadOnlyRef[G, SeichiAmountData]] =
            breakCountRepository.map(ReadOnlyRef.fromRef)
          override val persistedSeichiAmountDataRepository
            : UUID => ReadOnlyRef[G, Option[SeichiAmountData]] =
            uuid =>
              ReadOnlyRef.fromAnySource {
                persistence.read(uuid)
              }
          override val incrementSeichiExp: IncrementSeichiExp[G, Player] =
            IncrementSeichiExp.using(breakCountRepository, breakCountTopic)
          override val seichiAmountUpdates: fs2.Stream[F, (Player, SeichiAmountData)] =
            breakCountTopic.subscribe(1).mapFilter(identity)
        }
        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] = Seq(
          breakCountRepositoryControls.coerceFinalizationContextTo[F]
        )
      }
    }

    createSystem.flatTap { system =>
      subsystems.notification.System.backgroundProcess(system.api).start
    }
  }

}
