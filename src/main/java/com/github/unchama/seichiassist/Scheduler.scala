package com.github.unchama.seichiassist

import kotlinx.coroutines.CoroutineDispatcher

object Schedulers {

  val sync: CoroutineDispatcher by lazy { SeichiAssist.instance.dispatcher() }

  val async: CoroutineDispatcher by lazy { SeichiAssist.instance.dispatcher(async = true) }

}
