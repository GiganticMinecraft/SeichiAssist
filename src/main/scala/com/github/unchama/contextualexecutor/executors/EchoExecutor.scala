package com.github.unchama.contextualexecutor.executors

import cats.effect.IO
import com.github.unchama.contextualexecutor.{ContextualExecutor, RawCommandContext}
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.command.CommandSender

/**
 * 実行されたときに[effect]を送り返すだけの[ContextualExecutor].
 */
class EchoExecutor(private val effect: TargetedEffect[CommandSender]) extends ContextualExecutor {
  override def executeWith(rawContext: RawCommandContext): IO[Unit] = effect(rawContext.sender)
}