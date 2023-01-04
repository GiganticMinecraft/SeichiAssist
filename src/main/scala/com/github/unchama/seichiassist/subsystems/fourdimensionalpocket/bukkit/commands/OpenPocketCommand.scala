package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.bukkit.commands

import cats.data.Kleisli
import cats.effect.{Effect, IO, SyncIO}
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.contextualexecutor.executors.EchoExecutor
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.RefDict
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.actions.InteractInventory
import com.github.unchama.seichiassist.subsystems.itemmigration.domain.minecraft.UuidRepository
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.Bukkit
import org.bukkit.ChatColor.RED
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

import java.util.UUID

class OpenPocketCommand[F[_]: Effect: InteractInventory[*[_], Player, Inventory]](
  repository: KeyedDataRepository[Player, ReadOnlyRef[F, Inventory]],
  persistence: RefDict[F, UUID, Inventory]
)(implicit syncIOUuidRepository: UuidRepository[SyncIO]) {
  private val descriptionPrintExecutor =
    EchoExecutor(MessageEffect {
      List(s"$RED/openpocket [プレイヤー名]", "対象プレイヤーの四次元ポケットを開きます。", "編集結果はオンラインのプレイヤーにのみ反映されます。")
    })

  import cats.effect.implicits._
  import cats.implicits._

  val executor: TabExecutor = playerCommandBuilder
    .argumentsParsers(List(Parsers.identity), onMissingArguments = descriptionPrintExecutor)
    .execution { context =>
      val playerName = context.args.parsed.head.asInstanceOf[String]

      val computeEffect: IO[TargetedEffect[Player]] =
        for {
          player <- IO {
            Bukkit.getPlayer(playerName)
          }
          effect <-
            if (player != null) {
              repository(player)
                .read
                .map(inventory =>
                  Kleisli(InteractInventory.apply.open(inventory)).mapK(Effect.toIOK)
                )
                .toIO
            } else {
              IO {
                syncIOUuidRepository.getUuid(playerName).unsafeRunSync().orNull
              }.flatMap {
                case null =>
                  IO.pure {
                    MessageEffect(s"${RED}プレーヤー $playerName のuuidを取得できませんでした。")
                  }
                case targetPlayerUuid =>
                  persistence
                    .read(targetPlayerUuid)
                    .map {
                      case Some(inventory) =>
                        Kleisli(InteractInventory.apply.open(inventory)).mapK(Effect.toIOK)
                      case None =>
                        MessageEffect(s"${RED}プレーヤーのインベントリが見つかりませんでした")
                    }
                    .toIO
              }
            }
        } yield effect

      computeEffect
    }
    .build()
    .asNonBlockingTabExecutor()
}
