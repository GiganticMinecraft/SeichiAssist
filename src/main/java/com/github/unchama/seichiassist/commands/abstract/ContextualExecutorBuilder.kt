package com.github.unchama.seichiassist.commands.abstract

import arrow.core.*
import arrow.effects.IO
import com.github.unchama.seichiassist.commands.abstract.response.ResponseToSender
import com.github.unchama.seichiassist.commands.abstract.response.asResponseToSender
import org.bukkit.command.CommandSender
import arrow.core.extensions.either.fx.fx as fxEither
import arrow.effects.extensions.io.fx.fx as fxIO

typealias CommandResponse = Option<ResponseToSender>

typealias Result<Error, Success> = Either<Error, Success>
typealias ResponseOrResult<T> = Result<CommandResponse, T>

typealias CommandArgumentsParser = (RawCommandContext) -> ResponseOrResult<PartiallyParsedArgs>

typealias ScopedContextualExecution<CS> = CommandExecutionScope.(ParsedArgCommandContext<CS>) -> IO<CommandResponse>

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

private val commandUsage: (RawCommandContext) -> CommandResponse = { Some(it.command.command.usage.asResponseToSender()) }

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

    fun argumentsParsers(parsers: List<(String) -> ResponseOrResult<Any>>,
                         onMissingArguments: (RawCommandContext) -> CommandResponse = commandUsage): ContextualExecutorBuilder<CS> {
        val combinedParser: (RawCommandContext) -> ResponseOrResult<PartiallyParsedArgs> = { context: RawCommandContext ->
            parse(parsers, onMissingArguments(context), context.args).map { parseResult ->
                PartiallyParsedArgs(parseResult.first, parseResult.second)
            }
        }

        return this.copy(argumentsParser = combinedParser)
    }

    fun execution(execution: ScopedContextualExecution<CS>): ContextualExecutorBuilder<CS> =
            this.copy(contextualExecution = execution)

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

    inline fun <reified CS1: CS> refineSenderWithoutError(): ContextualExecutorBuilder<CS1> =
            refineSender(None)

    inline fun <reified CS1: CS> refineSenderWithError(message: String): ContextualExecutorBuilder<CS1> =
            refineSender(Some(message.asResponseToSender()))

    inline fun <reified CS1: CS> refineSenderWithError(messages: List<String>): ContextualExecutorBuilder<CS1> =
            refineSender(Some(messages.asResponseToSender()))

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
    }
}
