package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy

import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairySpawnRequestError

package object domain {

  type FairySpawnRequestErrorOrSpawn[F[_]] = Either[FairySpawnRequestError, F[Unit]]

}
