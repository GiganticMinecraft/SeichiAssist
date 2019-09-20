package com.github.unchama.contextualexecutor.builder

object TypeAliases {
  type Result[Error, Success] = Either[Error, Success]

  type ResponseEffectOrResult[CS, T] = Result[TargetedEffect[CS], T]

  type SingleArgumentParser = (String) => ResponseEffectOrResult[CommandSender, Any]

  type SenderTypeValidation[CS] = suspend (CommandSender) => Option[CS]

  type CommandArgumentsParser[CS] = suspend (CS, RawCommandContext) => Option[PartiallyParsedArgs]

  type ScopedContextualExecution[CS] = suspend (ParsedArgCommandContext[CS]) => TargetedEffect[CS]
}
