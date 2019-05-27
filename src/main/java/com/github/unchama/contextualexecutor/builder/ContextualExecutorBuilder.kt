package com.github.unchama.contextualexecutor.builder

import arrow.core.*
import arrow.effects.IO
import com.github.unchama.contextualexecutor.ContextualExecutor
import com.github.unchama.contextualexecutor.ParsedArgCommandContext
import com.github.unchama.contextualexecutor.PartiallyParsedArgs
import com.github.unchama.contextualexecutor.RawCommandContext
import com.github.unchama.contextualexecutor.builder.response.asResponseToSender
import org.bukkit.command.CommandSender
import arrow.core.extensions.either.fx.fx as fxEither
import arrow.effects.extensions.io.fx.fx as fxIO

/**
 * [ContextualExecutor]を作成するためのビルダークラス.
 *
 * @param CS 生成するExecutorが受け付ける[CommandSender]のサブタイプの上限
 * @param senderTypeValidation [CommandSender]
 * @param argumentsParser [RawCommandContext]から[PartiallyParsedArgs]を作成する関数
 * @param contextualExecution [ParsedArgCommandContext]からコマンドのアクションを表す[IO]を計算する関数
 */
data class ContextualExecutorBuilder<CS: CommandSender>(
        val senderTypeValidation: (CommandSender) -> ResponseOrResult<CS>,
        val argumentsParser: CommandArgumentsParser,
        val contextualExecution: ScopedContextualExecution<CS>) {

    private tailrec fun parse(parsers: List<(String) -> ResponseOrResult<Any>>,
                              onMissingArguments: CommandResponse,
                              args: List<String>,
                              reverseAccumulator: List<Any> = listOf()): ResponseOrResult<Pair<List<Any>, List<String>>> {
        val firstParser = parsers.firstOrNull() ?: return Right(reverseAccumulator.reversed() to args)
        val firstArg = args.firstOrNull() ?: return Left(onMissingArguments)

        return when (val transformed = firstParser(firstArg)) {
            is Either.Left -> transformed
            is Either.Right -> {
                val parsedArg = transformed.b
                parse(parsers.drop(1), onMissingArguments, args.drop(1), reverseAccumulator.plus(parsedArg))
            }
        }
    }

    /**
     * @param parsers i番目にi番目の引数の変換を試みるような関数が入ったリスト
     * @param onMissingArguments 引数がパーサに対して不足しているときに返却すべき[CommandResponse]を生成する関数
     *
     * @return [argumentsParser]に, [parsers]と[onMissingArguments]が組み合わされた関数が入った新しい[ContextualExecutorBuilder].
     */
    fun argumentsParsers(parsers: List<(String) -> ResponseOrResult<Any>>,
                         onMissingArguments: (RawCommandContext) -> CommandResponse = commandUsageResponse): ContextualExecutorBuilder<CS> {
        val combinedParser: (RawCommandContext) -> ResponseOrResult<PartiallyParsedArgs> = { context: RawCommandContext ->
            parse(parsers, onMissingArguments(context), context.args).map { parseResult ->
                PartiallyParsedArgs(parseResult.first, parseResult.second)
            }
        }

        return this.copy(argumentsParser = combinedParser)
    }

    /**
     * @return [contextualExecution]に[execution]に相当する関数が入った新しい[ContextualExecutorBuilder]
     */
    fun execution(execution: ScopedContextualExecution<CS>): ContextualExecutorBuilder<CS> =
            this.copy(contextualExecution = execution)

    /**
     * @return [CS]を[CS1]へ狭めるキャストを試み,
     * 失敗したら[errorMessageOnFail]が返るような[senderTypeValidation]が入った
     * 新しい[ContextualExecutorBuilder]
     */
    inline fun <reified CS1: CS> refineSender(errorMessageOnFail: CommandResponse): ContextualExecutorBuilder<CS1> {
        val newSenderTypeValidation: (CommandSender) -> ResponseOrResult<CS1> = { sender ->
            fxEither {
                val (refined1: CS) = senderTypeValidation(sender)
                val (refined2: CS1) = (refined1 as? CS1).toOption().toEither { errorMessageOnFail }

                refined2
            }
        }

        return ContextualExecutorBuilder(newSenderTypeValidation, argumentsParser, contextualExecution)
    }

    /**
     * @return [CS]を[CS1]へ狭めるキャストを試み,
     * 失敗してもエラーメッセージが返らない[senderTypeValidation]が入った
     * 新しい[ContextualExecutorBuilder]
     */
    inline fun <reified CS1: CS> refineSenderWithoutError(): ContextualExecutorBuilder<CS1> =
            refineSender(None)

    /**
     * @return [CS]を[CS1]へ狭めるキャストを試み,
     * 失敗すると[message]がエラーメッセージとして返る[senderTypeValidation]が入った
     * 新しい[ContextualExecutorBuilder]
     */
    inline fun <reified CS1: CS> refineSenderWithError(message: String): ContextualExecutorBuilder<CS1> =
            refineSender(Some(message.asResponseToSender()))

    /**
     * @return [CS]を[CS1]へ狭めるキャストを試み,
     * 失敗すると[messages]がエラーメッセージとして返る[senderTypeValidation]が入った
     * 新しい[ContextualExecutorBuilder]
     */
    inline fun <reified CS1: CS> refineSenderWithError(messages: List<String>): ContextualExecutorBuilder<CS1> =
            refineSender(Some(messages.asResponseToSender()))

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
     * ような[IO]を生成する.
     */
    fun build(): ContextualExecutor = object : ContextualExecutor {
        override fun executionFor(rawContext: RawCommandContext): IO<Unit> {
            val errorOrContext: Either<CommandResponse, ParsedArgCommandContext<CS>> =
                    fxEither {
                        val (refinedSender) = senderTypeValidation(rawContext.sender)
                        val (parsedArgs) = argumentsParser(rawContext)

                        ParsedArgCommandContext(refinedSender, rawContext.command, parsedArgs)
                    }

            return fxIO {
                val (response) = errorOrContext.fold({ IO.just(it) }, { CommandExecutionScope.contextualExecution(it) })
                val (_) = sendResponse(rawContext.sender, response)
            }
        }
    }

    companion object {
        private val defaultArgumentParser: CommandArgumentsParser = { context ->
            Right(PartiallyParsedArgs(listOf(), context.args))
        }
        private val defaultExecution: ScopedContextualExecution<CommandSender> = { returnNone() }
        private val defaultSenderValidation = { sender: CommandSender -> Right(sender) }

        fun beginConfiguration() = ContextualExecutorBuilder(defaultSenderValidation, defaultArgumentParser, defaultExecution)

        private fun sendResponse(sender: CommandSender, response: CommandResponse): IO<Unit> =
                when (response) {
                    is Some -> {
                        fxIO {
                            !effect {
                                response.t.transmitTo(sender)
                            }
                        }
                    }
                    else -> { IO.unit }
                }

        private val commandUsageResponse: (RawCommandContext) -> CommandResponse = {
            Some(it.command.command.usage.asResponseToSender())
        }
    }
}
