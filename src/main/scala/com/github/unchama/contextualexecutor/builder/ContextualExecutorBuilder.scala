package com.github.unchama.contextualexecutor.builder

import cats.data.{Kleisli, OptionT}
import cats.effect.{Effect, IO}
import com.github.unchama.contextualexecutor.{
  ContextualExecutor,
  ParsedArgCommandContext,
  PartiallyParsedArgs,
  RawCommandContext
}
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.command.CommandSender
import shapeless.ops.hlist.Prepend
import shapeless.{HList, HNil, :: => HCons}

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
 */
// TODO(scala3): HListをTupleに置き換える。Shapelessで実装された同等の操作がネイティブに (`Tuple.Map` などを通じて) サポートされている。
case class ContextualExecutorBuilder[CS <: CommandSender, HArgs <: HList](
  senderTypeValidation: SenderTypeValidation[CS],
  argumentsParser: CommandArgumentsParser[CS, HArgs],
  onMissingArguments: Option[ContextualExecutor] = None
) {
  import cats.implicits._

  /**
   * 引数を追加で受け取る。追加された引数は[[HArgs]]の末尾に追加され、新しいContextualExecutionBuilderが返される。
   */
  def thenParse[LastArg](parserToBeAdded: SingleArgumentParser[LastArg])(
    implicit prepend: Prepend[HArgs, HCons[LastArg, HNil]]
  ): ContextualExecutorBuilder[CS, prepend.Out] = {
    this.copy(argumentsParser = (sender, rawContext) => {
      val program = for {
        head <- OptionT(this.argumentsParser(sender, rawContext))
        nextArgument <- head.yetToBeParsed.headOption match {
          case Some(value) => OptionT.some[IO](value)
          case None =>
            OptionT.liftF(onMissingArguments match {
              case Some(onMissingArguments) => onMissingArguments.executionWith(rawContext)
              case None                     => IO.unit
            }) >> OptionT.none[IO, String]
        }
        parsedLastArg <- parserToBeAdded(nextArgument) match {
          case Left(errorNotification) =>
            OptionT.liftF(errorNotification(sender)) >> OptionT.none[IO, LastArg]
          case Right(lastArg) => OptionT.pure[IO](lastArg)
        }
      } yield {
        val parsedArguments = (head.parsed :+ parsedLastArg)(prepend)
        PartiallyParsedArgs(parsedArguments, head.yetToBeParsed.tail)
      }

      program.value
    })
  }

  /**
   * 引数が足りなかったときにエラーを通知するContextualExecutorを上書き設定する。
   * @param errorNotifier エラーを知らせる[[ContextualExecutor]]
   * @return 引数で指定されたエラーを通知するContextualExecutorが設定された新しいビルダー
   */
  def ifMissingArguments(
    errorNotifier: ContextualExecutor
  ): ContextualExecutorBuilder[CS, HArgs] = this.copy(onMissingArguments = Some(errorNotifier))

  /**
   * @return
   *   [CS]を[CS1]へ狭めるキャストを試み, 失敗すると[message]がエラーメッセージとして返る[senderTypeValidation]が入った
   *   新しい[ContextualExecutorBuilder]
   */
  def refineSenderWithError[CS1 <: CS: ClassTag](
    message: String
  ): ContextualExecutorBuilder[CS1, HArgs] =
    refineSender(MessageEffect(message))

  /**
   * @return
   *   [[CS]]を[[CS1]]へ狭めるキャストを試み, 失敗すると[messages]がエラーメッセージとして返る[senderTypeValidation]が入った
   *   新しい[ContextualExecutorBuilder]
   */
  def refineSenderWithError[CS1 <: CS: ClassTag](
    messages: List[String]
  ): ContextualExecutorBuilder[CS1, HArgs] =
    refineSender(MessageEffect(messages))

  /**
   * @return
   *   [[CS]]を[[CS1]]へ狭めるキャストを試み, 失敗したら[errorMessageOnFail]が返るような[senderTypeValidation]が入った
   *   新しい[ContextualExecutorBuilder]
   */
  def refineSender[CS1 <: CS: ClassTag](
    effectOnFail: TargetedEffect[CommandSender]
  ): ContextualExecutorBuilder[CS1, HArgs] = {
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

    val argumentsParser: CommandArgumentsParser[CS1, HArgs] = this.argumentsParser
    ContextualExecutorBuilder(newSenderTypeValidation, argumentsParser)
  }

  /**
   * ビルダーに入っている情報と与えられた [[ScopedContextualExecution]] からと [[ContextualExecutor]] を生成する.
   *
   * [[ContextualExecutor]]の制約にあるとおり, [contextualExecution] は任意スレッドからの呼び出しに対応しなければならない.
   *
   * 生成された[ContextualExecutor]は,
   *   - 先ず, 送信者が[[CS]]であるかを確認する
   *   - 次に, 引数のパースを試みる
   *   - 最後に, 変換された引数を用いて[[ParsedArgCommandContext]]を作成し, それを用いて[contextualExecution]で指定される動作を行う
   * 処理を[[ContextualExecutor.executionWith]]内で行う.
   */
  def buildWith(contextualExecution: ScopedContextualExecution[CS, HArgs]): ContextualExecutor =
    (rawContext: RawCommandContext) => {
      val optionalExecution = for {
        refinedSender <- OptionT(senderTypeValidation(rawContext.sender))
        parsedArgs <- OptionT(argumentsParser(refinedSender, rawContext))
        context = ParsedArgCommandContext(refinedSender, rawContext.command, parsedArgs)
        executionResponse <- OptionT.liftF(contextualExecution(context))
        _ <- OptionT.liftF(executionResponse(context.sender))
      } yield ()

      optionalExecution.value.map(_ => ())
    }

  /**
   * [contextualExecution]に[execution]に相当する関数が入った新しい[ContextualExecutorBuilder]を作成する.
   * ここで、`execution` はコンテキストを受け取って作用を起こすようなプログラムである.
   *
   * [ContextualExecutor]の制約にあるとおり, [execution]は任意スレッドでの実行に対応しなければならない.
   */
  def buildWithExecutionF[F[_]: Effect, U](
    execution: ExecutionF[F, CS, U, HArgs]
  ): ContextualExecutor =
    buildWith(context => {
      Effect[F].toIO(execution(context)).as(TargetedEffect.emptyEffect)
    })

  /**
   * [contextualExecution]に[execution]に相当する関数が入った新しい[ContextualExecutorBuilder]を作成する.
   * ここで、`execution` はコンテキストを受け取って、 コマンド実行者に対する作用(`Kleisli[F, CS, U]`) を起こすようなプログラムである.
   *
   * [[ContextualExecutor]]の制約にあるとおり, `execution` は任意スレッドからの呼び出しに対応しなければならない.
   */
  def buildWithExecutionCSEffect[F[_]: Effect, U](
    execution: ExecutionCSEffect[F, CS, U, HArgs]
  ): ContextualExecutor =
    buildWithExecutionF[F, U](context => execution(context).run(context.sender))

  /**
   * コンテキストを利用せずに動作する `effect` が入った [[ContextualExecutor]] を作成する.
   *
   * [[ContextualExecutor]]の制約にあるとおり, `effect` は任意スレッドからの呼び出しに対応しなければならない.
   */
  def buildWithEffectAsExecution[U](effect: Kleisli[IO, CS, U]): ContextualExecutor =
    buildWith(_ => IO.pure(effect.map(_ => ())))

  /**
   * コンテキストを利用せずに動作する `io` が入った [[ContextualExecutor]] を作成する.
   *
   * [[ContextualExecutor]]の制約にあるとおり, `io` は任意スレッドからの呼び出しに対応しなければならない.
   */
  def buildWithIOAsExecution[U](io: IO[U]): ContextualExecutor =
    buildWithEffectAsExecution(Kleisli(_ => io))
}

object ContextualExecutorBuilder {
  private def defaultArgumentParser: CommandArgumentsParser[CommandSender, HNil] = {
    case (_, context) => IO.pure(Some(PartiallyParsedArgs(HNil, context.args)))
  }

  private val defaultSenderValidation: SenderTypeValidation[CommandSender] = {
    sender: CommandSender => IO.pure(Some(sender))
  }

  def beginConfiguration: ContextualExecutorBuilder[CommandSender, HNil] =
    ContextualExecutorBuilder(defaultSenderValidation, defaultArgumentParser)
}
