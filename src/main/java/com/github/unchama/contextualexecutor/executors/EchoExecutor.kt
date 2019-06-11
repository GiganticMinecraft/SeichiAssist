package com.github.unchama.contextualexecutor.executors

import com.github.unchama.contextualexecutor.ContextualExecutor
import com.github.unchama.contextualexecutor.RawCommandContext
import com.github.unchama.messaging.MessageToSender

/**
 * 実行されたときに[messageToSender]を送り返すだけの[ContextualExecutor].
 */
class EchoExecutor(private val messageToSender: MessageToSender): ContextualExecutor {
  override suspend fun executeWith(rawContext: RawCommandContext): MessageToSender = messageToSender
}