package com.github.unchama.contextualexecutor.builder

import com.github.unchama.contextualexecutor.{PartiallyParsedArgs, RawCommandContext}
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.{contextualexecutor, targetedeffect}
import org.bukkit.command.CommandSender

object TypeAliases {
  type Result[Error, Success] = Either[Error, Success]

  type ResponseEffectOrResult[CS, T] = Result[TargetedEffect[CS], T]

  type SingleArgumentParser = (String) => ResponseEffectOrResult[CommandSender, Any]

  type SenderTypeValidation[CS] = suspend (CommandSender) => Option[CS]

  type CommandArgumentsParser[CS] = suspend (CS, RawCommandContext) => Option[PartiallyParsedArgs]

  type ScopedContextualExecution[CS] = suspend
  (contextualexecutor.ParsedArgCommandContext[CS]
  ) => targetedeffect.TargetedEffect[CS]
}
