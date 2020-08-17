package com.github.unchama.targetedeffect.player

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.targetedeffect.TargetedEffect
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player

object ActionBarMessageEffect {
  def apply(message: String): TargetedEffect[Player] =
    apply(new TextComponent(message))

  def apply(textComponent: TextComponent): TargetedEffect[Player] = Kleisli { player =>
    IO {
      player.spigot().sendMessage(ChatMessageType.ACTION_BAR, textComponent)
    }
  }
}
