package com.github.unchama.contextualexecutor.builder

import cats.effect.IO
import com.github.unchama.contextualexecutor.{ParsedArgCommandContext, PartiallyParsedArgs, RawCommandContext}
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.command.CommandSender

object TypeAliases {
  type Result[+Error, +Success] = Either[Error, Success]

  type ResponseEffectOrResult[-CS, +T] = Result[TargetedEffect[CS], T]

  type SingleArgumentParser = String => ResponseEffectOrResult[CommandSender, Any]

  type SenderTypeValidation[+CS] = CommandSender => IO[Option[CS]]

  type CommandArgumentsParser[-CS] = (CS, RawCommandContext) => IO[Option[PartiallyParsedArgs]]

  type ScopedContextualExecution[-CS <: CommandSender] = ParsedArgCommandContext[CS] => IO[TargetedEffect[CS]]
}
