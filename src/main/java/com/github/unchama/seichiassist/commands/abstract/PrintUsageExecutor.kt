package com.github.unchama.seichiassist.commands.abstract

import arrow.effects.IO
import arrow.effects.extensions.io.fx.fx

object PrintUsageExecutor: ContextualExecutor {
    override fun executionFor(rawContext: RawCommandContext): IO<Unit> =
            fx {
                !effect {
                    rawContext.sender.sendMessage(rawContext.command.command.usage)
                }
            }

}