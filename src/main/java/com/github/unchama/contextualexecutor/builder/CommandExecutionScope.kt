package com.github.unchama.contextualexecutor.builder

import arrow.core.None
import arrow.core.Option
import arrow.effects.IO
import com.github.unchama.contextualexecutor.builder.response.ResponseToSender
import com.github.unchama.contextualexecutor.builder.response.asResponseToSender

/**
 * [ScopedContextualExecution<CS>]を作成する際のスコープを提供するオブジェクト.
 */
object CommandExecutionScope {
    /**
     * 何も[ResponseToSender]を含まないようなコマンドの応答を作成する.
     */
    fun returnNone(): IO<CommandResponse> = IO.just(None)

    /**
     * 単一の[String]を含むコマンドの応答を作成する.
     */
    fun returnMessage(message: String): IO<CommandResponse> = IO.just(Option.just(message.asResponseToSender()))
}
