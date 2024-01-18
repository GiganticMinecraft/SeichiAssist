package com.github.unchama.datarepository.bukkit.player

import cats.effect.{Sync, SyncEffect, SyncIO}
import cats.{Monad, ~>}
import com.github.unchama.bungeesemaphoreresponder.domain.PlayerDataFinalizer
import com.github.unchama.datarepository.template._
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.{
  PrefetchResult,
  SinglePhasedRepositoryInitialization,
  TwoPhasedRepositoryInitialization
}
import com.github.unchama.generic.ContextCoercion
import org.bukkit.entity.Player
import org.bukkit.event.player.{AsyncPlayerPreLoginEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, EventPriority, Listener}

import java.util.UUID
import scala.collection.concurrent.TrieMap

case class BukkitRepositoryControls[F[_], R](
  repository: PlayerDataRepository[R],
  initializer: Listener,
  backupProcess: F[Unit],
  finalizer: PlayerDataFinalizer[F, Player]
) {

  def transformFinalizationContext[G[_]](trans: F ~> G): BukkitRepositoryControls[G, R] =
    BukkitRepositoryControls(
      repository,
      initializer,
      trans(backupProcess),
      finalizer.transformContext(trans)
    )

  import cats.implicits._

  def map[S](f: R => S): BukkitRepositoryControls[F, S] =
    BukkitRepositoryControls(repository.map(f), initializer, backupProcess, finalizer)

  def coerceFinalizationContextTo[G[_]: ContextCoercion[F, *[_]]]
    : BukkitRepositoryControls[G, R] =
    transformFinalizationContext(ContextCoercion.asFunctionK)
}

object BukkitRepositoryControls {

  import cats.effect.implicits._
  import cats.implicits._
  import org.bukkit.entity.Player
  import org.bukkit.event.Listener

  private trait PreLoginAndJoinListener extends Listener {
    def onPlayerPreLogin(event: AsyncPlayerPreLoginEvent): Unit
    def onPlayerJoin(event: PlayerJoinEvent): Unit
  }

  private object Initializers {
    def singlePhased[F[_]: SyncEffect, R](
      initialization: SinglePhasedRepositoryInitialization[F, R]
    )(tapOnJoin: (Player, R) => F[Unit])(dataMap: TrieMap[UUID, R]): PreLoginAndJoinListener = {
      // noinspection ScalaUnusedSymbol
      new PreLoginAndJoinListener {
        @EventHandler(priority = EventPriority.LOWEST)
        override def onPlayerPreLogin(event: AsyncPlayerPreLoginEvent): Unit = {
          initialization
            .prepareData(event.getUniqueId, event.getName)
            .runSync[SyncIO]
            .attempt
            .unsafeRunSync() match {
            case Left(error) =>
              // TODO use Logger
              error.printStackTrace()
              event.setKickMessage("初期化処理中にエラーが発生しました。")
              event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER)
            case Right(PrefetchResult.Failed(errorMessageOption)) =>
              errorMessageOption.foreach(event.setKickMessage)
              event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER)
            case Right(PrefetchResult.Success(data)) =>
              dataMap(event.getUniqueId) = data
          }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        override def onPlayerJoin(event: PlayerJoinEvent): Unit = {
          val player = event.getPlayer

          tapOnJoin(player, dataMap(player.getUniqueId)).runSync[SyncIO].unsafeRunSync()
        }
      }
    }

    def twoPhased[F[_]: SyncEffect, R](
      initialization: TwoPhasedRepositoryInitialization[F, Player, R]
    )(
      temporaryDataMap: TrieMap[UUID, initialization.IntermediateData],
      dataMap: TrieMap[Player, R]
    ): PreLoginAndJoinListener = {

      val temporaryDataMapInitializer =
        singlePhased(initialization.prefetchIntermediateValue(_, _))((_, _) => Monad[F].unit)(
          temporaryDataMap
        )

      new PreLoginAndJoinListener {
        // noinspection ScalaUnusedSymbol
        @EventHandler(priority = EventPriority.LOWEST)
        override def onPlayerPreLogin(event: AsyncPlayerPreLoginEvent): Unit =
          temporaryDataMapInitializer.onPlayerPreLogin(event)

        // noinspection ScalaUnusedSymbol
        @EventHandler(priority = EventPriority.LOWEST)
        override def onPlayerJoin(event: PlayerJoinEvent): Unit = {
          val player = event.getPlayer

          temporaryDataMap.get(player.getUniqueId) match {
            case Some(temporaryData) =>
              dataMap(player) = initialization
                .prepareData(player, temporaryData)
                .runSync[SyncIO]
                .unsafeRunSync()

            case None =>
              val message =
                s"""
                   |データの読み込みに失敗しました。
                   |再接続しても改善されない場合は、
                   |整地鯖公式Discordサーバーからお知らせ下さい。
                   |""".stripMargin

              player.kickPlayer(message)
          }
        }
      }
    }
  }

  private object Finalizers {
    def singlePhased[F[_]: Sync, Key, R, T](
      finalization: RepositoryFinalization[F, Key, R]
    )(dataMap: TrieMap[Key, R]): PlayerDataFinalizer[F, Key] = { player =>
      for {
        finalData <- Sync[F].delay {
          dataMap.remove(player).get
        }
        _ <- finalization.persistPair(player, finalData)
        _ <- finalization.finalizeBeforeUnload(player, finalData)
      } yield ()
    }

    def twoPhased[F[_]: Sync, R, T](finalization: RepositoryFinalization[F, Player, R])(
      temporaryDataMap: TrieMap[UUID, T],
      dataMap: TrieMap[Player, R]
    ): PlayerDataFinalizer[F, Player] = { player =>
      for {
        _ <- Sync[F].delay {
          temporaryDataMap.remove(player.getUniqueId)
        }
        _ <- singlePhased(finalization)(dataMap).onQuitOf(player)
      } yield ()
    }
  }

  private def backupProcess[F[_]: Sync, Key, R](
    finalization: RepositoryFinalization[F, Key, R]
  )(dataMap: TrieMap[Key, R]): F[Unit] = {
    Sync[F].defer {
      dataMap.toList.traverse(finalization.persistPair.tupled).as(())
    }
  }

  def createHandles[F[_]: SyncEffect, R](
    definition: RepositoryDefinition[F, Player, R]
  ): F[BukkitRepositoryControls[F, R]] = {
    import cats.implicits._

    definition match {
      case RepositoryDefinition
            .Phased
            .SinglePhased(initialization, tappingAction, finalization) =>
        Sync[F]
          .delay {
            TrieMap.empty[UUID, R]
          }
          .map { dataMap =>
            // workaround of https://youtrack.jetbrains.com/issue/SCL-18638
            val i: initialization.type = initialization

            BukkitRepositoryControls(
              PlayerDataRepository.unlift(player => dataMap.get(player.getUniqueId)),
              Initializers.singlePhased(i)(tappingAction)(dataMap),
              backupProcess(finalization)(dataMap),
              player =>
                Finalizers.singlePhased(finalization)(dataMap).onQuitOf(player.getUniqueId)
            )
          }

      case RepositoryDefinition.Phased.TwoPhased(initialization, finalization) =>
        Sync[F]
          .delay {
            (TrieMap.empty[Player, R], TrieMap.empty[UUID, initialization.IntermediateData])
          }
          .map {
            case (dataMap, temporaryDataMap) =>
              // workaround of https://youtrack.jetbrains.com/issue/SCL-18638
              val i: initialization.type = initialization

              BukkitRepositoryControls(
                PlayerDataRepository.unlift(player => dataMap.get(player)),
                Initializers.twoPhased(i)(temporaryDataMap, dataMap),
                backupProcess(finalization)(dataMap),
                Finalizers.twoPhased(finalization)(temporaryDataMap, dataMap)
              )
          }

      case rd: RepositoryDefinition.Mapped[F, Player, s, R] =>
        createHandles[F, s](rd.source).map(_.map(rd.sr))
    }
  }
}
