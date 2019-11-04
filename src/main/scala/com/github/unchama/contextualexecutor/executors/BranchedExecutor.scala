package com.github.unchama.contextualexecutor.executors

import cats.effect.IO
import com.github.unchama.contextualexecutor.{ContextualExecutor, RawCommandContext}

/**
 * コマンドの枝分かれでのルーティングを静的に行うアクションを返す[ContextualExecutor]
 */
case class BranchedExecutor(branches: Map[String, ContextualExecutor],
                            whenArgInsufficient: Option[ContextualExecutor] = Some(PrintUsageExecutor),
                            whenBranchNotFound: Option[ContextualExecutor] = Some(PrintUsageExecutor)) extends ContextualExecutor {

  override def executeWith(rawContext: RawCommandContext): IO[Unit] = {
    def executeOptionally(executor: Option[ContextualExecutor]): IO[Unit] =
      executor match {
        case Some(executor) => executor.executeWith(rawContext)
        case None => IO.pure(())
      }

    val (argHead, argTail) = rawContext.args match {
      case ::(head, tl) => (head, tl)
      case Nil => return executeOptionally(whenArgInsufficient)
    }

    val branch = branches.getOrElse(argHead, return executeOptionally(whenBranchNotFound))

    val argShiftedContext = rawContext.copy(args = argTail)

    branch.executeWith(argShiftedContext)
  }

  override def tabCandidatesFor(context: RawCommandContext): List[String] = {
    context.args match {
      case head :: tail =>
        val childExecutor = branches.getOrElse(head, return Nil)

        childExecutor.tabCandidatesFor(context.copy(args = tail))
      case Nil => branches.keys.toArray.sorted.toList
    }
  }

}