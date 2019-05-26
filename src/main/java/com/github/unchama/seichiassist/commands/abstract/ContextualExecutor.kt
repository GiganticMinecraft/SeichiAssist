package com.github.unchama.seichiassist.commands.abstract

import arrow.effects.IO
import arrow.effects.extensions.io.applicative.just
import arrow.effects.extensions.io.fx.fx
import arrow.effects.extensions.io.unsafeRun.runBlocking
import arrow.unsafe
import com.github.unchama.util.ActionStatus.Fail
import com.github.unchama.util.ActionStatus.Ok
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

/**
 * コマンド実行時に[TabExecutor]へ渡される情報をラップした[RawCommandContext]を用いて処理を行うオブジェクトへのinterface.
 */
interface ContextualExecutor {

    /**
     * [rawContext] に基づいて, コマンドが行うべき処理を発火する
     *
     * @return 処理が「成功」扱いなら[Ok], そうでなければ[Fail].
     */
    fun executionFor(rawContext: RawCommandContext): IO<Unit>

    /**
     * [context] に基づいてTab補完の候補をListで返却する.
     */
    fun tabCandidatesFor(context: RawCommandContext): List<String>? = null

}

/**
 * この[ContextualExecutor]を[TabExecutor]オブジェクトへ変換する.
 */
fun ContextualExecutor.asTabExecutor(): TabExecutor {
    return object: TabExecutor {
        override fun onCommand(sender: CommandSender, command: Command, alias: String, args: Array<out String>): Boolean {
            val context = RawCommandContext(sender, ExecutedCommand(command, alias), args.toList())
            val program =
                    fx {
                        continueOn(NonBlocking)
                        !effect {
                            executionFor(context)
                        }
                    }

            unsafe { runBlocking { program } }

            // コマンドは非同期の操作を含むことを前提とするため, Bukkitへのコマンドの成否を必ず成功扱いにする
            return true
        }

        override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String>? {
            val context = RawCommandContext(sender, ExecutedCommand(command, alias), args.toList())

            return tabCandidatesFor(context)
        }
    }
}

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
