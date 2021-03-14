package com.github.unchama.seichiassist.subsystems.gachapoint.domain.settings

import com.github.unchama.generic.RefDict

import java.util.UUID

trait GachaTicketReceivingSettingsPersistence[F[_]] extends RefDict[F, UUID, GachaTicketReceivingSettings]
