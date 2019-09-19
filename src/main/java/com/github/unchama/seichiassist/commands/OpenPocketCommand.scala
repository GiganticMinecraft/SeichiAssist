package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.targetedeffect.EmptyEffect
import org.bukkit.Bukkit

object OpenPocketCommand {
  private val descriptionPrintExecutor = EchoExecutor(listOf(
      "${ChatColor.RED}/openpocket <プレイヤー名>",
      "対象プレイヤーの四次元ポケットを開きます。",
      "編集結果はオンラインのプレイヤーにのみ反映されます。"
  ).asMessageEffect())

  val executor = playerCommandBuilder
      .argumentsParsers(listOf(Parsers.identity), onMissingArguments = descriptionPrintExecutor)
      .execution { context ->
        val playerName = context.args.parsed[0] as String
        val player = Bukkit.getPlayer(playerName)

        if (player != null) {
          val playerData = SeichiAssist.playermap[player.uniqueId]!!
          val targetInventory = playerData.pocketInventory

          context.sender.openInventory(targetInventory)
          EmptyEffect
        } else {
          @Suppress("DEPRECATION") val targetPlayerUuid = Bukkit.getOfflinePlayer(playerName)?.uniqueId
              ?: return@execution "${ChatColor.RED}プレーヤー $playerName のuuidを取得できませんでした。".asMessageEffect()

          SeichiAssist.databaseGateway.playerDataManipulator
              .selectPocketInventoryOf(targetPlayerUuid)
              .map { inventory ->
                context.sender.openInventory(inventory)

                "${ChatColor.RED}対象プレイヤーはオフラインです。編集結果は反映されません。".asMessageEffect()
              }
              .merge()
        }
      }
      .build()
      .asNonBlockingTabExecutor()
}