package com.github.unchama.seichiassist.commands.abstract

import arrow.core.Left
import arrow.core.None
import arrow.core.Right
import arrow.core.Some

object ArgumentParserScope {

    fun failWithoutError(): ResponseOrResult<Any> =
            Left(None)

    fun failWith(message: String): ResponseOrResult<Any> =
            Left(Some(message.asResponseToSender()))

    fun failWith(message: List<String>): ResponseOrResult<Any> =
            Left(Some(message.asResponseToSender()))

    fun succeedWith(result: Any): ResponseOrResult<Any> = Right(result)

    object ScopeProvider {
        fun parser(function: ArgumentParserScope.(String) -> ResponseOrResult<Any>): (String) -> ResponseOrResult<Any> =
                { argument -> ArgumentParserScope.function(argument) }
    }
}
