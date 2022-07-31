package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyAppleConsumeStrategy._

object AppleOpenStateDependency {

  val dependency: Map[FairyAppleConsumeStrategy, FairyAppleConsumeStrategy] =
    Map(
      Permissible -> Consume,
      Consume -> LessConsume,
      LessConsume -> NoConsume,
      NoConsume -> Permissible
    )

}
