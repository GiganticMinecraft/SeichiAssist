package com.github.unchama.seichiassist.commands.abstract

import arrow.core.None
import arrow.core.Option
import arrow.core.extensions.option.monad.binding
import arrow.core.toOption
import arrow.syntax.collections.firstOption
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

/**
 * コマンドの実行時に使用された[Command]とエイリアスの情報
 */
data class ExecutedCommand(val command: Command, val aliasUsed: String)

/**
 * コマンドの実行に関わる一連の生パラメータの情報
 */
data class RawCommandContext(val sender: CommandSender,
                             val command: ExecutedCommand,
                             val args: List<String>)

/**
 * 変換されたコマンド引数の情報
 *
 * @param parsedArgs コマンド引数のうち, [Any?]を型上限とするオブジェクトに変換されたもの.
 * @param argsYetToBeParsed コマンド引数のうち, [parsedArgs]へと変換されていない文字列.
 */
data class PartiallyParsedArgs(val parsedArgs: List<Any?>, val argsYetToBeParsed: List<String>) {
    /**
     * [argsYetToBeParsed]の先頭にある文字列を[parser]で変換した値を[parsedArgs]に取る新しい[ParsedArgCommandContext]を計算する.
     * 引数が不足していたり, 変換に失敗していた場合[None]を返す.
     *
     * @param parser 変換に失敗したとき[None]を, そうでなければ成功値を含んだ[Option]を返す関数
     */
    fun <R> parseArgHead(parser: (String) -> Option<R>): Option<PartiallyParsedArgs> =
            binding {
                val (firstNonParsedArg) = argsYetToBeParsed.firstOption()
                val (parsedArg) = parser(firstNonParsedArg)

                this@PartiallyParsedArgs.copy(
                        parsedArgs = parsedArgs.plusElement(parsedArg),
                        argsYetToBeParsed = argsYetToBeParsed.drop(1)
                )
            }
}

/**
 * コマンドの実行時のコマンド引数や実行者などの情報を変換, 加工したデータ.
 *
 * @param CS [CommandSender]オブジェクトの型上限. [sender]は[CS]であることまでが保証されている.
 * @param command 実行コマンドに関する情報
 * @param args 引数情報
 */
data class ParsedArgCommandContext<CS: CommandSender>(val sender: CS,
                                                      val command: ExecutedCommand,
                                                      val args: PartiallyParsedArgs) {

    /**
     * [sender]が[CS1]のインスタンスである場合, その情報を内包した[ParsedArgCommandContext]を, そうでなければnullを返す.
     */
    inline fun <reified CS1: CS> refineSender(): Option<ParsedArgCommandContext<CS1>> =
            (sender as? CS1).toOption().map { ParsedArgCommandContext(it, command, args) }

    /**
     * 失敗する可能性のある[transformation]を用いて[args]を変換し,
     * 成功値が[args]になった新しい[ParsedArgCommandContext]を返す.
     */
    inline fun transformArgs(transformation: (PartiallyParsedArgs) -> Option<PartiallyParsedArgs>) =
            transformation(args).map { this.copy(args = it) }

}
