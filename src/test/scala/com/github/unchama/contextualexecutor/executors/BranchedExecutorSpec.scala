package com.github.unchama.contextualexecutor.executors

import cats.effect.IO
import com.github.unchama.contextualexecutor.{
  ContextualExecutor,
  ExecutedCommand,
  RawCommandContext
}
import org.bukkit.command.{Command, CommandSender}
import org.scalamock.scalatest.MockFactory
import org.scalatest.wordspec.AnyWordSpec

/**
 * サブコマンドの分岐とタブ補完を担う [[BranchedExecutor]] の回帰テスト。
 *
 * ほぼすべてのコマンドがサブコマンドのルーティングにこのExecutorを利用しているため、
 * Scala 3移行前に分岐解決とタブ補完候補の挙動を固定する。
 */
class BranchedExecutorSpec extends AnyWordSpec with MockFactory {

  private val dummyCommand: Command = new Command("dummy") {
    override def execute(sender: CommandSender, label: String, args: Array[String]): Boolean =
      true
  }

  private def contextWith(args: List[String]): RawCommandContext =
    RawCommandContext(stub[CommandSender], ExecutedCommand(dummyCommand, "dummy"), args)

  private class RecordingExecutor(tabCandidates: List[String] = Nil)
      extends ContextualExecutor {
    var recordedExecutionContext: Option[RawCommandContext] = None
    var recordedTabContext: Option[RawCommandContext] = None

    override def executionWith(context: RawCommandContext): IO[Unit] =
      IO { recordedExecutionContext = Some(context) }

    override def tabCandidatesFor(context: RawCommandContext): List[String] = {
      recordedTabContext = Some(context)
      tabCandidates
    }
  }

  "executionWith" should {
    "第一引数に対応する分岐を、その引数を除いたコンテキストで実行する" in {
      val giveBranch = new RecordingExecutor
      val listBranch = new RecordingExecutor
      val executor = BranchedExecutor(Map("give" -> giveBranch, "list" -> listBranch))

      executor.executionWith(contextWith(List("give", "player1", "3"))).unsafeRunSync()

      assert(giveBranch.recordedExecutionContext.exists(_.args == List("player1", "3")))
      assert(listBranch.recordedExecutionContext.isEmpty)
    }

    "引数が空の場合はwhenArgInsufficientを元のコンテキストで実行する" in {
      val branch = new RecordingExecutor
      val insufficientHandler = new RecordingExecutor
      val executor =
        BranchedExecutor(Map("give" -> branch), whenArgInsufficient = Some(insufficientHandler))

      val context = contextWith(List())
      executor.executionWith(context).unsafeRunSync()

      assert(branch.recordedExecutionContext.isEmpty)
      assert(insufficientHandler.recordedExecutionContext.contains(context))
    }

    "分岐が見つからない場合はwhenBranchNotFoundを元のコンテキストで実行する" in {
      val branch = new RecordingExecutor
      val notFoundHandler = new RecordingExecutor
      val executor =
        BranchedExecutor(Map("give" -> branch), whenBranchNotFound = Some(notFoundHandler))

      val context = contextWith(List("unknown", "arg"))
      executor.executionWith(context).unsafeRunSync()

      assert(branch.recordedExecutionContext.isEmpty)
      assert(notFoundHandler.recordedExecutionContext.contains(context))
    }

    "ハンドラがNoneの場合は何も実行しない" in {
      val branch = new RecordingExecutor
      val executor = BranchedExecutor(
        Map("give" -> branch),
        whenArgInsufficient = None,
        whenBranchNotFound = None
      )

      executor.executionWith(contextWith(List())).unsafeRunSync()
      executor.executionWith(contextWith(List("unknown"))).unsafeRunSync()

      assert(branch.recordedExecutionContext.isEmpty)
    }
  }

  "tabCandidatesFor" should {
    "引数が空の場合、分岐名のソート済みリストを返す" in {
      val executor = BranchedExecutor(
        Map(
          "list" -> new RecordingExecutor,
          "give" -> new RecordingExecutor,
          "get" -> new RecordingExecutor
        )
      )

      assert(executor.tabCandidatesFor(contextWith(List())) == List("get", "give", "list"))
    }

    "第一引数に対応する分岐がある場合、その分岐へ残りの引数で委譲する" in {
      val giveBranch = new RecordingExecutor(tabCandidates = List("player1", "player2"))
      val executor = BranchedExecutor(Map("give" -> giveBranch))

      val candidates = executor.tabCandidatesFor(contextWith(List("give", "pl")))

      assert(candidates == List("player1", "player2"))
      assert(giveBranch.recordedTabContext.exists(_.args == List("pl")))
    }

    "対応する分岐がない場合は空リストを返す" in {
      val executor = BranchedExecutor(Map("give" -> new RecordingExecutor))

      assert(executor.tabCandidatesFor(contextWith(List("unknown", "x"))) == Nil)
    }
  }
}
