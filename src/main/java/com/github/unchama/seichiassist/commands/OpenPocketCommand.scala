package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.targetedeffect.EmptyEffect
import kotlin.Suppress
import org.bukkit.Bukkit
import org.bukkit.ChatColor._

object OpenPocketCommand {
  private val descriptionPrintExecutor = EchoExecutor(List(
    s"${RED}/openpocket [プレイヤー名]",
      "対象プレイヤーの四次元ポケットを開きます。",
      "編集結果はオンラインのプレイヤーにのみ反映されます。"
  ).asMessageEffect())

  val executor = playerCommandBuilder
      .argumentsParsers(List(Parsers.identity), onMissingArguments = descriptionPrintExecutor)
      .execution { context =>
        val playerName = context.args.parsed[0].asInstanceOf[String]
        val player = Bukkit.getPlayer(playerName)

        if (player != null) {
          val playerData = SeichiAssist.playermap(player.uniqueId)
          val targetInventory = playerData.pocketInventory

          context.sender.openInventory(targetInventory)
          EmptyEffect
        } else {
          @Suppress("DEPRECATION") val targetPlayerUuid = Bukkit.getOfflinePlayer(playerName)?.uniqueId
          ?: return@execution s"${RED}プレーヤー $playerName のuuidを取得できませんでした。".asMessageEffect()

          SeichiAssist.databaseGateway.playerDataManipulator
              .selectPocketInventoryOf(targetPlayerUuid)
              .map { inventory =>
                context.sender.openInventory(inventory)

                s"${RED}対象プレイヤーはオフラインです。編集結果は反映されません。".asMessageEffect()
              }
              .merge()
        }
      }
      .build()
      .asNonBlockingTabExecutor()
}