package com.github.unchama.seichiassist.subsystems.everywhereender.bukkit.command

import cats.effect.IO
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.everywhereender.EverywhereEnderChestAPI
import org.bukkit.command.TabExecutor

/**
 * エンダーチェストを開くコマンド
 */
object EnderChestCommand {
  def executor(implicit enderChestAccessApi: EverywhereEnderChestAPI[IO]): TabExecutor =
    playerCommandBuilder
      .argumentsParsers(List())
      .execution { context =>
        val sender = context.sender

        // * 参照透明ではない
        IO {
          enderChestAccessApi.openEnderChestOrError(sender)
        }
      }
      .build()
      .asNonBlockingTabExecutor()
}
