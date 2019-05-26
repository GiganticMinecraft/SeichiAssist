package com.github.unchama.seichiassist.commands.abstract

import arrow.core.*
import arrow.data.EitherT
import arrow.data.extensions.eithert.monad.monad
import arrow.data.value
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.extensions.io.fx.fx
import arrow.effects.extensions.io.monadThrow.monadThrow
import arrow.effects.fix
import org.bukkit.command.CommandSender

import arrow.core.extensions.either.monad.binding as bindEither
import arrow.effects.extensions.io.monadThrow.binding as bindIO

typealias CommandResponse = Option<ResponseToSender>

typealias Result<Error, Success> = Either<Error, Success>
typealias ResponseOrResult<T> = Result<CommandResponse, T>

typealias CommandArgumentParser = (List<String>) -> IO<ResponseOrResult<PartiallyParsedArgs>>

typealias ScopedContextualExecution<CS> = CommandExecutionScope.(ParsedArgCommandContext<CS>) -> IO<CommandResponse>

private fun sendResponse(sender: CommandSender, response: CommandResponse): IO<Unit> =
        when (response) {
            is Some -> {
                fx {
                    !effect {
                        sender.sendMessage(response.t)
                    }
                }
            }
            else -> { IO.unit }
        }

data class ContextualExecutorBuilder<CS: CommandSender>(
        val senderTypeValidation: (CommandSender) -> ResponseOrResult<CS>,
        val argumentParser: CommandArgumentParser,
        val contextualExecution: CommandExecutionScope.(ParsedArgCommandContext<CS>) -> IO<CommandResponse>) {

    fun argumentsParsing(configure: ArgumentParserConfigurationScope.() -> Unit): ContextualExecutorBuilder<CS> =
            this.copy(argumentParser = ArgumentParserConfigurationScope().apply { configure() }.buildParser())

    fun execution(execution: ScopedContextualExecution<CS>): ContextualExecutorBuilder<CS> =
            this.copy(contextualExecution = execution)

    inline fun <reified CS1: CS> refineSender(errorMessageOnFail: CommandResponse): ContextualExecutorBuilder<CS1> {
        val newSenderTypeValidation: (CommandSender) -> ResponseOrResult<CS1> = { sender ->
            bindEither {
                val (refined1: CS) = senderTypeValidation(sender)
                val (refined2: CS1) = (refined1 as? CS1).toOption().toEither { errorMessageOnFail }

                refined2
            }
        }

        return ContextualExecutorBuilder(newSenderTypeValidation, argumentParser, contextualExecution)
    }

    inline fun <reified CS1: CS> refineSenderWithoutError(): ContextualExecutorBuilder<CS1> =
            refineSender(None)

    inline fun <reified CS1: CS> refineSenderWithError(message: String): ContextualExecutorBuilder<CS1> =
            refineSender(Some(message.asResponseToSender()))

    inline fun <reified CS1: CS> refineSenderWithError(messages: List<String>): ContextualExecutorBuilder<CS1> =
            refineSender(Some(messages.asResponseToSender()))

    fun build(): ContextualExecutor = object : ContextualExecutor {
        override fun executionFor(rawContext: RawCommandContext): IO<Unit> {
            val createContext: IO<Either<CommandResponse, ParsedArgCommandContext<CS>>> =
                    EitherT.monad<ForIO, CommandResponse>(IO.monadThrow()).binding {
                        val (refinedSender) = EitherT(IO.just(senderTypeValidation(rawContext.sender)))
                        val (parsedArgs) = EitherT(argumentParser(rawContext.args))

                        ParsedArgCommandContext(refinedSender, rawContext.command, parsedArgs)
                    }.value().fix()

            return bindIO {
                val (errorOrContext) = createContext
                val (response) = errorOrContext.fold({ IO.just(it) }, { CommandExecutionScope.contextualExecution(it) })
                val (_) = sendResponse(rawContext.sender, response)
            }
        }
    }

    companion object {
        private val defaultArgumentParser: CommandArgumentParser = { IO.just(Right(PartiallyParsedArgs(listOf(), it))) }
        private val defaultExecution: ScopedContextualExecution<CommandSender> = { returnNone() }
        private val defaultSenderValidation = { sender: CommandSender -> Right(sender) }

        fun beginConfiguration() = ContextualExecutorBuilder(defaultSenderValidation, defaultArgumentParser, defaultExecution)
    }
}
