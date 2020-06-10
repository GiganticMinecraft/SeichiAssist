package com.github.unchama.contextualexecutor.builder

import com.github.unchama.targetedeffect.TargetedEffect.emptyEffect
import com.github.unchama.targetedeffect._
import com.github.unchama.targetedeffect.player.MessageEffect
import org.bukkit.command.CommandSender

/**
 * 文字列から`ResponseEffectOrResult[Any]`への関数の作成を行うためのオブジェクト.
 *
 * [ArgumentParserScope.ScopeProvider.parser]を通してスコープ付き関数をそのような関数に変換できる.
 */
// TODO update comment
object ArgumentParserScope {
  /**
   * メッセージなしで「失敗」を表す[ResponseEffectOrResult]を作成する.
   */
  def failWithoutError(): ResponseEffectOrResult[Any, Nothing] = failWith(emptyEffect)

  /**
   * メッセージ付きの「失敗」を表す[ResponseEffectOrResult]を作成する.
   */
  def failWith(message: String): ResponseEffectOrResult[CommandSender, Nothing] = failWith(MessageEffect(message))

  def failWith[CS](effect: TargetedEffect[CS]): ResponseEffectOrResult[CS, Nothing] = Left(effect)

  /**
   * メッセージ付きの「失敗」を表す[ResponseEffectOrResult]を作成する.
   */
  def failWith(message: List[String]): ResponseEffectOrResult[CommandSender, Any] = failWith(MessageEffect(message))

  /**
   * [result]により「成功」したことを示す[ResponseEffectOrResult]を作成する.
   */
  def succeedWith(result: Any): ResponseEffectOrResult[Any, Any] = Right(result)
}
