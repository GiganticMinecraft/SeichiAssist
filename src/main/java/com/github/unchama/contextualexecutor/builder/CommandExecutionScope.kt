package com.github.unchama.contextualexecutor.builder

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
}
