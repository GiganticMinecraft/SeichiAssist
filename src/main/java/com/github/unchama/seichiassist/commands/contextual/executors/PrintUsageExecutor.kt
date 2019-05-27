package com.github.unchama.seichiassist.commands.contextual.executors

import arrow.effects.IO
import arrow.effects.extensions.io.fx.fx
import com.github.unchama.seichiassist.commands.contextual.ContextualExecutor
import com.github.unchama.seichiassist.commands.contextual.RawCommandContext

object PrintUsageExecutor: ContextualExecutor {
    override fun executionFor(rawContext: RawCommandContext): IO<Unit> =
            fx {
                !effect {
                    rawContext.sender.sendMessage(rawContext.command.command.usage)
                }
            }

}
