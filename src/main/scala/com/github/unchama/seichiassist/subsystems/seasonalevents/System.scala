package com.github.unchama.seichiassist.subsystems.seasonalevents

import cats.Functor
import cats.effect.{Clock, ConcurrentEffect, IO, SyncEffect}
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.mana.ManaWriteApi
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary.AnniversaryListener
import com.github.unchama.seichiassist.subsystems.seasonalevents.api.SeasonalEventsAPI
import com.github.unchama.seichiassist.subsystems.seasonalevents.christmas.ChristmasItemListener
import com.github.unchama.seichiassist.subsystems.seasonalevents.commands.EventCommand
import com.github.unchama.seichiassist.subsystems.seasonalevents.domain.LastQuitPersistenceRepository
import com.github.unchama.seichiassist.subsystems.seasonalevents.halloween.HalloweenItemListener
import com.github.unchama.seichiassist.subsystems.seasonalevents.infrastructure.JdbcLastQuitPersistenceRepository
import com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin.LimitedLoginBonusGifter
import com.github.unchama.seichiassist.subsystems.seasonalevents.newyear.NewYearListener
import com.github.unchama.seichiassist.subsystems.seasonalevents.seizonsiki.SeizonsikiListener
import com.github.unchama.seichiassist.subsystems.seasonalevents.valentine.ValentineListener
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina.GtToSiinaAPI
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

import java.util.UUID

class System[F[_]](
  override val listeners: Seq[Listener],
  override val commands: Map[String, TabExecutor]
) extends Subsystem[F] {

  def api[G[_]: Clock: Functor]: SeasonalEventsAPI[G] = SeasonalEventsAPI.withF[G]

}

object System {
  def wired[F[_]: ConcurrentEffect: NonServerThreadContextShift, G[_]: SyncEffect, H[_]](
    instance: JavaPlugin
  )(
    implicit manaWriteApi: ManaWriteApi[G, Player],
    effectEnvironment: EffectEnvironment,
    ioOnMainThread: OnMinecraftServerThread[IO],
    gtToSiinaAPI: GtToSiinaAPI[ItemStack],
    playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]
  ): System[H] = {

    implicit val repository: LastQuitPersistenceRepository[F, UUID] =
      new JdbcLastQuitPersistenceRepository[F]

    new System(
      listeners = Seq(
        new AnniversaryListener(),
        new ChristmasItemListener(instance),
        HalloweenItemListener,
        new LimitedLoginBonusGifter,
        new SeizonsikiListener,
        new ValentineListener(),
        new NewYearListener()
      ),
      commands = Map("event" -> new EventCommand().executor)
    )
  }
}
