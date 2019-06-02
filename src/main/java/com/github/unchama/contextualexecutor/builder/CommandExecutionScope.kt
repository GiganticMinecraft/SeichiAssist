package com.github.unchama.contextualexecutor.builder

import com.github.unchama.contextualexecutor.builder.response.EmptyResponse
import com.github.unchama.contextualexecutor.builder.response.ResponseToSender
import com.github.unchama.contextualexecutor.builder.response.asResponseToSender

/**
 * [ScopedContextualExecution<CS>]を作成する際のスコープを提供するオブジェクト.
 */
object CommandExecutionScope {
  /**
   * 単一の[String]を含むコマンドの応答を作成する.
   */
  @Deprecated("", replaceWith = ReplaceWith("asResponseToSender"))
  fun returnMessage(message: String): ResponseToSender = message.asResponseToSender()

  /**
   * メッセージを複数個含むコマンドの応答を作成する.
   */
  @Deprecated("", replaceWith = ReplaceWith("asResponseToSender"))
  fun returnMessages(messages: List<String>): ResponseToSender = messages.asResponseToSender()

  /**
   * メッセージを含まないコマンドの応答を作成する.
   */
  @Deprecated("", replaceWith = ReplaceWith("EmptyResponse"))
  fun returnNone(): ResponseToSender = EmptyResponse
}
