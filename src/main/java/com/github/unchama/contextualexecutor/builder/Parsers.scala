package com.github.unchama.contextualexecutor.builder

import com.github.unchama.contextualexecutor.builder.TypeAliases.SingleArgumentParser
import com.github.unchama.targetedeffect.{EmptyEffect, TargetedEffect}
import org.bukkit.command.CommandSender

object Parsers {
  val identity: SingleArgumentParser = parser { succeedWith(it) }

  def integer(failureMessage: TargetedEffect[CommandSender] = EmptyEffect): SingleArgumentParser = {
    val parseResult = it.toIntOrNull()

    if (parseResult != null) succeedWith(parseResult) else failWith(failureMessage)
  }

  val boolean: SingleArgumentParser = { succeedWith(it.toBoolean()) }

  /**
   * @return [smallEnd]より大きいか等しく[largeEnd]より小さいか等しい整数のパーサ
   */
  def closedRangeInt(
      smallEnd: Int, largeEnd: Int,
      failureMessage: TargetedEffect[CommandSender] = EmptyEffect): SingleArgumentParser = { arg =>
    integer(failureMessage)(arg).flatMap {
      val parsed = it.asInstanceOf[Int]

      if (parsed in smallEnd..largeEnd) succeedWith(it) else failWith(failureMessage)
    }
  }

  def nonNegativeInteger(failureMessage: TargetedEffect[CommandSender] = EmptyEffect): SingleArgumentParser =
      closedRangeInt(0, Int.MAX_VALUE, failureMessage)

  def double(failureMessage: TargetedEffect[CommandSender] = EmptyEffect): SingleArgumentParser = {
    val parseResult = it.toDoubleOrNull()

    if (parseResult != null) succeedWith(parseResult) else failWith(failureMessage)
  }
}