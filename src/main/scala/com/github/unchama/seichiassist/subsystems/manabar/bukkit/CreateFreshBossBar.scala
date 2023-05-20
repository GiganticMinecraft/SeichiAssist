package com.github.unchama.seichiassist.subsystems.manabar.bukkit

import cats.effect.Sync
import com.github.unchama.minecraft.bukkit.objects.BukkitBossBar
import com.github.unchama.minecraft.objects.MinecraftBossBar
import org.bukkit.boss.{BarColor, BarStyle}
import org.bukkit.entity.{Player => BukkitPlayer}

object CreateFreshBossBar {

  import cats.implicits._

  def in[G[_]: Sync, F[_]: Sync]: G[MinecraftBossBar[F] { type Player = BukkitPlayer }] =
    BukkitBossBar.in[G, F]("", BarColor.BLUE, BarStyle.SOLID).widen

}
