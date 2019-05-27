package com.github.unchama.contextualexecutor.executors

import arrow.effects.IO
import arrow.effects.extensions.io.fx.fx
import com.github.unchama.contextualexecutor.ContextualExecutor
import com.github.unchama.contextualexecutor.RawCommandContext

object PrintUsageExecutor: ContextualExecutor {
    override fun executionFor(rawContext: RawCommandContext): IO<Unit> =
            fx {
                !effect {
                    rawContext.sender.sendMessage(rawContext.command.command.usage)
                }
            }

}
