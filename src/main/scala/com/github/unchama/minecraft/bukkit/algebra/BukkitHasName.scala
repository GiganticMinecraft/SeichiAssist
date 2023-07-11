package com.github.unchama.minecraft.bukkit.algebra

import com.github.unchama.minecraft.algebra.HasName
import org.bukkit.entity.Player

class BukkitHasName extends HasName[Player] {

  override def of(x: Player): String = x.getName

}

object BukkitHasName {

  implicit val instance: HasName[Player] = new BukkitHasName

}
