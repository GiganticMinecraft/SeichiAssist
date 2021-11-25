package com.github.unchama.seichiassist.subsystems.anywhereender.bukkit.command

import cats.arrow.FunctionK
import cats.effect.{Effect, IO}
import cats.effect.implicits._
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.anywhereender.AnywhereEnderChestAPI
import org.bukkit.command.TabExecutor

/**
 * エンダーチェストを開くコマンド
 */
object EnderChestCommand {
  def executor[F[_]: Effect](implicit enderChestAccessApi: AnywhereEnderChestAPI[F]): TabExecutor =
    playerCommandBuilder
      .argumentsParsers(List())
      .execution { _ =>
        IO {
          enderChestAccessApi.openEnderChestOrNotifyInsufficientLevel.mapK(new FunctionK[F, IO] {
            override def apply[A](fa: F[A]): IO[A] = fa.toIO
          })
        }
      }
      .build()
      .asNonBlockingTabExecutor()
}
