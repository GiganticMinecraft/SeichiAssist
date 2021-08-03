package com.github.unchama.contextualexecutor.executors

import cats.effect.IO
import com.github.unchama.contextualexecutor.{ContextualExecutor, RawCommandContext}

case class TraverseExecutor(executors: List[ContextualExecutor]) extends ContextualExecutor {
  import cats.implicits._

  override def executeWith(commandContext: RawCommandContext): IO[Unit] = {
    executors.traverse(_.executeWith(commandContext)).void
  }
}
