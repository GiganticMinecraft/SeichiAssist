package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.AppleOpenState._

object AppleOpenStateDependency {

  val dependency: Map[AppleOpenState, AppleOpenState] =
    Map(OpenAnyway -> Open, Open -> OpenALittle, OpenALittle -> NotOpen, NotOpen -> OpenAnyway)

}
