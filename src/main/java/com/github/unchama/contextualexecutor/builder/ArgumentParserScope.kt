package com.github.unchama.contextualexecutor.builder

import arrow.core.Left
import arrow.core.Right
import com.github.unchama.effect.EmptyMessage
import com.github.unchama.effect.MessageToSender
import com.github.unchama.effect.asResponseToSender

/**
 * [ContextualExecutorBuilder.argumentsParser]が要求する,
 * 引数文字列から[ResponseOrResult<Any>]への関数の作成を行うためのスコープオブジェクト.
 *
 * [ArgumentParserScope.ScopeProvider.parser]を通してスコープ付き関数をそのような関数に変換できる.
 */
object ArgumentParserScope {
  fun failWith(message: MessageToSender): ResponseOrResult<Nothing> = Left(message)

  /**
   * メッセージなしで「失敗」を表す[ResponseOrResult]を作成する.
   */
  fun failWithoutError(): ResponseOrResult<Nothing> = failWith(EmptyMessage)

  /**
   * メッセージ付きの「失敗」を表す[ResponseOrResult]を作成する.
   */
  fun failWith(message: String): ResponseOrResult<Nothing> = failWith(message.asResponseToSender())

  /**
   * メッセージ付きの「失敗」を表す[ResponseOrResult]を作成する.
   */
  fun failWith(message: List<String>): ResponseOrResult<Any> = failWith(message.asResponseToSender())

  /**
   * [result]により「成功」したことを示す[ResponseOrResult]を作成する.
   */
  fun succeedWith(result: Any): ResponseOrResult<Any> = Right(result)

  object ScopeProvider {
    /**
     * [ArgumentParserScope]のスコープ付き関数をプレーンな関数へと変換する.
     */
    fun parser(parse: ArgumentParserScope.(String) -> ResponseOrResult<Any>): (String) -> ResponseOrResult<Any> =
        { argument -> with(ArgumentParserScope) { parse(argument) } }
  }
}
