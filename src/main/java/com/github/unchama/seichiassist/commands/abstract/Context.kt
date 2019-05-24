package com.github.unchama.seichiassist.commands.abstract

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

import arrow.core.Option
import arrow.core.None
import arrow.core.extensions.option.monad.binding
import arrow.syntax.collections.firstOption

data class ExecutedCommand(val command: Command, val aliasUsed: String)

/**
 * コマンドの実行に関わる一連のパラメータを纏めたデータ
 */
data class CommandExecutionContext(val sender: CommandSender,
                                   val command: ExecutedCommand,
                                   val args: List<String>)

/**
 * コマンドの実行時に, コマンド引数や実行者などの情報を変換、加工したデータ.
 *
 * @param CS [CommandSender]オブジェクトの型上限. [sender]は[CS]であることまでが保証されている.
 * @param parsedArgs コマンド引数のうち, [Any?]を型上限とするオブジェクトに変換されたもの.
 * @param argsYetToBeParsed コマンド引数のうち, [parsedArgs]へと変換されていない文字列.
 */
data class ParsedArgCommandContext<CS: CommandSender>(val sender: CS,
                                                      val command: ExecutedCommand,
                                                      val parsedArgs: List<Any?>,
                                                      val argsYetToBeParsed: List<String>) {

    /**
     * [sender]が[CS1]のインスタンスである場合, その情報を内包した[ParsedArgCommandContext]を, そうでなければnullを返す.
     */
    inline fun <reified CS1: CS> refineSender(): ParsedArgCommandContext<CS1>? =
            (sender as? CS1)?.let { ParsedArgCommandContext(it, command, parsedArgs, argsYetToBeParsed) }

    /**
     * [argsYetToBeParsed]の先頭にある文字列を[parser]で変換した値を[parsedArgs]に取る新しい[ParsedArgCommandContext]を計算する.
     * 引数が不足していたり, 変換に失敗していた場合[None]を返す.
     *
     * @param parser 変換に失敗したとき[None]を, そうでなければ成功値を含んだ[Option]を返す関数
     */
    fun <R> parseArgHead(parser: (String) -> Option<R>): Option<ParsedArgCommandContext<CS>> =
            binding {
                val (firstNonParsedArg) = argsYetToBeParsed.firstOption()
                val (parsedArg) = parser(firstNonParsedArg)

                this@ParsedArgCommandContext.copy(
                        parsedArgs = parsedArgs.plusElement(parsedArg),
                        argsYetToBeParsed = argsYetToBeParsed.drop(1)
                )
            }
}
