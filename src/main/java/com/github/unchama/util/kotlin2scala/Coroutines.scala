package com.github.unchama.util.kotlin2scala

import kotlin.coroutines.{Continuation, CoroutineContext, EmptyCoroutineContext}
import kotlinx.coroutines._

object Coroutines {
  def launchInGlobalScope(context: CoroutineContext = EmptyCoroutineContext.INSTANCE,
                          start: CoroutineStart = CoroutineStart.DEFAULT,
                          block: (CoroutineScope, Continuation[Unit]) => Unit): Job = {
    BuildersKt.launch(
      GlobalScope.INSTANCE, context, start,
      new kotlin.jvm.functions.Function2[CoroutineScope, Continuation[kotlin.Unit], Unit] {
        override @SuspendingMethod def invoke(p1: CoroutineScope): Unit = block(p1, p2)
      }.asInstanceOf[kotlin.jvm.functions.Function2[_ >: CoroutineScope, _ >: Continuation[_ >: kotlin.Unit], Unit]]
    )
  }
}
