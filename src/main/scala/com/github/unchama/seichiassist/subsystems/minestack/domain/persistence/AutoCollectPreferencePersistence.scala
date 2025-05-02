package com.github.unchama.seichiassist.subsystems.minestack.domain.persistence

import com.github.unchama.generic.RefDict
import com.github.unchama.seichiassist.subsystems.minestack.domain.AutoCollectPreference

import java.util.UUID

trait AutoCollectPreferencePersistence[F[_]] extends RefDict[F, UUID, AutoCollectPreference]
