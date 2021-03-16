package com.github.unchama.seichiassist.subsystems.gachapoint.application.repository

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.{Applicative, Monad}
import com.github.unchama.datarepository.template.{RefDictBackedRepositoryFinalization, RefDictBackedRepositoryInitialization, RepositoryFinalization, SinglePhasedRepositoryInitialization}
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.settings.{GachaTicketReceivingSettings, GachaTicketReceivingSettingsPersistence}

import java.util.UUID

object GachaTicketReceivingSettingsRepositoryDefinitions {

  type RepositoryValue[F[_]] = Ref[F, GachaTicketReceivingSettings]

  import scala.util.chaining._

  private def initialSettings[F[_] : Applicative]: F[GachaTicketReceivingSettings] =
    Applicative[F].pure(GachaTicketReceivingSettings.EveryMinute)

  def initialization[F[_] : Sync](persistence: GachaTicketReceivingSettingsPersistence[F])
  : SinglePhasedRepositoryInitialization[F, RepositoryValue[F]] =
    RefDictBackedRepositoryInitialization
      .usingUuidRefDict(persistence)(initialSettings)
      .pipe(SinglePhasedRepositoryInitialization.forRefCell[F, GachaTicketReceivingSettings])

  def finalization[
    F[_] : Monad
  ](persistence: GachaTicketReceivingSettingsPersistence[F])
  : RepositoryFinalization[F, UUID, RepositoryValue[F]] =
    RefDictBackedRepositoryFinalization
      .usingUuidRefDict(persistence)
      .pipe(RepositoryFinalization.liftToRefFinalization[F, UUID, GachaTicketReceivingSettings])

}
