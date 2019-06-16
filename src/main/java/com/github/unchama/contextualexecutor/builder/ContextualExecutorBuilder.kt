package com.github.unchama.contextualexecutor.builder

import arrow.core.*
import com.github.unchama.contextualexecutor.ContextualExecutor
import com.github.unchama.contextualexecutor.ParsedArgCommandContext
import com.github.unchama.contextualexecutor.PartiallyParsedArgs
import com.github.unchama.contextualexecutor.RawCommandContext
import com.github.unchama.contextualexecutor.executors.PrintUsageExecutor
import com.github.unchama.effect.EmptyEffect
import com.github.unchama.effect.TargetedEffect
import com.github.unchama.effect.asMessageEffect
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
data class ContextualExecutorBuilder<CS : CommandSender>(
    val senderTypeValidation: SenderTypeValidation<CS>,
    val argumentsParser: CommandArgumentsParser<CS>,
    val contextualExecution: ScopedContextualExecution<CS>) {

  /**
   * @param parsers i番目にi番目の引数の変換を試みるような関数が入ったリスト
   * @param onMissingArguments 引数がパーサに対して不足しているときに[RawCommandContext]を用いて実行する[ContextualExecutor]
   *
   * @return [argumentsParser]に, [parsers]と[onMissingArguments]が組み合わされた関数が入った新しい[ContextualExecutorBuilder].
   */
  fun argumentsParsers(parsers: List<SingleArgumentParser>,
                       onMissingArguments: ContextualExecutor = PrintUsageExecutor): ContextualExecutorBuilder<CS> {
    val combinedParser: CommandArgumentsParser<CS> = { refinedSender, context: RawCommandContext ->
      tailrec suspend fun parse(parsers: List<(String) -> ResponseEffectOrResult<CS, Any>>,
                                onMissingArguments: ContextualExecutor,
                                args: List<String>,
                                reverseAccumulator: List<Any> = listOf()): Option<Pair<List<Any>, List<String>>> {
        val firstParser = parsers.firstOrNull() ?: return Some(reverseAccumulator.reversed() to args)
        val firstArg = args.firstOrNull()
            ?: return None.also { onMissingArguments.executeWith(context) }

        return when (val transformed = firstParser(firstArg)) {
          is Either.Left -> None.also { transformed.a.runFor(refinedSender) }
          is Either.Right -> {
            val parsedArg = transformed.b
            parse(parsers.drop(1), onMissingArguments, args.drop(1), reverseAccumulator.plus(parsedArg))
          }
        }
      }

      parse(parsers, onMissingArguments, context.args).map { parseResult ->
        PartiallyParsedArgs(parseResult.first, parseResult.second)
      }
    }

    return this.copy(argumentsParser = combinedParser)
  }

  /**
   * [contextualExecution]に[execution]に相当する関数が入った新しい[ContextualExecutorBuilder]を作成する.
   *
   * [ContextualExecutor]の制約にあるとおり, [execution]は任意スレッドからの呼び出しに対応しなければならない.
   */
  // TODO Effectを返すのでExecutionではない 名前を変えるべき
  fun execution(execution: ScopedContextualExecution<CS>): ContextualExecutorBuilder<CS> =
      this.copy(contextualExecution = execution)

  /**
   * @return [CS]を[CS1]へ狭めるキャストを試み,
   * 失敗したら[errorMessageOnFail]が返るような[senderTypeValidation]が入った
   * 新しい[ContextualExecutorBuilder]
   */
  inline fun <reified CS1 : CS> refineSender(errorMessageOnFail: TargetedEffect<CS>): ContextualExecutorBuilder<CS1> {
    val newSenderTypeValidation: SenderTypeValidation<CS1> = { sender ->
      senderTypeValidation(sender).flatMap { refined1 ->
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
  inline fun <reified CS1 : CS> refineSenderWithError(message: String): ContextualExecutorBuilder<CS1> =
      refineSender(message.asMessageEffect())

  /**
   * @return [CS]を[CS1]へ狭めるキャストを試み,
   * 失敗すると[messages]がエラーメッセージとして返る[senderTypeValidation]が入った
   * 新しい[ContextualExecutorBuilder]
   */
  inline fun <reified CS1 : CS> refineSenderWithError(messages: List<String>): ContextualExecutorBuilder<CS1> =
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
  fun build(): ContextualExecutor = object : ContextualExecutor {
    override suspend fun executeWith(rawContext: RawCommandContext) {
      senderTypeValidation(rawContext.sender)
          .flatMap { refinedSender ->
            argumentsParser(refinedSender, rawContext).map { parsedArgs ->
              ParsedArgCommandContext(refinedSender, rawContext.command, parsedArgs)
            }
          }
          .map { context -> contextualExecution(context).runFor(context.sender) }
    }
  }

  companion object {
    private val defaultArgumentParser: CommandArgumentsParser<CommandSender> = { _, context ->
      Some(PartiallyParsedArgs(listOf(), context.args))
    }
    private val defaultExecution: ScopedContextualExecution<CommandSender> = { EmptyEffect }
    private val defaultSenderValidation: SenderTypeValidation<CommandSender> = { sender: CommandSender -> Some(sender) }

    fun beginConfiguration() = ContextualExecutorBuilder(defaultSenderValidation, defaultArgumentParser, defaultExecution)
  }
}
