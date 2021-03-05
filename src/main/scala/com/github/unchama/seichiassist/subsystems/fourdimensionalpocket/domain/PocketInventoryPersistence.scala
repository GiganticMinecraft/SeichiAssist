package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain

import com.github.unchama.generic.RefDict

import java.util.UUID

trait PocketInventoryPersistence[F[_], Inventory] extends RefDict[F, UUID, Inventory]
