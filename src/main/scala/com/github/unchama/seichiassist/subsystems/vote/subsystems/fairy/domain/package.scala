package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy

import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairySummonRequestError

package object domain {

  type FairySpawnRequestErrorOrSpawn[F[_]] = Either[FairySummonRequestError, F[Unit]]

}
