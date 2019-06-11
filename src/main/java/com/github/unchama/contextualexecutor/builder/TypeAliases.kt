package com.github.unchama.contextualexecutor.builder

import arrow.core.Either
import com.github.unchama.contextualexecutor.ParsedArgCommandContext
import com.github.unchama.contextualexecutor.PartiallyParsedArgs
import com.github.unchama.contextualexecutor.RawCommandContext
import com.github.unchama.messaging.MessageToSender

typealias Result<Error, Success> = Either<Error, Success>

typealias ResponseOrResult<T> = Result<MessageToSender, T>

typealias CommandArgumentsParser = suspend (RawCommandContext) -> ResponseOrResult<PartiallyParsedArgs>

typealias ScopedContextualExecution<CS> = suspend (ParsedArgCommandContext<CS>) -> MessageToSender

typealias SingleArgumentParser = (String) -> ResponseOrResult<Any>
