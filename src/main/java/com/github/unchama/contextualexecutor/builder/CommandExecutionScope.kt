package com.github.unchama.contextualexecutor.builder

import arrow.core.None
import arrow.core.Some
import com.github.unchama.contextualexecutor.builder.response.asResponseToSender

/**
 * [ScopedContextualExecution<CS>]を作成する際のスコープを提供するオブジェクト.
 */
object CommandExecutionScope {
  /**
   * 単一の[String]を含むコマンドの応答を作成する.
   */
  fun returnMessage(message: String): CommandResponse = Some(message.asResponseToSender())

  /**
   * メッセージを複数個含むコマンドの応答を作成する.
   */
  fun returnMessages(messages: List<String>): CommandResponse = Some(messages.asResponseToSender())

  /**
   * メッセージを含まないコマンドの応答を作成する.
   */
  fun returnNone(): CommandResponse = None
}
