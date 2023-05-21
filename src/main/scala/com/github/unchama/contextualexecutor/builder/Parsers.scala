package com.github.unchama.contextualexecutor.builder

import com.github.unchama.generic.CoerceTo
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.TargetedEffect.emptyEffect
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.NonNegative
import org.bukkit.command.CommandSender

object Parsers {
  import ParserResponse._

  val identity: SingleArgumentParser[String] = {
    succeedWith(_)
  }

  def nonNegativeInteger(
    failureMessage: TargetedEffect[CommandSender] = emptyEffect
  ): SingleArgumentParser[Int Refined NonNegative] =
    closedRangeInt(0, Int.MaxValue, failureMessage)

  /**
   * @return
   *   [smallEnd]より大きいか等しく[largeEnd]より小さいか等しい整数のパーサ
   */
  def closedRangeInt[I](
    smallEnd: I,
    largeEnd: I,
    failureMessage: TargetedEffect[CommandSender] = emptyEffect
  )(implicit coerceI: CoerceTo[I, Int]): SingleArgumentParser[I] = { arg =>
    integer(failureMessage)(arg).flatMap { p =>
      val parsed = p.asInstanceOf[I]
      if ((coerceI.coerceTo(smallEnd) to coerceI.coerceTo(largeEnd)).contains(parsed))
        succeedWith(parsed)
      else
        failWith(failureMessage)
    }
  }

  def integer(
    failureEffect: TargetedEffect[CommandSender] = emptyEffect
  ): SingleArgumentParser[Int] = { arg =>
    arg.toIntOption match {
      case Some(value) => succeedWith(value)
      case None        => failWith(failureEffect)
    }
  }

  def double(
    failureEffect: TargetedEffect[CommandSender] = emptyEffect
  ): SingleArgumentParser[Double] = { arg =>
    arg.toDoubleOption match {
      case Some(value) => succeedWith(value)
      case None        => failWith(failureEffect)
    }
  }

  def fromOptionParser[T](
    fromString: String => Option[T],
    failureMessage: TargetedEffect[CommandSender] = emptyEffect
  ): SingleArgumentParser[T] = {
    fromString.andThen(_.toRight(failureMessage))
  }
}
