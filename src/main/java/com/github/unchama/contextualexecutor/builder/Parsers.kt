package com.github.unchama.contextualexecutor.builder

import arrow.core.flatMap
import com.github.unchama.contextualexecutor.builder.ArgumentParserScope.ScopeProvider.parser
import com.github.unchama.contextualexecutor.builder.ArgumentParserScope.failWith
import com.github.unchama.contextualexecutor.builder.ArgumentParserScope.succeedWith
import com.github.unchama.contextualexecutor.builder.response.EmptyResponse
import com.github.unchama.contextualexecutor.builder.response.ResponseToSender

object Parsers {
  val identity: SingleArgumentParser = parser { succeedWith(it) }

  fun integer(failureMessage: ResponseToSender = EmptyResponse): SingleArgumentParser = {
    val parseResult = it.toIntOrNull()

    if (parseResult != null) succeedWith(parseResult) else failWith(failureMessage)
  }

  fun nonNegativeInteger(failureMessage: ResponseToSender = EmptyResponse): SingleArgumentParser = { arg ->
    integer(failureMessage)(arg).flatMap {
      val parsed = it as Int

      if (parsed > 0) succeedWith(it) else failWith(failureMessage)
    }
  }
}