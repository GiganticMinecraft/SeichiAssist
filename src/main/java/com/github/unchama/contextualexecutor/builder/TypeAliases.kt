package com.github.unchama.contextualexecutor.builder

import arrow.core.Either
import arrow.core.Option
import com.github.unchama.contextualexecutor.ParsedArgCommandContext
import com.github.unchama.contextualexecutor.PartiallyParsedArgs
import com.github.unchama.contextualexecutor.RawCommandContext
import com.github.unchama.messaging.MessageToSender
import org.bukkit.command.CommandSender

typealias Result<Error, Success> = Either<Error, Success>

typealias ResponseOrResult<T> = Result<MessageToSender, T>

typealias SenderTypeValidation<CS> = suspend (CommandSender) -> Option<CS>

typealias CommandArgumentsParser = suspend (RawCommandContext) -> Option<PartiallyParsedArgs>

typealias ScopedContextualExecution<CS> = suspend (ParsedArgCommandContext<CS>) -> MessageToSender

typealias SingleArgumentParser = (String) -> ResponseOrResult<Any>
