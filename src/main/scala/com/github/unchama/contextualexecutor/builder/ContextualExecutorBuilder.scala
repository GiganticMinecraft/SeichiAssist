package com.github.unchama.contextualexecutor.builder

import cats.data.OptionT
import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.TypeAliases.{CommandArgumentsParser, ScopedContextualExecution, SenderTypeValidation, SingleArgumentParser}
import com.github.unchama.contextualexecutor.executors.PrintUsageExecutor
import com.github.unchama.contextualexecutor.{ContextualExecutor, ParsedArgCommandContext, PartiallyParsedArgs, RawCommandContext}
import com.github.unchama.targetedeffect
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.syntax._
import org.bukkit.command.CommandSender

import scala.reflect.ClassTag

/**
 * [ContextualExecutor]を作成するためのビルダークラス.
 *
 * 各引数はビルドされる[ContextualExecutor]において異常系を見つけるとすぐに[RawCommandContext.sender]に応答を送り返す.
 * この副作用を内包させるためにsuspending functionとして宣言されている.
 *
 * @tparam CS 生成するExecutorが受け付ける[CommandSender]のサブタイプの上限
 * @param senderTypeValidation [CommandSender]の[CS]へのダウンキャストを試みる関数
 * @param argumentsParser      [RawCommandContext]から[PartiallyParsedArgs]の作成を試みる関数
 * @param contextualExecution  [ParsedArgCommandContext]に基づいてコマンドの副作用を計算する関数
 */
case class ContextualExecutorBuilder[CS <: CommandSender](senderTypeValidation: SenderTypeValidation[CS],
                                                          argumentsParser: CommandArgumentsParser[CS],
                                                          contextualExecution: ScopedContextualExecution[CS]) {

  /**
   * @param parsers            i番目にi番目の引数の変換を試みるような関数が入ったリスト
   * @param onMissingArguments 引数がパーサに対して不足しているときに[RawCommandContext]を用いて実行する[ContextualExecutor]
   * @return [argumentsParser]に, [parsers]と[onMissingArguments]が組み合わされた関数が入った新しい[ContextualExecutorBuilder].
   */
  def argumentsParsers(parsers: List[SingleArgumentParser],
                       onMissingArguments: ContextualExecutor = PrintUsageExecutor): ContextualExecutorBuilder[CS] = {
    val combinedParser: CommandArgumentsParser[CS] = {
      case (refinedSender, context: RawCommandContext) =>
        @scala.annotation.tailrec
        def parse(remainingParsers: List[SingleArgumentParser],
                  remainingArgs: List[String],
                  reverseAccumulator: List[Any] = List()): Either[IO[Unit], PartiallyParsedArgs] = {
          val (parserHead, parserTail) = remainingParsers match {
            case ::(head, next) => (head, next)
            case Nil => return Right(PartiallyParsedArgs(reverseAccumulator.reverse, remainingArgs))
          }

          val (argHead, argTail) = remainingArgs match {
            case ::(head, next) => (head, next)
            case Nil => return Left(onMissingArguments.executeWith(context))
          }

          parserHead(argHead) match {
            case Left(failingEffect) => Left(failingEffect(refinedSender))
            case Right(parsedArg) => parse(parserTail, argTail, parsedArg :: reverseAccumulator)
          }
        }

        parse(parsers, context.args) match {
          case Right(partiallyParsed) => IO.pure(Some(partiallyParsed))
          case Left(effect) => effect.map(_ => None)
        }
    }

    this.copy(argumentsParser = combinedParser)
  }

  /**
   * [contextualExecution]に[execution]に相当する関数が入った新しい[ContextualExecutorBuilder]を作成する.
   *
   * [ContextualExecutor]の制約にあるとおり, [execution]は任意スレッドからの呼び出しに対応しなければならない.
   */
  def execution(execution: ScopedContextualExecution[CS]): ContextualExecutorBuilder[CS] =
    this.copy(contextualExecution = execution)

  /**
   * @return [CS]を[CS1]へ狭めるキャストを試み,
   *         失敗すると[message]がエラーメッセージとして返る[senderTypeValidation]が入った
   *         新しい[ContextualExecutorBuilder]
   */
  def refineSenderWithError[CS1 <: CS : ClassTag](message: String): ContextualExecutorBuilder[CS1] =
    refineSender(message.asMessageEffect())

  /**
   * @return [CS]を[CS1]へ狭めるキャストを試み,
   *         失敗すると[messages]がエラーメッセージとして返る[senderTypeValidation]が入った
   *         新しい[ContextualExecutorBuilder]
   */
  def refineSenderWithError[CS1 <: CS : ClassTag](messages: List[String]): ContextualExecutorBuilder[CS1] =
    refineSender(messages.asMessageEffect())

  /**
   * @return [CS]を[CS1]へ狭めるキャストを試み,
   *         失敗したら[errorMessageOnFail]が返るような[senderTypeValidation]が入った
   *         新しい[ContextualExecutorBuilder]
   */
  def refineSender[CS1 <: CS : ClassTag](effectOnFail: TargetedEffect[CommandSender]): ContextualExecutorBuilder[CS1] = {
    val newSenderTypeValidation: SenderTypeValidation[CS1] = { sender =>
      val verificationProgram = for {
        refined1 <- OptionT(senderTypeValidation(sender))
        refined2: CS1 <- refined1 match {
          case refined1: CS1 => OptionT.pure[IO](refined1)
          case _ => OptionT[IO, Nothing](effectOnFail(sender).map(_ => None))
        }
      } yield refined2

      verificationProgram.value
    }

    ContextualExecutorBuilder(newSenderTypeValidation, argumentsParser, contextualExecution)
  }

  /**
   * ビルダーに入っている情報から[ContextualExecutor]を生成する.
   *
   * 生成された[ContextualExecutor]は,
   *
   *  - 先ず, 送信者が[CS]であるかを確認する
   *  - 次に, 引数のパースを試みる
   *  - 最後に, 変換された引数を用いて[ParsedArgCommandContext]を作成し,
   * それを用いて[contextualExecution]で指定される動作を行う
   *
   * 処理を[ContextualExecutor.executeWith]内で行う.
   */
  def build(): ContextualExecutor = (rawContext: RawCommandContext) => {
    val optionalExecution = for {
      refinedSender <- OptionT(senderTypeValidation(rawContext.sender))
      parsedArgs <- OptionT(argumentsParser(refinedSender, rawContext))
      context = ParsedArgCommandContext(refinedSender, rawContext.command, parsedArgs)
      executionResponse <- OptionT.liftF(contextualExecution(context))
      _ <- OptionT.liftF(executionResponse(context.sender))
    } yield ()

    optionalExecution.value.map(_ => ())
  }
}

object ContextualExecutorBuilder {
  private val defaultArgumentParser: CommandArgumentsParser[CommandSender] = {
    case (_, context) =>
      IO.pure(Some(PartiallyParsedArgs(List(), context.args)))
  }
  private val defaultExecution: ScopedContextualExecution[CommandSender] = { _ => IO(targetedeffect.emptyEffect) }
  private val defaultSenderValidation: SenderTypeValidation[CommandSender] = { sender: CommandSender => IO.pure(Some(sender)) }

  def beginConfiguration() = ContextualExecutorBuilder(defaultSenderValidation, defaultArgumentParser, defaultExecution)
}
