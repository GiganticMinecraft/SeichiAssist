package com.github.unchama.seichiassist.util.typeclass

import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.entity.Player
import simulacrum.typeclass

@typeclass trait Sendable[-T]{
  def sendMessage(player: Player, message: T): Unit
}

object Sendable {
  implicit val forString: Sendable[String] =
    (player: Player, message: String) => player.sendMessage(message)

  implicit val forStringArray: Sendable[Array[String]] =
    (player: Player, message: Array[String]) => player.sendMessage(message)

  implicit val forBaseComponent: Sendable[BaseComponent] =
    (player: Player, message: BaseComponent) => player.spigot().sendMessage(message)
}