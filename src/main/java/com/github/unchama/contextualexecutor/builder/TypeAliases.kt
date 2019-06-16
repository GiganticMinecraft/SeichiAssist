package com.github.unchama.contextualexecutor.builder

import arrow.core.Either
import arrow.core.Option
import com.github.unchama.contextualexecutor.ParsedArgCommandContext
import com.github.unchama.contextualexecutor.PartiallyParsedArgs
import com.github.unchama.contextualexecutor.RawCommandContext
import com.github.unchama.effect.TargetedEffect
import org.bukkit.command.CommandSender

typealias Result<Error, Success> = Either<Error, Success>

typealias ResponseEffectOrResult<CS, T> = Result<TargetedEffect<CS>, T>

typealias SingleArgumentParser = (String) -> ResponseEffectOrResult<CommandSender, Any>

typealias SenderTypeValidation<CS> = suspend (CommandSender) -> Option<CS>

typealias CommandArgumentsParser<CS> = suspend (CS, RawCommandContext) -> Option<PartiallyParsedArgs>

typealias ScopedContextualExecution<CS> = suspend (ParsedArgCommandContext<CS>) -> TargetedEffect<CS>
