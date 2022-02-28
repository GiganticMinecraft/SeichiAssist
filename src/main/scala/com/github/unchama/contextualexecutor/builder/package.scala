package com.github.unchama.contextualexecutor

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.command.CommandSender

package object builder {
  type Result[+Error, +Success] = Either[Error, Success]

  type ResponseEffectOrResult[-CS, +T] = Result[TargetedEffect[CS], T]

  type SingleArgumentParser = String => ResponseEffectOrResult[CommandSender, Any]

  type SenderTypeValidation[+CS] = CommandSender => IO[Option[CS]]

  type CommandArgumentsParser[-CS] = (CS, RawCommandContext) => IO[Option[PartiallyParsedArgs]]

  type ScopedContextualExecution[-CS <: CommandSender] =
    ParsedArgCommandContext[CS] => IO[TargetedEffect[CS]]

  type ExecutionF[F[_], CS <: CommandSender, U] = ParsedArgCommandContext[CS] => F[U]

  // コンテキストから、 CS に対して実行できる作用への関数
  type ExecutionCSEffect[F[_], CS <: CommandSender, U] =
    ParsedArgCommandContext[CS] => Kleisli[F, CS, U]
}
