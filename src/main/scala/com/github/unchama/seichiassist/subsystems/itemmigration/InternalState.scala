package com.github.unchama.seichiassist.subsystems.itemmigration

import cats.effect.concurrent.TryableDeferred
import com.github.unchama.playerdatarepository.PlayerDataRepository

case class InternalState[F[_]](entryPoints: EntryPoints,
                               migrationStateRepository: PlayerDataRepository[TryableDeferred[F, Unit]])
