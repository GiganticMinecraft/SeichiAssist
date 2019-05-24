package com.github.unchama.seichiassist.commands.abstract

import arrow.core.*
import arrow.core.extensions.either.monad.binding
import org.bukkit.command.CommandSender

typealias CommandArgumentParser = (List<String>) -> Either<Option<ResponseToSender>, PartiallyParsedArgs>
typealias ContextualExecution<CS> = (ParsedArgCommandContext<CS>) -> ExecutionResult

object CommandExecutionScope {
    fun succeed(): Either<Nothing, None> = None.right()
    fun succeedWithMessage(message: ResponseToSender): Either<Nothing, Option<ResponseToSender>> = message.some().right()

    fun fail(): Either<None, Nothing> = None.left()
    fun failWithMessage(message: ResponseToSender): Either<Option<ResponseToSender>, Nothing> = message.some().left()
}

typealias ScopedContextualExecution<CS> = CommandExecutionScope.(ParsedArgCommandContext<CS>) -> ExecutionResult

data class ContextualExecutorBuilder<CS: CommandSender>(
        val senderTypeValidation: (CommandSender) -> Either<Option<ResponseToSender>, CS>,
        val argumentParser: CommandArgumentParser,
        val execution: ContextualExecution<CS>) {

    class ArgumentParserConfiguration {
        // TODO implement transformation-appending functions
        fun buildParser(): CommandArgumentParser = TODO()
    }

    fun argumentsParsing(configure: ArgumentParserConfiguration.() -> Unit): ContextualExecutorBuilder<CS> =
            this.copy(argumentParser = ArgumentParserConfiguration().apply { configure() }.buildParser())

    fun execution(execution: ScopedContextualExecution<CS>): ContextualExecutorBuilder<CS> =
            this.copy(execution = { CommandExecutionScope.execution(it) })

    inline fun <reified CS1: CS> refineSender(errorMessageOnFail: Option<ResponseToSender>): ContextualExecutorBuilder<CS1> {
        val newSenderTypeValidation: (CommandSender) -> Either<Option<ResponseToSender>, CS1> = { sender ->
            binding {
                val (refined1: CS) = senderTypeValidation(sender)
                val (refined2: CS1) = (refined1 as? CS1).toOption().toEither { errorMessageOnFail }

                refined2
            }
        }

        return ContextualExecutorBuilder(newSenderTypeValidation, argumentParser, execution)
    }

    inline fun <reified CS1: CS> refineSenderWithoutError(): ContextualExecutorBuilder<CS1> = refineSender(None)
    inline fun <reified CS1: CS> refineSenderWithError(errorMessageOnFail: ResponseToSender): ContextualExecutorBuilder<CS1> = refineSender(errorMessageOnFail.some())

    fun build(): ContextualExecutor = object : ContextualExecutor {
        override fun executeWith(rawContext: RawCommandContext): ExecutionResult =
                binding {
                    val (refinedSender) = senderTypeValidation(rawContext.sender)
                    val (parsedArg) = argumentParser(rawContext.args)
                    val context = ParsedArgCommandContext(refinedSender, rawContext.command, parsedArg)
                    val (response) = execution(context)

                    response
                }
    }

    companion object {
        fun beginConfiguration() = ContextualExecutorBuilder(defaultSenderValidation, defaultArgumentParser, defaultExecution)

        private val defaultArgumentParser: CommandArgumentParser = { Right(PartiallyParsedArgs(listOf(), it)) }
        private val defaultExecution: ContextualExecution<CommandSender> = { None.right() }
        private val defaultSenderValidation = { sender: CommandSender -> Right(sender) }
    }
}
