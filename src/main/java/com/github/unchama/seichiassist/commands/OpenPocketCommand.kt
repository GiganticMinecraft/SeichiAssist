package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.asNonBlockingTabExecutor
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.contextualexecutor.executors.EchoExecutor
import com.github.unchama.effect.EmptyMessage
import com.github.unchama.effect.asResponseToSender
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.util.data.merge
import org.bukkit.Bukkit
import org.bukkit.ChatColor

object OpenPocketCommand {
  private val descriptionPrintExecutor = EchoExecutor(listOf(
      "${ChatColor.RED}/openpocket <プレイヤー名>",
      "対象プレイヤーの四次元ポケットを開きます。",
      "編集結果はオンラインのプレイヤーにのみ反映されます。"
  ).asResponseToSender())

  val executor = playerCommandBuilder
      .argumentsParsers(listOf(Parsers.identity), onMissingArguments = descriptionPrintExecutor)
      .execution { context ->
        val playerName = context.args.parsed[0] as String
        val player = Bukkit.getPlayer(playerName)

        if (player != null) {
          val playerData = SeichiAssist.playermap[player.uniqueId]!!
          val targetInventory = playerData.inventory

          context.sender.openInventory(targetInventory)
          EmptyMessage
        } else {
          @Suppress("DEPRECATION") val targetPlayerUuid = Bukkit.getOfflinePlayer(playerName)?.uniqueId
              ?: return@execution "${ChatColor.RED}プレーヤー $playerName のuuidを取得できませんでした。".asResponseToSender()

          SeichiAssist.databaseGateway.playerDataManipulator
              .selectPocketInventoryOf(targetPlayerUuid)
              .map { inventory ->
                context.sender.openInventory(inventory)

                "${ChatColor.RED}対象プレイヤーはオフラインです。編集結果は反映されません。".asResponseToSender()
              }
              .merge()
        }
      }
      .build()
      .asNonBlockingTabExecutor()
}