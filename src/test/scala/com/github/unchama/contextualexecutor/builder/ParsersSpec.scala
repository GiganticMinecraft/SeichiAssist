package com.github.unchama.contextualexecutor.builder

import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import eu.timepit.refined.api.{Refined, Validate}
import eu.timepit.refined.numeric.{Interval, NonNegative, Positive}
import eu.timepit.refined.refineV
import org.bukkit.command.CommandSender
import org.scalatest.wordspec.AnyWordSpec

import java.lang.reflect.Proxy
import java.time.LocalDate
import scala.collection.mutable

/**
 * コマンド引数パーサの回帰テスト。
 *
 * Scala 3移行の際、コマンドDSLの内部実装(Shapeless HList)がTupleへ置き換えられ、
 * refinedの利用方法も変更される予定であるため、現行のパース挙動をここで固定する。
 *
 * 注意: [[Parsers.closedRangeInt]] は、refinedの述語検証に失敗した場合には引数で渡した
 * 失敗エフェクトではなく、refineVが生成する英語のエラーメッセージ
 * (例: `Predicate (-1 < 0) did not fail.`) をそのまま送信者へ送る。
 * 引数の失敗エフェクトが使われるのは、整数としてパースできない場合と、
 * 述語検証には通るが指定範囲外である場合のみである。このテストはその現行挙動を固定する。
 */
class ParsersSpec extends AnyWordSpec {

  /**
   * [[CommandSender]] へ送られたメッセージを記録しながらeffectを実行し、記録を返す。
   */
  private def messagesSentBy(effect: TargetedEffect[CommandSender]): List[String] = {
    val messages = mutable.ListBuffer[String]()
    val recordingSender = Proxy
      .newProxyInstance(
        classOf[CommandSender].getClassLoader,
        Array(classOf[CommandSender]),
        (_, method, args) => {
          if (method.getName == "sendMessage" && args != null && args.length == 1) {
            args(0) match {
              case message: String                 => messages += message
              case lines: Array[String @unchecked] => messages ++= lines
              case _                               =>
            }
          }
          null
        }
      )
      .asInstanceOf[CommandSender]

    effect.run(recordingSender).unsafeRunSync()
    messages.toList
  }

  private def assertFailureEffectSends(
    result: ResponseEffectOrResult[CommandSender, _],
    expectedMessage: String
  ): Unit = {
    val effect = result.swap.getOrElse(fail("パース結果はLeftであるべきです"))
    assert(messagesSentBy(effect) == List(expectedMessage))
    ()
  }

  /**
   * refineVが失敗時に生成するエラーメッセージ。
   */
  private def refinedErrorMessageOf[P](int: Int)(implicit validate: Validate[Int, P]): String =
    refineV[P](int).swap.getOrElse(fail(s"refineVが${int}の検証に成功してしまいました"))

  "Parsers.identity" should {
    "入力文字列をそのまま返す" in {
      assert(Parsers.identity("abc") == Right("abc"))
      assert(Parsers.identity("") == Right(""))
      assert(Parsers.identity("日本語の引数") == Right("日本語の引数"))
    }
  }

  "Parsers.integer" should {
    "整数をパースできる" in {
      assert(Parsers.integer()("42") == Right(42))
      assert(Parsers.integer()("-1") == Right(-1))
      assert(Parsers.integer()("0") == Right(0))
    }

    "Intの境界値をパースできる" in {
      assert(Parsers.integer()(Int.MaxValue.toString) == Right(Int.MaxValue))
      assert(Parsers.integer()(Int.MinValue.toString) == Right(Int.MinValue))
    }

    "Intの範囲外や非数値の入力では失敗し、指定した失敗エフェクトを返す" in {
      val parser = Parsers.integer(MessageEffect("整数を指定してください。"))

      assertFailureEffectSends(parser("abc"), "整数を指定してください。")
      assertFailureEffectSends(parser("3.5"), "整数を指定してください。")
      assertFailureEffectSends(parser((Int.MaxValue.toLong + 1).toString), "整数を指定してください。")
      assertFailureEffectSends(parser(""), "整数を指定してください。")
    }
  }

  "Parsers.double" should {
    "小数をパースできる" in {
      assert(Parsers.double()("3.5") == Right(3.5))
      assert(Parsers.double()("-0.25") == Right(-0.25))
      assert(Parsers.double()("10") == Right(10.0))
    }

    "非数値の入力では失敗する" in {
      assertFailureEffectSends(
        Parsers.double(MessageEffect("小数を指定してください。"))("abc"),
        "小数を指定してください。"
      )
    }
  }

  "Parsers.nonNegativeInteger" should {
    "0と正数を受理し、refinedの値として返す" in {
      val parser = Parsers.nonNegativeInteger()

      assert(parser("0").map(_.value) == Right(0))
      assert(parser("15").map(_.value) == Right(15))
      assert(parser(Int.MaxValue.toString).map(_.value) == Right(Int.MaxValue))
    }

    "負数は述語検証で拒否され、refinedのエラーメッセージが送られる" in {
      val parser = Parsers.nonNegativeInteger(MessageEffect("非負整数を指定してください。"))

      assertFailureEffectSends(parser("-1"), refinedErrorMessageOf[NonNegative](-1))
    }
  }

  "Parsers.closedRangeInt" should {
    // 本体コードで実際に使われている3種のrefined型で挙動を固定する
    "Int Refined Positive: 範囲内の値を受理する" in {
      val parser =
        Parsers.closedRangeInt[Int Refined Positive](1, 100, MessageEffect("範囲外です。"))

      assert(parser("1").map(_.value) == Right(1))
      assert(parser("100").map(_.value) == Right(100))
    }

    "Int Refined Positive: 述語違反はrefinedのエラー、述語成立かつ範囲外は指定エフェクトで拒否する" in {
      val parser =
        Parsers.closedRangeInt[Int Refined Positive](1, 100, MessageEffect("範囲外です。"))

      // 0はPositive述語の検証で失敗するため、refinedのメッセージが送られる
      assertFailureEffectSends(parser("0"), refinedErrorMessageOf[Positive](0))
      // 101はPositive述語を通過し、範囲チェックで失敗するため、指定したメッセージが送られる
      assertFailureEffectSends(parser("101"), "範囲外です。")
    }

    "Int Refined NonNegative: 0を受理し、負数を拒否する" in {
      val parser = Parsers
        .closedRangeInt[Int Refined NonNegative](0, Int.MaxValue, MessageEffect("範囲外です。"))

      assert(parser("0").map(_.value) == Right(0))
      assertFailureEffectSends(parser("-1"), refinedErrorMessageOf[NonNegative](-1))
    }

    "Int Refined Interval.Closed: 区間の両端を受理し、区間外を拒否する" in {
      val parser = Parsers.closedRangeInt[Int Refined Interval.Closed[1, 64]](
        1,
        64,
        MessageEffect("スタック数は1から64で指定してください。")
      )

      assert(parser("1").map(_.value) == Right(1))
      assert(parser("64").map(_.value) == Right(64))
      // 区間外の値は範囲チェックより先に述語検証で失敗するため、refinedのメッセージが送られる
      assertFailureEffectSends(parser("0"), refinedErrorMessageOf[Interval.Closed[1, 64]](0))
      assertFailureEffectSends(parser("65"), refinedErrorMessageOf[Interval.Closed[1, 64]](65))
    }
  }

  "Parsers.fromOptionParser" should {
    "Someを返す関数からRightを得る" in {
      val parser = Parsers.fromOptionParser((s: String) => Option.when(s == "give")(s))

      assert(parser("give") == Right("give"))
    }

    "Noneを返す関数から失敗エフェクトを得る" in {
      val parser = Parsers.fromOptionParser(
        (s: String) => Option.when(s == "give")(s),
        MessageEffect("操作が不正です。")
      )

      assertFailureEffectSends(parser("take"), "操作が不正です。")
    }
  }

  "Parsers.hyphenatedDate" should {
    "yyyy-MM-dd形式の日付をパースできる" in {
      assert(Parsers.hyphenatedDate()("2026-07-20") == Right(LocalDate.of(2026, 7, 20)))
    }

    "不正な形式の日付では失敗する" in {
      val parser = Parsers.hyphenatedDate(MessageEffect("日付はyyyy-MM-dd形式で指定してください。"))

      assertFailureEffectSends(parser("2026/07/20"), "日付はyyyy-MM-dd形式で指定してください。")
      assertFailureEffectSends(parser("2026-13-01"), "日付はyyyy-MM-dd形式で指定してください。")
      assertFailureEffectSends(parser("not-a-date"), "日付はyyyy-MM-dd形式で指定してください。")
    }
  }
}
