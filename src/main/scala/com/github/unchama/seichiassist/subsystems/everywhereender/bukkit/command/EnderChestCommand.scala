package com.github.unchama.seichiassist.subsystems.everywhereender.bukkit.command

import cats.effect.{IO, SyncIO}
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiLevel
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.PlayerEffects
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

/**
 * エンダーチェストを開くコマンド
 */
object EnderChestCommand {
  def executor(implicit breakAmountApi: BreakCountReadAPI[IO, SyncIO, Player]): TabExecutor =
    playerCommandBuilder
      .argumentsParsers(List())
      .execution { context =>
        val sender = context.sender

        import cats.implicits._

        for {
          ref <- breakAmountApi.seichiAmountDataRepository(sender).read.toIO
          level = ref.levelCorrespondingToExp
        } yield {
          if (level >= SeichiLevel(25)) {
            // エンダーチェストは参照透明ではない
            PlayerEffects.openInventoryEffect(sender.getEnderChest)
          } else {
            MessageEffect("整地レベルが25に達していないため、このコマンドは使えません。")
          }
        }
      }
      .build()
      .asNonBlockingTabExecutor()
}
