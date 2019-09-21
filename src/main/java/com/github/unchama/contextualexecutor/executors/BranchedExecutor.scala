package com.github.unchama.contextualexecutor.executors

import com.github.unchama.contextualexecutor.{ContextualExecutor, RawCommandContext}
import com.github.unchama.util.kotlin2scala.SuspendingMethod

/**
 * コマンドの枝分かれでのルーティングを静的に行うアクションを返す[ContextualExecutor]
 */
case class BranchedExecutor(val branches: Map[String, ContextualExecutor],
                            val whenArgInsufficient: ContextualExecutor = PrintUsageExecutor,
                            val whenBranchNotFound: ContextualExecutor = PrintUsageExecutor) extends ContextualExecutor {

  import com.github.unchama.util.syntax.Nullability._

  override @SuspendingMethod def executeWith(rawContext: RawCommandContext) {
    val (argHead, argTail) = rawContext.args match {
      case ::(head, tl) => (head, tl)
      case Nil =>
        whenArgInsufficient.ifNotNull(_.executeWith(rawContext, cont))
        return
    }

    val branch = branches.getOrElse(argHead, return whenBranchNotFound.ifNotNull(_.executeWith(rawContext, cont)))

    val argShiftedContext = rawContext.copy(args = argTail)

    branch.executeWith(argShiftedContext, cont)
  }

  override def tabCandidatesFor(context: RawCommandContext): List[String] = {
    val args = context.args

    if (args.size <= 1) return branches.keys.toArray.sorted.toList

    val childExecutor = branches.getOrElse(args.head, whenBranchNotFound.ifNull { return null })

    childExecutor.tabCandidatesFor(context.copy(args = args.drop(1)))
  }

}