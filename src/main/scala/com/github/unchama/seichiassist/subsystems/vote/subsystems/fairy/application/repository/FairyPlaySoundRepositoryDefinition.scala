package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.repository

import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairyPlaySound

object FairyPlaySoundRepositoryDefinition {

  case class RepositoryValue[F[_]](playSoundRef: Ref[F, FairyPlaySound])

  def withContext[F[_], Player: HasUuid]: RepositoryDefinition[F, Player, RepositoryValue[F]] =
    RefDictBackedRepositoryDefinition.usingUuidRefDict[F, Player, FairyPlaySound]()(
      FairyPlaySound(true)
    )

}
