package com.github.unchama.seichiassist.subsystems.gachapoint.application.repository

import cats.Applicative
import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.template.{RefDictBackedRepositoryFinalization, RefDictBackedRepositoryInitialization, RepositoryFinalization, SinglePhasedRepositoryInitialization}
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.settings.{GachaTicketReceivingSettings, GachaTicketReceivingSettingsPersistence}

import java.util.UUID

object GachaTicketReceivingSettingsRepositoryDefinitions {

  type RepositoryValue[F[_]] = Ref[F, GachaTicketReceivingSettings]

  import scala.util.chaining._

  private def initialSettings[F[_] : Applicative]: F[GachaTicketReceivingSettings] =
    Applicative[F].pure(GachaTicketReceivingSettings.EveryMinute)

  def initialization[F[_] : Sync](persistence: GachaTicketReceivingSettingsPersistence[F])
  : SinglePhasedRepositoryInitialization[F, Ref[F, GachaTicketReceivingSettings]] =
    RefDictBackedRepositoryInitialization
      .usingUuidRefDict(persistence)(initialSettings)
      .pipe(SinglePhasedRepositoryInitialization.forRefCell)

  def finalization[
    F[_] : Applicative
  ](persistence: GachaTicketReceivingSettingsPersistence[F])
  : RepositoryFinalization[F, UUID, RepositoryValue[F]] =
    RefDictBackedRepositoryFinalization.usingUuidRefDict(persistence)

}
