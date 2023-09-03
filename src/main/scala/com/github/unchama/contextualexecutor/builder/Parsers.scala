package com.github.unchama.contextualexecutor.builder

import cats.effect.IO
import com.github.unchama.generic.{CoerceTo, TryInto}
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.TargetedEffect.emptyEffect
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.NonNegative
import org.bukkit.command.CommandSender

import java.time.LocalDate
import java.time.format.{DateTimeFormatter, DateTimeParseException}

object Parsers {
  import ParserResponse._

  val identity: SingleArgumentParser[String] = {
    succeedWith(_)
  }

  def nonNegativeInteger(
    failureMessage: TargetedEffect[CommandSender] = emptyEffect
  ): SingleArgumentParser[Int Refined NonNegative] =
    closedRangeInt[Int Refined NonNegative](0, Int.MaxValue, failureMessage)

  /**
   * @tparam X refineされているかもしれない整数型。例: `Int`、[[Refined]][Int, [[eu.timepit.refined.numeric.Positive]]]
   * @return
   *   [smallEnd]より大きいか等しく[largeEnd]より小さいか等しい整数のパーサ
   */
  def closedRangeInt[X](
    smallEnd: Int,
    largeEnd: Int,
    failureMessage: TargetedEffect[CommandSender] = emptyEffect
  )(
    implicit coerceI: CoerceTo[X, Int],
    assertion: TryInto[Int, X, String]
  ): SingleArgumentParser[X] = { arg =>
    for {
      parsedInt <- integer(failureMessage)(arg)
      x <- assertion
        .tryInto(parsedInt)
        // RではなくLを写す
        .left
        .map(errorMessage =>
          TargetedEffect.delay[IO, CommandSender](cs => cs.sendMessage(errorMessage))
        )

      res <- if ((smallEnd to largeEnd).contains(coerceI.coerceTo(x)))
        succeedWith(x)
      else
        failWith(failureMessage)
    } yield res
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

  // It is safe to cache.
  private val hyphenatedDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  /**
   * `YYYY-mm-DD`形式の日付文字列をパースするパーサー。
   */
  def hyphenatedDate(
    failureMessage: TargetedEffect[CommandSender] = emptyEffect
  ): SingleArgumentParser[LocalDate] = in =>
    try {
      Right(LocalDate.parse(in, hyphenatedDateFormatter))
    } catch {
      case _: DateTimeParseException =>
        Left(failureMessage)
      case e => throw e
    }
}
