package com.github.unchama.seichiassist.subsystems.minestack.application.repository

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.{
  MineStackObjectPersistence,
  MineStackObjectWithAmount
}

object MineStackObjectRepositoryDefinition {

  def withContext[F[_]: Sync, Player, ItemStack](
    implicit persistence: MineStackObjectPersistence[F, ItemStack]
  ): RepositoryDefinition[F, Player, Ref[F, List[MineStackObjectWithAmount[ItemStack]]]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict[F, Player, List[MineStackObjectWithAmount[ItemStack]]](persistence)(
        List.empty
      )
      .toRefRepository

}
