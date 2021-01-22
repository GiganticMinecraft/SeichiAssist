package com.github.unchama.datarepository.bukkit.player

import cats.effect.{Sync, SyncEffect, SyncIO}
import com.github.unchama.bungeesemaphoreresponder.domain.PlayerDataFinalizer
import com.github.unchama.datarepository.template.{PrefetchResult, RepositoryFinalization, TwoPhasedRepositoryInitialization}
import org.bukkit.entity.Player
import org.bukkit.event.player.{AsyncPlayerPreLoginEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, EventPriority, Listener}

import java.util.UUID
import scala.collection.concurrent.TrieMap
import scala.collection.mutable

case class BukkitRepositoryControls[F[_], R](repository: PlayerDataRepository[R],
                                             initializer: Listener,
                                             backupProcess: F[Unit],
                                             finalizer: PlayerDataFinalizer[F, Player])

object BukkitRepositoryControls {

  import org.bukkit.entity.Player
  import org.bukkit.event.Listener

  def createRepositoryAndHandles[
    F[_] : SyncEffect, R
  ](initialization: TwoPhasedRepositoryInitialization[F, Player, R],
    finalization: RepositoryFinalization[F, Player, R]): F[BukkitRepositoryControls[F, R]] = Sync[F].delay {

    val dataMap: TrieMap[Player, R] = TrieMap.empty
    val temporaryDataMap: mutable.Map[UUID, initialization.IntermediateData] = TrieMap.empty

    val repository: PlayerDataRepository[R] = player => dataMap(player)

    import cats.effect.implicits._
    import cats.implicits._

    val initializer: Listener = new Listener {
      //noinspection ScalaUnusedSymbol
      @EventHandler(priority = EventPriority.LOWEST)
      final def onPlayerPreLogin(event: AsyncPlayerPreLoginEvent): Unit = {
        initialization.prefetchIntermediateValue(event.getName, event.getUniqueId)
          .runSync[SyncIO]
          .unsafeRunSync() match {
          case PrefetchResult.Failed(errorMessageOption) =>
            errorMessageOption.foreach(event.setKickMessage)
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER)
          case PrefetchResult.Success(data) =>
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
                 |
                 |エラー： $getClass の初期化に失敗しています。
                 |""".stripMargin

            player.kickPlayer(message)
        }
      }
    }

    val backupProcess: F[Unit] = Sync[F].suspend {
      dataMap.toList.traverse(finalization.persistPair.tupled).as(())
    }

    val finalizer: PlayerDataFinalizer[F, Player] = {
      import cats.implicits._

      player =>
        for {
          finalData <- Sync[F].delay {
            temporaryDataMap.remove(player.getUniqueId)
            dataMap.remove(player).get
          }
          _ <- finalization.persistPair(player, finalData)
          _ <- finalization.finalizeBeforeUnload(player, finalData)
        } yield ()
    }

    BukkitRepositoryControls(repository, initializer, backupProcess, finalizer)
  }
}
