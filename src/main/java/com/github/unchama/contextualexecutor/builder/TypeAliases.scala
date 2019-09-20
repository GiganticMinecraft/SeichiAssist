package com.github.unchama.contextualexecutor.builder

typealias Result[Error, Success] = Either[Error, Success]

typealias ResponseEffectOrResult[CS, T] = Result[TargetedEffect[CS], T]

typealias SingleArgumentParser = (String) => ResponseEffectOrResult[CommandSender, Any]

typealias SenderTypeValidation[CS] = suspend (CommandSender) => Option[CS]

typealias CommandArgumentsParser[CS] = suspend (CS, RawCommandContext) => Option[PartiallyParsedArgs]

typealias ScopedContextualExecution[CS] = suspend (ParsedArgCommandContext[CS]) => TargetedEffect[CS]
