package com.github.unchama.contextualexecutor.builder

import arrow.core.Either
import arrow.core.Option
import arrow.effects.IO
import com.github.unchama.contextualexecutor.ParsedArgCommandContext
import com.github.unchama.contextualexecutor.PartiallyParsedArgs
import com.github.unchama.contextualexecutor.RawCommandContext
import com.github.unchama.contextualexecutor.builder.response.ResponseToSender

typealias CommandResponse = Option<ResponseToSender>

typealias Result<Error, Success> = Either<Error, Success>

typealias ResponseOrResult<T> = Result<CommandResponse, T>

typealias CommandArgumentsParser = (RawCommandContext) -> ResponseOrResult<PartiallyParsedArgs>

typealias ScopedContextualExecution<CS> = CommandExecutionScope.(ParsedArgCommandContext<CS>) -> IO<CommandResponse>
