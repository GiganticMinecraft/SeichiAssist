package com.github.unchama.seichiassist.subsystems.joinandquitmessenger.bukkit

import com.github.unchama.seichiassist.subsystems.joinandquitmessenger.domain.Messages
import org.bukkit.ChatColor
import org.bukkit.event.Listener
import org.bukkit.event.player.{PlayerJoinEvent, PlayerQuitEvent}

class JoinAndQuitListeners extends Listener {

  def onJoin(event: PlayerJoinEvent): Unit = {
    event.setJoinMessage(s"${ChatColor.GRAY}${Messages.joinMessage(event.getPlayer.getName)}")
  }

  def onQuit(event: PlayerQuitEvent): Unit = {
    event.setQuitMessage(Messages.quitMessage(event.getPlayer.getName))
  }

}
