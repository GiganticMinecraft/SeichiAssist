package com.github.unchama.seichiassist.commands.abstract

import arrow.core.Either
import arrow.core.Option
import arrow.effects.IO
import com.github.unchama.seichiassist.commands.abstract.response.ResponseToSender

typealias CommandResponse = Option<ResponseToSender>

typealias Result<Error, Success> = Either<Error, Success>

typealias ResponseOrResult<T> = Result<CommandResponse, T>

typealias CommandArgumentsParser = (RawCommandContext) -> ResponseOrResult<PartiallyParsedArgs>

typealias ScopedContextualExecution<CS> = CommandExecutionScope.(ParsedArgCommandContext<CS>) -> IO<CommandResponse>
