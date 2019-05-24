package com.github.unchama.seichiassist.commands.abstract

import arrow.core.Either
import arrow.core.Left
import arrow.core.None
import arrow.core.Option
import com.github.unchama.util.ActionStatus.Fail
import com.github.unchama.util.ActionStatus.Ok
import com.github.unchama.util.merge
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

typealias ResponseToSender = String
typealias ExecutionResult = Either<Option<ResponseToSender>, Option<ResponseToSender>>

/**
 * コマンド実行時に[TabExecutor]へ渡される情報をラップした[RawCommandContext]を用いて処理を行うオブジェクトへのinterface.
 */
interface ContextualExecutor {

    /**
     * [rawContext] に基づいてコマンドが行うべき処理を発火する.
     *
     * @return 処理が「成功」扱いなら[Ok], そうでなければ[Fail].
     */
    fun executeWith(rawContext: RawCommandContext): ExecutionResult

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
            val result = executeWith(context)

            result.merge().map { response -> sender.sendMessage(response) }

            // 成功もせずエラーメッセージも得られなかった場合、コマンドそのものを失敗扱いとする(Bukkitの仕様によりusageが表示される)
            return result != Left(None)
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
data class BranchedExecutor(val branches: Map<String, ContextualExecutor>, val default: ContextualExecutor? = null): ContextualExecutor {

    override fun executeWith(rawContext: RawCommandContext): ExecutionResult {
        // TODO look for default branch if first argument is not found
        val firstArg = rawContext.args.firstOrNull() ?: return Left(None)
        val branch = (branches[firstArg] ?: default) ?: return Left(None)

        val argShiftedContext = rawContext.copy(args = rawContext.args.drop(1))

        return branch.executeWith(argShiftedContext)
    }

    override fun tabCandidatesFor(context: RawCommandContext): List<String>? {
        val args = context.args

        if (args.size <= 1) return branches.keys.sorted()

        val childExecutor = (branches[args.first()] ?: default) ?: return null

        return childExecutor.tabCandidatesFor(context.copy(args = args.drop(1)))
    }

}
