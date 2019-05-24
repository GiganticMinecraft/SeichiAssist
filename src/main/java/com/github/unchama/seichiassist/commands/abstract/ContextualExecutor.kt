package com.github.unchama.seichiassist.commands.abstract

import com.github.unchama.util.ActionStatus
import com.github.unchama.util.ActionStatus.Fail
import com.github.unchama.util.ActionStatus.Ok
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

/**
 * コマンド実行時に[TabExecutor]へ渡される情報をラップした[CommandExecutionContext]を用いて処理を行うオブジェクトへのinterface.
 */
interface ContextualExecutor {

    /**
     * [context] に基づいてコマンドが行うべき処理を発火する.
     *
     * @return 処理が「成功」扱いなら[Ok], そうでなければ[Fail].
     */
    fun executeWith(context: CommandExecutionContext): ActionStatus

    /**
     * [context] に基づいてTab補完の候補をListで返却する.
     */
    fun tabCandidatesFor(context: CommandExecutionContext): List<String>? = null

}

/**
 * この[ContextualExecutor]を[TabExecutor]オブジェクトへ変換する.
 */
fun ContextualExecutor.asTabExecutor(): TabExecutor {
    return object: TabExecutor {
        override fun onCommand(sender: CommandSender, command: Command, alias: String, args: Array<out String>): Boolean {
            val context = CommandExecutionContext(sender, ExecutedCommand(command, alias), args.toList())
            return executeWith(context) == Ok
        }

        override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String>? {
            val context = CommandExecutionContext(sender, ExecutedCommand(command, alias), args.toList())

            return tabCandidatesFor(context)
        }
    }
}

/**
 * コマンドの枝分かれでのルーティングを静的に行う[ContextualExecutor]
 */
data class BranchedExecutor(val branches: Map<String, ContextualExecutor>, val default: ContextualExecutor? = null): ContextualExecutor {

    override fun executeWith(context: CommandExecutionContext): ActionStatus {
        val firstArg = context.args.firstOrNull() ?: return Fail
        val branch = (branches[firstArg] ?: default) ?: return Fail

        val argShiftedContext = context.copy(args = context.args.drop(1))

        return branch.executeWith(argShiftedContext)
    }

    override fun tabCandidatesFor(context: CommandExecutionContext): List<String>? {
        val args = context.args

        if (args.size <= 1) return branches.keys.sorted()

        val childExecutor = (branches[args.first()] ?: default) ?: return null

        return childExecutor.tabCandidatesFor(context.copy(args = args.drop(1)))
    }

}
