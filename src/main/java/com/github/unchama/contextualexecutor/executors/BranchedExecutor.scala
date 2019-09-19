package com.github.unchama.contextualexecutor.executors

import com.github.unchama.contextualexecutor.{ContextualExecutor, RawCommandContext}

/**
 * コマンドの枝分かれでのルーティングを静的に行うアクションを返す[ContextualExecutor]
 */
case class BranchedExecutor(val branches: Map[String, ContextualExecutor],
                            val whenArgInsufficient: ContextualExecutor = PrintUsageExecutor,
                            val whenBranchNotFound: ContextualExecutor = PrintUsageExecutor) extends ContextualExecutor {

  import com.github.unchama.util.syntax.Nullability._

  override def executeWith(rawContext: RawCommandContext) {
    val firstArg = rawContext.args.firstOrNull()
      .ifNull { return whenArgInsufficient.ifNotNull(_.executeWith(rawContext)).ifNull(Unit) }

    val branch = branches.getOrElse(firstArg, return whenBranchNotFound.ifNotNull(_.executeWith(rawContext)).ifNull(Unit))

    val argShiftedContext = rawContext.copy(args = rawContext.args.drop(1))

    branch.executeWith(argShiftedContext)
  }

  override def tabCandidatesFor(context: RawCommandContext): List[String] = {
    val args = context.args

    if (args.size <= 1) return branches.keys.sorted()

    val childExecutor = branches[args.first()] ?: whenBranchNotFound ?: return null

    return childExecutor.tabCandidatesFor(context.copy(args = args.drop(1)))
  }

}