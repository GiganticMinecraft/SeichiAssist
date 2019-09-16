package com.github.unchama.contextualexecutor.builder

import arrow.core.flatMap
import com.github.unchama.contextualexecutor.builder.ArgumentParserScope.ScopeProvider.parser
import com.github.unchama.contextualexecutor.builder.ArgumentParserScope.failWith
import com.github.unchama.contextualexecutor.builder.ArgumentParserScope.succeedWith
import com.github.unchama.targetedeffect.EmptyEffect
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.command.CommandSender

object Parsers {
  val identity: SingleArgumentParser = parser { succeedWith(it) }

  fun integer(failureMessage: TargetedEffect<CommandSender> = EmptyEffect): SingleArgumentParser = {
    val parseResult = it.toIntOrNull()

    if (parseResult != null) succeedWith(parseResult) else failWith(failureMessage)
  }

  val boolean: SingleArgumentParser = { succeedWith(it.toBoolean()) }

  /**
   * @return [smallEnd]より大きいか等しく[largeEnd]より小さいか等しい整数のパーサ
   */
  fun closedRangeInt(
      smallEnd: Int, largeEnd: Int,
      failureMessage: TargetedEffect<CommandSender> = EmptyEffect): SingleArgumentParser = { arg ->
    integer(failureMessage)(arg).flatMap {
      val parsed = it as Int

      if (parsed in smallEnd..largeEnd) succeedWith(it) else failWith(failureMessage)
    }
  }

  fun nonNegativeInteger(failureMessage: TargetedEffect<CommandSender> = EmptyEffect): SingleArgumentParser =
      closedRangeInt(0, Int.MAX_VALUE, failureMessage)

  fun double(failureMessage: TargetedEffect<CommandSender> = EmptyEffect): SingleArgumentParser = {
    val parseResult = it.toDoubleOrNull()

    if (parseResult != null) succeedWith(parseResult) else failWith(failureMessage)
  }
}