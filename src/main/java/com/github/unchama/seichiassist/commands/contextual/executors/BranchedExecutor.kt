package com.github.unchama.seichiassist.commands.contextual.executors

import arrow.effects.IO
import arrow.effects.extensions.io.applicative.just
import com.github.unchama.seichiassist.commands.contextual.ContextualExecutor
import com.github.unchama.seichiassist.commands.contextual.RawCommandContext

/**
 * コマンドの枝分かれでのルーティングを静的に行う[ContextualExecutor]
 */
data class BranchedExecutor(val branches: Map<String, ContextualExecutor>,
                            val whenArgInsufficient: ContextualExecutor? = PrintUsageExecutor,
                            val whenBranchNotFound: ContextualExecutor? = PrintUsageExecutor): ContextualExecutor {

    override fun executionFor(rawContext: RawCommandContext): IO<Unit> {
        val firstArg = rawContext.args.firstOrNull()
                ?: return whenArgInsufficient?.executionFor(rawContext) ?: Unit.just()

        val branch = branches[firstArg]
                ?: return whenBranchNotFound?.executionFor(rawContext) ?: Unit.just()

        val argShiftedContext = rawContext.copy(args = rawContext.args.drop(1))

        return branch.executionFor(argShiftedContext)
    }

    override fun tabCandidatesFor(context: RawCommandContext): List<String>? {
        val args = context.args

        if (args.size <= 1) return branches.keys.sorted()

        val childExecutor = branches[args.first()] ?: whenBranchNotFound ?: return null

        return childExecutor.tabCandidatesFor(context.copy(args = args.drop(1)))
    }

}