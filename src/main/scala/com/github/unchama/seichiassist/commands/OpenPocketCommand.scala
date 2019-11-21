package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.contextualexecutor.executors.EchoExecutor
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.targetedeffect.syntax._
import com.github.unchama.targetedeffect.{TargetedEffect, emptyEffect}
import org.bukkit.Bukkit
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

object OpenPocketCommand {
  private val descriptionPrintExecutor = new EchoExecutor(List(
    s"$RED/openpocket [プレイヤー名]",
    "対象プレイヤーの四次元ポケットを開きます。",
    "編集結果はオンラインのプレイヤーにのみ反映されます。"
  ).asMessageEffect())

  val executor: TabExecutor = playerCommandBuilder
    .argumentsParsers(List(Parsers.identity), onMissingArguments = descriptionPrintExecutor)
    .execution { context =>
      val playerName = context.args.parsed(0).asInstanceOf[String]
      val player = Bukkit.getPlayer(playerName)

      def execute(): TargetedEffect[Player] = {
        if (player != null) {
          val playerData = SeichiAssist.playermap(player.getUniqueId)
          val targetInventory = playerData.pocketInventory

          context.sender.openInventory(targetInventory)

          emptyEffect
        } else {
          val targetPlayerUuid = Bukkit.getOfflinePlayer(playerName).getUniqueId
          if (targetPlayerUuid == null) {
            s"${RED}プレーヤー $playerName のuuidを取得できませんでした。".asMessageEffect()
          }

          SeichiAssist.databaseGateway.playerDataManipulator
            .selectPocketInventoryOf(targetPlayerUuid)
            .map { result =>
              context.sender.openInventory(
                result.getOrElse(return s"${RED}プレーヤー $playerName のuuidを取得できませんでした。".asMessageEffect()))
            }

          emptyEffect
        }
      }

      IO(execute())
    }
    .build()
    .asNonBlockingTabExecutor()
}