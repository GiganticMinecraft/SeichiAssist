package com.github.unchama.seichiassist.commands.contextual.builder

import arrow.core.Either
import arrow.core.Option
import arrow.effects.IO
import com.github.unchama.seichiassist.commands.contextual.ParsedArgCommandContext
import com.github.unchama.seichiassist.commands.contextual.PartiallyParsedArgs
import com.github.unchama.seichiassist.commands.contextual.RawCommandContext
import com.github.unchama.seichiassist.commands.contextual.builder.response.ResponseToSender

typealias CommandResponse = Option<ResponseToSender>

typealias Result<Error, Success> = Either<Error, Success>

typealias ResponseOrResult<T> = Result<CommandResponse, T>

typealias CommandArgumentsParser = (RawCommandContext) -> ResponseOrResult<PartiallyParsedArgs>

typealias ScopedContextualExecution<CS> = CommandExecutionScope.(ParsedArgCommandContext<CS>) -> IO<CommandResponse>
