package com.github.unchama.contextualexecutor.executors

import cats.effect.IO
import com.github.unchama.contextualexecutor.{ContextualExecutor, RawCommandContext}

/**
 * コマンドの枝分かれでのルーティングを静的に行うアクションを返す[ContextualExecutor]
 */
case class BranchedExecutor(
  branches: Map[String, ContextualExecutor],
  whenArgInsufficient: Option[ContextualExecutor] = Some(PrintUsageExecutor),
  whenBranchNotFound: Option[ContextualExecutor] = Some(PrintUsageExecutor)
) extends ContextualExecutor {

  override def executionWith(rawContext: RawCommandContext): IO[Unit] = {
    def executeOptionally(executor: Option[ContextualExecutor]): IO[Unit] =
      executor match {
        case Some(executor) => executor.executionWith(rawContext)
        case None           => IO.pure(())
      }

    rawContext.args match {
      case argHead :: argTail =>
        branches.get(argHead) match {
          case Some(branch) => branch.executionWith(rawContext.copy(args = argTail))
          case None         => executeOptionally(whenBranchNotFound)
        }
      case Nil => executeOptionally(whenArgInsufficient)
    }
  }

  override def tabCandidatesFor(context: RawCommandContext): List[String] = {
    context.args match {
      case head :: tail =>
        branches
          .get(head)
          .fold(List.empty[String])(_.tabCandidatesFor(context.copy(args = tail)))
      case Nil => branches.keys.toArray.sorted.toList
    }
  }

}
