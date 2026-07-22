package com.github.unchama.contextualexecutor.builder

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.github.unchama.contextualexecutor.{
  ContextualExecutor,
  ExecutedCommand,
  RawCommandContext
}
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.command.{Command, CommandSender}
import org.bukkit.entity.Player
import org.scalamock.scalatest.MockFactory
import org.scalatest.wordspec.AnyWordSpec

/**
 * コマンド引数DSL ([[ContextualExecutorBuilder]]) の回帰テスト。
 *
 * Scala 2時代はShapeless HListで実装されていた引数型の蓄積がScala 3のTupleへ置き換えられたため、
 * 引数のパース順序、エラー通知、送信者の絞り込みといった外部から観測できる挙動をここで固定する。
 */
class ContextualExecutorBuilderSpec extends AnyWordSpec with MockFactory {

  private val dummyCommand: Command = new Command("dummy") {
    override def execute(sender: CommandSender, label: String, args: Array[String]): Boolean =
      true
  }

  private def contextWith(sender: CommandSender, args: List[String]): RawCommandContext =
    RawCommandContext(sender, ExecutedCommand(dummyCommand, "dummy"), args)

  private class RecordingExecutor extends ContextualExecutor {
    var recordedContext: Option[RawCommandContext] = None

    override def executionWith(context: RawCommandContext): IO[Unit] =
      IO { recordedContext = Some(context) }
  }

  "引数を取らないビルダー" should {
    "空のタプルと未パース引数をそのまま実行部へ渡す" in {
      val sender = stub[CommandSender]
      var capturedParsed: Option[Tuple] = None
      var capturedYetToBeParsed: Option[List[String]] = None

      val executor = ContextualExecutorBuilder.beginConfiguration.buildWith { context =>
        IO {
          capturedParsed = Some(context.args.parsed)
          capturedYetToBeParsed = Some(context.args.yetToBeParsed)
        }.as(TargetedEffect.emptyEffect)
      }

      executor.executionWith(contextWith(sender, List("raw1", "raw2"))).unsafeRunSync()

      assert(capturedParsed.contains(EmptyTuple))
      assert(capturedYetToBeParsed.contains(List("raw1", "raw2")))
    }
  }

  "thenParseを使ったビルダー" should {
    "正常な引数をパースしてタプルとして実行部へ渡す" in {
      val sender = stub[CommandSender]
      var captured: Option[Tuple] = None

      val executor =
        ContextualExecutorBuilder.beginConfiguration.thenParse(Parsers.integer()).buildWith {
          context => IO { captured = Some(context.args.parsed) }.as(TargetedEffect.emptyEffect)
        }

      executor.executionWith(contextWith(sender, List("42"))).unsafeRunSync()

      assert(captured.contains(42 *: EmptyTuple))
    }

    "複数の引数を宣言順にパースし、余剰引数はyetToBeParsedに残す" in {
      val sender = stub[CommandSender]
      var capturedParsed: Option[Tuple] = None
      var capturedYetToBeParsed: Option[List[String]] = None

      val executor = ContextualExecutorBuilder
        .beginConfiguration
        .thenParse(Parsers.identity)
        .thenParse(Parsers.integer())
        .buildWith { context =>
          IO {
            capturedParsed = Some(context.args.parsed)
            capturedYetToBeParsed = Some(context.args.yetToBeParsed)
          }.as(TargetedEffect.emptyEffect)
        }

      executor
        .executionWith(contextWith(sender, List("name", "7", "rest1", "rest2")))
        .unsafeRunSync()

      assert(capturedParsed.contains(("name", 7)))
      assert(capturedYetToBeParsed.contains(List("rest1", "rest2")))
    }

    "パースに失敗した場合、実行部を呼ばず失敗エフェクトを送信者へ送る" in {
      val sender = stub[CommandSender]
      var executed = false

      val executor = ContextualExecutorBuilder
        .beginConfiguration
        .thenParse(Parsers.integer(MessageEffect("整数を指定してください。")))
        .buildWith { _ => IO { executed = true }.as(TargetedEffect.emptyEffect) }

      executor.executionWith(contextWith(sender, List("abc"))).unsafeRunSync()

      assert(!executed)
      (sender.sendMessage(_: String)).verify("整数を指定してください。")
    }

    "引数が不足した場合、既定では何もせず実行部も呼ばない" in {
      val sender = stub[CommandSender]
      var executed = false

      val executor =
        ContextualExecutorBuilder.beginConfiguration.thenParse(Parsers.integer()).buildWith {
          _ => IO { executed = true }.as(TargetedEffect.emptyEffect)
        }

      executor.executionWith(contextWith(sender, List())).unsafeRunSync()

      assert(!executed)
      (sender.sendMessage(_: String)).verify(*).never()
    }

    "引数が不足した場合、ifArgumentsMissingで指定したExecutorが元のコンテキストで実行される" in {
      val sender = stub[CommandSender]
      val missingArgumentsHandler = new RecordingExecutor
      var executed = false

      val executor = ContextualExecutorBuilder
        .beginConfiguration
        .ifArgumentsMissing(missingArgumentsHandler)
        .thenParse(Parsers.integer())
        .buildWith { _ => IO { executed = true }.as(TargetedEffect.emptyEffect) }

      val rawContext = contextWith(sender, List())
      executor.executionWith(rawContext).unsafeRunSync()

      assert(!executed)
      assert(missingArgumentsHandler.recordedContext.contains(rawContext))
    }
  }

  "refineSenderWithErrorを使ったビルダー" should {
    "送信者が要求された型であれば実行部を呼ぶ" in {
      val playerSender = stub[Player]
      var executed = false

      val executor = ContextualExecutorBuilder
        .beginConfiguration
        .refineSenderWithError[Player]("このコマンドはゲーム内から実行してください。")
        .buildWith { _ => IO { executed = true }.as(TargetedEffect.emptyEffect) }

      executor.executionWith(contextWith(playerSender, List())).unsafeRunSync()

      assert(executed)
    }

    "送信者が要求された型でなければエラーメッセージを送り、実行部を呼ばない" in {
      val consoleSender = stub[CommandSender]
      var executed = false

      val executor = ContextualExecutorBuilder
        .beginConfiguration
        .refineSenderWithError[Player]("このコマンドはゲーム内から実行してください。")
        .buildWith { _ => IO { executed = true }.as(TargetedEffect.emptyEffect) }

      executor.executionWith(contextWith(consoleSender, List())).unsafeRunSync()

      assert(!executed)
      (consoleSender.sendMessage(_: String)).verify("このコマンドはゲーム内から実行してください。")
    }

    "型の絞り込みと引数パースを組み合わせられる" in {
      val playerSender = stub[Player]
      var captured: Option[Tuple] = None

      val executor = ContextualExecutorBuilder
        .beginConfiguration
        .refineSenderWithError[Player]("このコマンドはゲーム内から実行してください。")
        .thenParse(Parsers.identity)
        .buildWith { context =>
          IO { captured = Some(context.args.parsed) }.as(TargetedEffect.emptyEffect)
        }

      executor.executionWith(contextWith(playerSender, List("arg"))).unsafeRunSync()

      assert(captured.contains("arg" *: EmptyTuple))
    }
  }
}
