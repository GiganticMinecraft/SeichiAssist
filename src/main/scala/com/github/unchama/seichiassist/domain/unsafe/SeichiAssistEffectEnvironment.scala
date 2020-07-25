package com.github.unchama.seichiassist.domain.unsafe

import cats.effect.Effect

trait SeichiAssistEffectEnvironment {

  def runEffectAsync[U, F[_] : Effect](context: String, program: F[U]): Unit

}
