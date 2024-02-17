package com.github.unchama.minecraft.bukkit.algebra

import com.github.unchama.minecraft.algebra.HasName
import org.bukkit.entity.Player

class BukkitPlayerHasName extends HasName[Player] {

  override def of(x: Player): String = x.getName

}

object BukkitPlayerHasName {

  implicit val instance: HasName[Player] = new BukkitPlayerHasName

}
