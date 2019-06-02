package com.github.unchama.contextualexecutor.builder

import arrow.core.Either
import arrow.core.Option
import com.github.unchama.contextualexecutor.ParsedArgCommandContext
import com.github.unchama.contextualexecutor.PartiallyParsedArgs
import com.github.unchama.contextualexecutor.RawCommandContext
import com.github.unchama.contextualexecutor.builder.response.ResponseToSender

typealias Result<Error, Success> = Either<Error, Success>

typealias ResponseOrResult<T> = Result<ResponseToSender, T>

typealias CommandArgumentsParser = (RawCommandContext) -> ResponseOrResult<PartiallyParsedArgs>

typealias ScopedContextualExecution<CS> = suspend (ParsedArgCommandContext<CS>) -> ResponseToSender

typealias SingleArgumentParser = (String) -> ResponseOrResult<Any>
