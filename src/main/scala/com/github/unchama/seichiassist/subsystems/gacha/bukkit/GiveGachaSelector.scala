package com.github.unchama.seichiassist.subsystems.gacha.bukkit

import java.util.UUID

sealed trait GiveGachaSelector

case object GiveAll extends GiveGachaSelector

case class ByUUID(uuid: UUID) extends GiveGachaSelector

case class ByName(playerLogin: String) extends GiveGachaSelector
