package com.github.unchama.datarepository.bukkit.player

import cats.Monad
import cats.effect.{Sync, SyncEffect, SyncIO}
import com.github.unchama.bungeesemaphoreresponder.domain.PlayerDataFinalizer
import com.github.unchama.datarepository.template.{PrefetchResult, RepositoryFinalization, SinglePhasedRepositoryInitialization, TwoPhasedRepositoryInitialization}
import org.bukkit.entity.Player
import org.bukkit.event.player.{AsyncPlayerPreLoginEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, EventPriority, Listener}

import java.util.UUID
import scala.collection.concurrent.TrieMap

case class BukkitRepositoryControls[F[_], R](repository: PlayerDataRepository[R],
                                             initializer: Listener,
                                             backupProcess: F[Unit],
                                             finalizer: PlayerDataFinalizer[F, Player])

object BukkitRepositoryControls {

  import cats.effect.implicits._
  import cats.implicits._
  import org.bukkit.entity.Player
  import org.bukkit.event.Listener

  trait PreLoginListener extends Listener {
    def onPlayerPreLogin(event: AsyncPlayerPreLoginEvent): Unit
  }

  object Initializers {
    def singlePhased[
      F[_] : SyncEffect, R
    ](initialization: SinglePhasedRepositoryInitialization[F, R])
     (tapOnJoin: (Player, R) => F[Unit])
     (dataMap: TrieMap[UUID, R]): PreLoginListener = {

      //noinspection ScalaUnusedSymbol
      new PreLoginListener {
        @EventHandler(priority = EventPriority.LOWEST)
        override final def onPlayerPreLogin(event: AsyncPlayerPreLoginEvent): Unit = {
          initialization.prepareData(event.getUniqueId, event.getName)
            .runSync[SyncIO]
            .unsafeRunSync() match {
            case PrefetchResult.Failed(errorMessageOption) =>
              errorMessageOption.foreach(event.setKickMessage)
              event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER)
            case PrefetchResult.Success(data) =>
              dataMap(event.getUniqueId) = data
          }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        final def onPlayerJoin(event: PlayerJoinEvent): Unit = {
          val player = event.getPlayer

          tapOnJoin(player, dataMap(player.getUniqueId))
            .runSync[SyncIO]
            .unsafeRunSync()
        }
      }
    }

    def twoPhased[
      F[_] : SyncEffect, R
    ](initialization: TwoPhasedRepositoryInitialization[F, Player, R])
     (temporaryDataMap: TrieMap[UUID, initialization.IntermediateData], dataMap: TrieMap[Player, R]): Listener = {

      import cats.implicits._

      new Listener {
        //noinspection ScalaUnusedSymbol
        @EventHandler(priority = EventPriority.LOWEST)
        final def onPlayerPreLogin(event: AsyncPlayerPreLoginEvent): Unit = {
          initialization
            .prefetchIntermediateValue(event.getUniqueId, event.getName)
            .attempt
            .runSync[SyncIO]
            .unsafeRunSync() match {
            case Left(error) =>
              event.setKickMessage("初期化処理中にエラーが発生しました。")
              event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER)
              error.printStackTrace()
            case Right(PrefetchResult.Failed(errorMessageOption)) =>
              errorMessageOption.foreach(event.setKickMessage)
              event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER)
            case Right(PrefetchResult.Success(data)) =>
              temporaryDataMap(event.getUniqueId) = data
          }
        }

        //noinspection ScalaUnusedSymbol
        @EventHandler(priority = EventPriority.LOWEST)
        final def onPlayerJoin(event: PlayerJoinEvent): Unit = {
          val player = event.getPlayer

          temporaryDataMap.get(player.getUniqueId) match {
            case Some(temporaryData) =>
              dataMap(player) =
                initialization.prepareData(player, temporaryData)
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

  object Finalizers {
    def singlePhased[F[_] : Sync, Key, R, T](finalization: RepositoryFinalization[F, Key, R])
                                            (dataMap: TrieMap[Key, R]): PlayerDataFinalizer[F, Key] = {
      player =>
        for {
          finalData <- Sync[F].delay {
            dataMap.remove(player).get
          }
          _ <- finalization.persistPair(player, finalData)
          _ <- finalization.finalizeBeforeUnload(player, finalData)
        } yield ()
    }

    def twoPhased[F[_] : Sync, R, T](finalization: RepositoryFinalization[F, Player, R])
                                    (temporaryDataMap: TrieMap[UUID, T],
                                     dataMap: TrieMap[Player, R]): PlayerDataFinalizer[F, Player] = {
      player =>
        for {
          _ <- Sync[F].delay {
            temporaryDataMap.remove(player.getUniqueId)
          }
          _ <- singlePhased(finalization)(dataMap).onQuitOf(player)
        } yield ()
    }
  }

  def backupProcess[F[_] : Sync, Key, R](finalization: RepositoryFinalization[F, Key, R])
                                        (dataMap: TrieMap[Key, R]): F[Unit] = {
    Sync[F].suspend {
      dataMap.toList.traverse(finalization.persistPair.tupled).as(())
    }
  }

  def createTappingSinglePhasedRepositoryAndHandles[
    F[_] : SyncEffect, R
  ](initialization: SinglePhasedRepositoryInitialization[F, R],
    onPlayerJoin: (Player, R) => F[Unit],
    finalization: RepositoryFinalization[F, UUID, R]): F[BukkitRepositoryControls[F, R]] = Sync[F].delay {

    val dataMap: TrieMap[UUID, R] = TrieMap.empty

    // workaround of https://youtrack.jetbrains.com/issue/SCL-18638
    val i: initialization.type = initialization

    BukkitRepositoryControls(
      player => dataMap(player.getUniqueId),
      Initializers.singlePhased(i)(onPlayerJoin)(dataMap),
      backupProcess(finalization)(dataMap),
      player => Finalizers.singlePhased(finalization)(dataMap).onQuitOf(player.getUniqueId)
    )
  }

  def createSinglePhasedRepositoryAndHandles[
    F[_] : SyncEffect, R
  ](initialization: SinglePhasedRepositoryInitialization[F, R],
    finalization: RepositoryFinalization[F, UUID, R]): F[BukkitRepositoryControls[F, R]] = Sync[F].delay {

    val dataMap: TrieMap[UUID, R] = TrieMap.empty

    // workaround of https://youtrack.jetbrains.com/issue/SCL-18638
    val i: initialization.type = initialization

    BukkitRepositoryControls(
      player => dataMap(player.getUniqueId),
      Initializers.singlePhased(i)((_, _) => Monad[F].unit)(dataMap),
      backupProcess(finalization)(dataMap),
      player => Finalizers.singlePhased(finalization)(dataMap).onQuitOf(player.getUniqueId)
    )
  }

  def createTwoPhasedRepositoryAndHandles[
    F[_] : SyncEffect, R
  ](initialization: TwoPhasedRepositoryInitialization[F, Player, R],
    finalization: RepositoryFinalization[F, Player, R]): F[BukkitRepositoryControls[F, R]] = Sync[F].delay {

    val dataMap: TrieMap[Player, R] = TrieMap.empty
    val temporaryDataMap: TrieMap[UUID, initialization.IntermediateData] = TrieMap.empty

    // workaround of https://youtrack.jetbrains.com/issue/SCL-18638
    val i: initialization.type = initialization

    BukkitRepositoryControls(
      player => dataMap(player),
      Initializers.twoPhased(i)(temporaryDataMap, dataMap),
      backupProcess(finalization)(dataMap),
      Finalizers.twoPhased(finalization)(temporaryDataMap, dataMap)
    )
  }
}
