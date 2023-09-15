package com.github.unchama.contextualexecutor

import org.bukkit.command.{Command, CommandSender}
import shapeless.HList

/**
 * コマンドの実行時に使用された[Command]とエイリアスの情報
 */
case class ExecutedCommand(command: Command, aliasUsed: String)

/**
 * コマンドの実行に関わる一連の生パラメータの情報
 */
case class RawCommandContext(
  sender: CommandSender,
  command: ExecutedCommand,
  args: List[String]
)

/**
 * 変換されたコマンド引数の情報
 *
 * @param parsed
 *   コマンド引数のうち, パースされたもの.
 * @param yetToBeParsed
 *   コマンド引数のうち, [parsed]へと変換されていない文字列. このフィールドは可変長引数や末尾のオプショナルな引数のハンドリングなど、
 *   特殊な処理を行う際にのみ利用されるべきものであり、ほとんどのユースケースでは型安全性が保証されないこのフィールドよりも
 *   [[parsed]]を参照してすでにパースされた値を取り扱うことが好ましい。
 */
case class PartiallyParsedArgs[+Args <: HList](parsed: Args, yetToBeParsed: List[String])

/**
 * コマンドの実行時のコマンド引数や実行者などの情報を変換, 加工したデータ.
 *
 * @tparam CS
 *   [CommandSender]オブジェクトの型上限. [sender]は[CS]であることまでが保証されている.
 * @param command
 *   実行コマンドに関する情報
 * @param args
 *   引数情報
 */
case class ParsedArgCommandContext[+CS <: CommandSender, +Args <: HList](
  sender: CS,
  command: ExecutedCommand,
  args: PartiallyParsedArgs[Args]
)
