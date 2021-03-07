package com.github.unchama.minecraft.bukkit.algebra

import com.github.unchama.minecraft.algebra.HasUuid
import org.bukkit.entity.Player

import java.util.UUID

class BukkitPlayerHasUuid extends HasUuid[Player] {

  override def of(x: Player): UUID = x.getUniqueId

}

object BukkitPlayerHasUuid {

  implicit val instance: HasUuid[Player] = new BukkitPlayerHasUuid

}
