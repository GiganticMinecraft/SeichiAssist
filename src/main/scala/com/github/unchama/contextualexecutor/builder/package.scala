package com.github.unchama.contextualexecutor

import cats.effect.IO
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.command.CommandSender

package object builder {
  type Result[+Error, +Success] = Either[Error, Success]

  type ResponseEffectOrResult[-CS, +T] = Result[TargetedEffect[CS], T]

  type SingleArgumentParser = String => ResponseEffectOrResult[CommandSender, Any]

  type SenderTypeValidation[+CS] = CommandSender => IO[Option[CS]]

  type CommandArgumentsParser[-CS] = (CS, RawCommandContext) => IO[Option[PartiallyParsedArgs]]

  type ScopedContextualExecution[-CS <: CommandSender] = ParsedArgCommandContext[CS] => IO[TargetedEffect[CS]]
}
