package com.github.unchama.contextualexecutor.builder

/**
 * [ContextualExecutorBuilder.argumentsParser]が要求する,
 * 引数文字列から[ResponseEffectOrResult[Any]]への関数の作成を行うためのスコープオブジェクト.
 *
 * [ArgumentParserScope.ScopeProvider.parser]を通してスコープ付き関数をそのような関数に変換できる.
 */
object ArgumentParserScope {
  def failWith[CS](message: TargetedEffect[CS]): ResponseEffectOrResult[CS, Nothing] = Left(message)

  /**
   * メッセージなしで「失敗」を表す[ResponseEffectOrResult]を作成する.
   */
  def failWithoutError(): ResponseEffectOrResult[Any?, Nothing] = failWith(EmptyEffect)

  /**
   * メッセージ付きの「失敗」を表す[ResponseEffectOrResult]を作成する.
   */
  def failWith(message: String): ResponseEffectOrResult[CommandSender, Nothing] = failWith(message.asMessageEffect())

  /**
   * メッセージ付きの「失敗」を表す[ResponseEffectOrResult]を作成する.
   */
  def failWith(message: List[String]): ResponseEffectOrResult[CommandSender, Any] = failWith(message.asMessageEffect())

  /**
   * [result]により「成功」したことを示す[ResponseEffectOrResult]を作成する.
   */
  def succeedWith(result: Any): ResponseEffectOrResult[Any?, Any] = Right(result)

  object ScopeProvider {
    /**
     * [ArgumentParserScope]のスコープ付き関数をプレーンな関数へと変換する.
     */
    def parser[CS](
        parse: ArgumentParserScope.(String) => ResponseEffectOrResult[CS, Any]
    ): (String) => ResponseEffectOrResult[CS, Any] = { argument => with(ArgumentParserScope) { parse(argument) } }
  }
}
