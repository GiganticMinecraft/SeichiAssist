package com.github.unchama.contextualexecutor.builder

import com.github.unchama.contextualexecutor.builder.TypeAliases.{CommandArgumentsParser, ScopedContextualExecution, SenderTypeValidation, SingleArgumentParser}
import com.github.unchama.contextualexecutor.executors.PrintUsageExecutor
import com.github.unchama.contextualexecutor.{ContextualExecutor, ParsedArgCommandContext, PartiallyParsedArgs, RawCommandContext}
import com.github.unchama.targetedeffect.{EmptyEffect, TargetedEffect}
import org.bukkit.command.CommandSender
/**
 * [ContextualExecutor]を作成するためのビルダークラス.
 *
 * 各引数はビルドされる[ContextualExecutor]において異常系を見つけるとすぐに[RawCommandContext.sender]に応答を送り返す.
 * この副作用を内包させるためにsuspending functionとして宣言されている.
 *
 * @param CS 生成するExecutorが受け付ける[CommandSender]のサブタイプの上限
 * @param senderTypeValidation [CommandSender]を[CS]にダウンキャストするようなSuspending Function
 * @param argumentsParser [RawCommandContext]から[PartiallyParsedArgs]を作成するSuspending Function
 * @param contextualExecution [ParsedArgCommandContext]に基づいてコマンドのアクションを実行するSuspending Function
 */
case class ContextualExecutorBuilder[CS  <: CommandSender](
    val senderTypeValidation: SenderTypeValidation[CS],
    val argumentsParser: CommandArgumentsParser[CS],
    val contextualExecution: ScopedContextualExecution[CS]) {

  /**
   * @param parsers i番目にi番目の引数の変換を試みるような関数が入ったリスト
   * @param onMissingArguments 引数がパーサに対して不足しているときに[RawCommandContext]を用いて実行する[ContextualExecutor]
   *
   * @return [argumentsParser]に, [parsers]と[onMissingArguments]が組み合わされた関数が入った新しい[ContextualExecutorBuilder].
   */
  def argumentsParsers(parsers: List[SingleArgumentParser],
                       onMissingArguments: ContextualExecutor = PrintUsageExecutor): ContextualExecutorBuilder[CS] = {
    val combinedParser: CommandArgumentsParser[CS] = { case (refinedSender, context: RawCommandContext) =>
      def parse[CS <: CommandSender](args: List[String], refinedSender: CS, reverseAccumulator: List[Any] = List())
        : Option[Pair[List[Any], List[String]]] = {
        val firstParser = parsers.firstOrNull() ?: return Some(reverseAccumulator.reversed() to args)
        val firstArg = args.firstOrNull() ?: return None.also { onMissingArguments.executeWith(context) }

        return when (val transformed = firstParser(firstArg)) {
          is Either.Left => None.also { transformed.a.runFor(refinedSender) }
          is Either.Right => {
            val parsedArg = transformed.b
            parse(parsers.drop(1), args.drop(1), onMissingArguments, context, refinedSender, reverseAccumulator.plus(parsedArg))
          }
        }
      }

      parse(context.args, refinedSender)
          .map { case (parsed, nonParsed) => PartiallyParsedArgs(parsed, nonParsed) }
    }

    return this.copy(argumentsParser = combinedParser)
  }

  /**
   * [contextualExecution]に[execution]に相当する関数が入った新しい[ContextualExecutorBuilder]を作成する.
   *
   * [ContextualExecutor]の制約にあるとおり, [execution]は任意スレッドからの呼び出しに対応しなければならない.
   */
  def execution(execution: ScopedContextualExecution[CS]): ContextualExecutorBuilder[CS] =
      this.copy(contextualExecution = execution)

  /**
   * @return [CS]を[CS1]へ狭めるキャストを試み,
   * 失敗したら[errorMessageOnFail]が返るような[senderTypeValidation]が入った
   * 新しい[ContextualExecutorBuilder]
   */
  inline def refineSender[reified CS1  <: CS](errorMessageOnFail: TargetedEffect[CS]): ContextualExecutorBuilder[CS1] = {
    val newSenderTypeValidation: SenderTypeValidation[CS1] = { sender =>
      senderTypeValidation(sender).flatMap { refined1 =>
        if (refined1 is CS1) {
          refined1.some()
        } else {
          errorMessageOnFail.runFor(refined1)
          None
        }
      }
    }

    return ContextualExecutorBuilder(newSenderTypeValidation, argumentsParser, contextualExecution)
  }

  /**
   * @return [CS]を[CS1]へ狭めるキャストを試み,
   * 失敗すると[message]がエラーメッセージとして返る[senderTypeValidation]が入った
   * 新しい[ContextualExecutorBuilder]
   */
  inline def refineSenderWithError[reified CS1  <: CS](message: String): ContextualExecutorBuilder[CS1] =
      refineSender(message.asMessageEffect())

  /**
   * @return [CS]を[CS1]へ狭めるキャストを試み,
   * 失敗すると[messages]がエラーメッセージとして返る[senderTypeValidation]が入った
   * 新しい[ContextualExecutorBuilder]
   */
  inline def refineSenderWithError[reified CS1  <: CS](messages: List[String]): ContextualExecutorBuilder[CS1] =
      refineSender(messages.asMessageEffect())

  /**
   * ビルダーに入っている情報から[ContextualExecutor]を生成する.
   *
   * 生成された[ContextualExecutor]は,
   *
   *  - 先ず, 送信者が[CS]であるかを確認する
   *  - 次に, 引数のパースを試みる
   *  - 最後に, 変換された引数を用いて[ParsedArgCommandContext]を作成し,
   *    それを用いて[contextualExecution]で指定される動作を行う
   *
   * 処理を[ContextualExecutor.executeWith]内で行う.
   */
  def build(): ContextualExecutor = object : ContextualExecutor {
    override @SuspendingMethod def executeWith(rawContext: RawCommandContext) {
      senderTypeValidation(rawContext.sender)
          .flatMap { refinedSender =>
            argumentsParser(refinedSender, rawContext).map { parsedArgs =>
              ParsedArgCommandContext(refinedSender, rawContext.command, parsedArgs)
            }
          }
          .map { context => contextualExecution(context).runFor(context.sender) }
    }
  }
}

object ContextualExecutorBuilder {
  private val defaultArgumentParser: CommandArgumentsParser[CommandSender] = { _, context =>
    Some(PartiallyParsedArgs(List(), context.args))
  }
  private val defaultExecution: ScopedContextualExecution[CommandSender] = { EmptyEffect }
  private val defaultSenderValidation: SenderTypeValidation[CommandSender] = { sender: CommandSender => Some(sender) }

  def beginConfiguration() = ContextualExecutorBuilder(defaultSenderValidation, defaultArgumentParser, defaultExecution)
}
