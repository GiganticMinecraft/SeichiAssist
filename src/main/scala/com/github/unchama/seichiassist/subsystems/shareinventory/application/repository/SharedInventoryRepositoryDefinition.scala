package com.github.unchama.seichiassist.subsystems.shareinventory.application.repository

import cats.effect.Sync
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.shareinventory.domain.{
  SharedFlag,
  SharedInventoryPersistence
}

object SharedInventoryRepositoryDefinition {

  case class RepositoryValue[F[_]](sharedFlag: ReadOnlyRef[F, SharedFlag])

  def withContext[G[_]: Sync, F[_], Player: HasUuid](
    persistence: SharedInventoryPersistence[G]
  ): RepositoryDefinition[G, Player, RepositoryValue[F]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict[G, Player, SharedFlag](persistence)(SharedFlag.NotSharing)
      .toRefRepository
      .map(ref => RepositoryValue[F](ReadOnlyRef.fromRef(ref)))

}
