package com.github.unchama.minecraft.bukkit.objects

import cats.effect.Sync
import com.github.unchama.generic.ReadWrite
import com.github.unchama.minecraft.objects.MinecraftBossBar
import org.bukkit.Bukkit
import org.bukkit.boss.{BarColor, BarFlag, BarStyle, BossBar}

class BukkitBossBar[F[_]] private (instance: BossBar)(implicit F: Sync[F])
    extends MinecraftBossBar[F] {
  override type Player = org.bukkit.entity.Player
  override type Style = BarStyle
  override type Color = BarColor
  override type Flag = BarFlag

  override val title: ReadWrite[F, String] =
    ReadWrite.liftUnsafe(instance.getTitle, instance.setTitle)
  override val color: ReadWrite[F, BarColor] =
    ReadWrite.liftUnsafe(instance.getColor, instance.setColor)
  override val style: ReadWrite[F, BarStyle] =
    ReadWrite.liftUnsafe(instance.getStyle, instance.setStyle)
  override val progress: ReadWrite[F, Double] =
    ReadWrite.liftUnsafe(instance.getProgress, instance.setProgress)
  override val visibility: ReadWrite[F, Boolean] =
    ReadWrite.liftUnsafe(instance.isVisible, instance.setVisible)

  override val flags: FlagOperations = new FlagOperations {
    override def add(flag: BarFlag): F[Unit] = F.delay(instance.addFlag(flag))

    override def remove(flag: BarFlag): F[Unit] = F.delay(instance.removeFlag(flag))

    override def has(flag: BarFlag): F[Unit] = F.delay(instance.hasFlag(flag))
  }
  override val players: PlayerOperations = new PlayerOperations {
    override val removeAll: F[Unit] = F.delay(instance.removeAll())

    override def add(player: Player): F[Unit] = F.delay(instance.addPlayer(player))

    override def remove(player: Player): F[Unit] = F.delay(instance.removePlayer(player))

    import scala.jdk.CollectionConverters._

    override def getAll: F[List[Player]] = F.delay(instance.getPlayers.asScala.toList)
  }
}

object BukkitBossBar {

  def apply[F[_]: Sync](title: String, color: BarColor, style: BarStyle): F[BukkitBossBar[F]] =
    in[F, F](title, color, style)

  def in[G[_]: Sync, F[_]: Sync](
    title: String,
    color: BarColor,
    style: BarStyle
  ): G[BukkitBossBar[F]] =
    Sync[G].delay(new BukkitBossBar(Bukkit.getServer.createBossBar(title, color, style)))
}
