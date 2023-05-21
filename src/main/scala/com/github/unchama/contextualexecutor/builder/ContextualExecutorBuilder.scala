package com.github.unchama.contextualexecutor.builder

import cats.data.{Kleisli, OptionT}
import cats.effect.{Effect, IO}
import com.github.unchama.contextualexecutor.executors.PrintUsageExecutor
import com.github.unchama.contextualexecutor.{
  ContextualExecutor,
  ParsedArgCommandContext,
  PartiallyParsedArgs,
  RawCommandContext
}
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.TargetedEffect.emptyEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.command.CommandSender

import scala.reflect.ClassTag

/**
 * [ContextualExecutor]を作成するためのビルダークラス.
 *
 * 各引数はビルドされる[ContextualExecutor]において異常系を見つけるとすぐに[RawCommandContext.sender]に応答を送り返す.
 * この副作用を内包させるために[[IO]]への関数として宣言されている.
 *
 * @tparam CS
 *   生成するExecutorが受け付ける[CommandSender]のサブタイプの上限
 * @param senderTypeValidation
 *   [CommandSender]の[CS]へのダウンキャストを試みる関数
 * @param argumentsParser
 *   [RawCommandContext]から[PartiallyParsedArgs]の作成を試みる関数
 * @param contextualExecution
 *   [ParsedArgCommandContext]に基づいてコマンドの副作用を計算する関数
 */
case class ContextualExecutorBuilder[CS <: CommandSender, Args](
  senderTypeValidation: SenderTypeValidation[CS],
  argumentsParser: CommandArgumentsParser[CS, Args],
  contextualExecution: ScopedContextualExecution[CS, Args]
) {

  /**
   * @param parsers
   *   i番目にi番目の引数の変換を試みるような関数が入ったリスト
   * @param onMissingArguments
   *   引数がパーサに対して不足しているときに[RawCommandContext]を用いて実行する[ContextualExecutor]
   * @return
   *   [argumentsParser]に,
   *   [parsers]と[onMissingArguments]が組み合わされた関数が入った新しい[ContextualExecutorBuilder].
   */
  def argumentsParsers(
    // TODO(scala3): Scala 3ではタプルに対するまともな操作ができるようになるので、Scala 3に移行したらArgsがLUBに消去された結果
    //  型安全性が損なわれることを防ぐためにT <: Tupleを受け取るようにするべき。そうすることで.apply(Int)などの結果が硬安全になり、
    //  asInstanceOfによるキャストがかなり削減できる。
    parsers: List[SingleArgumentParser[Args]],
    onMissingArguments: ContextualExecutor = PrintUsageExecutor
  ): ContextualExecutorBuilder[CS, Args] = {
    val combinedParser: CommandArgumentsParser[CS, Args] = {
      case (refinedSender, context: RawCommandContext) =>
        @scala.annotation.tailrec
        def parse(
          remainingParsers: List[SingleArgumentParser[Args]],
          remainingArgs: List[String],
          reverseAccumulator: List[Args] = List()
        ): Either[IO[Unit], PartiallyParsedArgs[Args]] = {
          val (parserHead, parserTail) = remainingParsers match {
            case ::(head, next) => (head, next)
            case Nil =>
              return Right(PartiallyParsedArgs(reverseAccumulator.reverse, remainingArgs))
          }

          val (argHead, argTail) = remainingArgs match {
            case ::(head, next) => (head, next)
            case Nil            => return Left(onMissingArguments.executionWith(context))
          }

          parserHead(argHead) match {
            case Left(failingEffect) => Left(failingEffect(refinedSender))
            case Right(parsedArg) => parse(parserTail, argTail, parsedArg :: reverseAccumulator)
          }
        }

        parse(parsers, context.args) match {
          case Right(partiallyParsed) => IO.pure(Some(partiallyParsed))
          case Left(effect)           => effect.map(_ => None)
        }
    }

    this.copy(argumentsParser = combinedParser)
  }

  /**
   * [contextualExecution]に[execution]に相当する関数が入った新しい[ContextualExecutorBuilder]を作成する.
   *
   * [ContextualExecutor]の制約にあるとおり, [execution]は任意スレッドからの呼び出しに対応しなければならない.
   */
  def execution(execution: ScopedContextualExecution[CS, Args]): ContextualExecutorBuilder[CS, Args] =
    this.copy(contextualExecution = execution)

  /**
   * [contextualExecution]に[execution]に相当する関数が入った新しい[ContextualExecutorBuilder]を作成する.
   * ここで、`execution` はコンテキストを受け取って作用を起こすようなプログラムである.
   *
   * [ContextualExecutor]の制約にあるとおり, [execution]は任意スレッドでの実行に対応しなければならない.
   */
  def executionF[F[_]: Effect, U](
    execution: ExecutionF[F, CS, U, Args]
  ): ContextualExecutorBuilder[CS, Args] =
    this.copy(contextualExecution = context => {
      Effect[F].toIO(execution(context)).as(TargetedEffect.emptyEffect)
    })

  /**
   * [contextualExecution]に[execution]に相当する関数が入った新しい[ContextualExecutorBuilder]を作成する.
   * ここで、`execution` はコンテキストを受け取って、 コマンド実行者に対する作用(`Kleisli[F, CS, U]`) を起こすようなプログラムである.
   *
   * [ContextualExecutor]の制約にあるとおり, [execution]は任意スレッドでの実行に対応しなければならない.
   */
  def executionCSEffect[F[_]: Effect, U](
    execution: ExecutionCSEffect[F, CS, U, Args]
  ): ContextualExecutorBuilder[CS, Args] =
    executionF[F, U](context => execution(context).run(context.sender))

  /**
   * [[contextualExecution]]に、コンテキストを利用せずに走る `effect` が入った
   * 新しい[[ContextualExecutorBuilder]]を作成する.
   *
   * [[ContextualExecutor]]の制約にあるとおり, effect`は任意スレッドからの呼び出しに対応しなければならない.
   */
  def withEffectAsExecution[T](effect: Kleisli[IO, CS, T]): ContextualExecutorBuilder[CS, Args] =
    execution(_ => IO.pure(effect.map(_ => ())))

  /**
   * @return
   *   [CS]を[CS1]へ狭めるキャストを試み, 失敗すると[message]がエラーメッセージとして返る[senderTypeValidation]が入った
   *   新しい[ContextualExecutorBuilder]
   */
  def refineSenderWithError[CS1 <: CS: ClassTag](
    message: String
  ): ContextualExecutorBuilder[CS1, Args] =
    refineSender(MessageEffect(message))

  /**
   * @return
   *   [CS]を[CS1]へ狭めるキャストを試み, 失敗すると[messages]がエラーメッセージとして返る[senderTypeValidation]が入った
   *   新しい[ContextualExecutorBuilder]
   */
  def refineSenderWithError[CS1 <: CS: ClassTag](
    messages: List[String]
  ): ContextualExecutorBuilder[CS1, Args] =
    refineSender(MessageEffect(messages))

  /**
   * @return
   *   [CS]を[CS1]へ狭めるキャストを試み, 失敗したら[errorMessageOnFail]が返るような[senderTypeValidation]が入った
   *   新しい[ContextualExecutorBuilder]
   */
  def refineSender[CS1 <: CS: ClassTag](
    effectOnFail: TargetedEffect[CommandSender]
  ): ContextualExecutorBuilder[CS1, Args] = {
    val newSenderTypeValidation: SenderTypeValidation[CS1] = { sender =>
      val verificationProgram = for {
        refined1 <- OptionT(senderTypeValidation(sender))
        refined2: CS1 <- refined1 match {
          case refined1: CS1 => OptionT.pure[IO](refined1)
          case _             => OptionT[IO, Nothing](effectOnFail(sender).map(_ => None))
        }
      } yield refined2

      verificationProgram.value
    }

    val argumentsParser: CommandArgumentsParser[CS1, Args] = this.argumentsParser
    val contextualExecution: ScopedContextualExecution[CS1, Args] = this.contextualExecution

    ContextualExecutorBuilder(newSenderTypeValidation, argumentsParser, contextualExecution)
  }

  /**
   * ビルダーに入っている情報から[ContextualExecutor]を生成する.
   *
   * 生成された[ContextualExecutor]は,
   *
   *   - 先ず, 送信者が[CS]であるかを確認する
   *   - 次に, 引数のパースを試みる
   *   - 最後に, 変換された引数を用いて[ParsedArgCommandContext]を作成し, それを用いて[contextualExecution]で指定される動作を行う
   *
   * 処理を[ContextualExecutor.executionWith]内で行う.
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
  private def defaultArgumentParser[A]: CommandArgumentsParser[CommandSender, A] = {
    case (_, context) => IO.pure(Some(PartiallyParsedArgs(List(), context.args)))
  }
  private def defaultExecution[A]: ScopedContextualExecution[CommandSender, A] = { _ =>
    IO(emptyEffect)
  }
  private val defaultSenderValidation: SenderTypeValidation[CommandSender] = {
    sender: CommandSender => IO.pure(Some(sender))
  }

  def beginConfiguration[A](): ContextualExecutorBuilder[CommandSender, A] =
    ContextualExecutorBuilder(defaultSenderValidation, defaultArgumentParser, defaultExecution)
}
