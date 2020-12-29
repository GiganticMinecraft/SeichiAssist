package com.github.unchama.seichiassist.subsystems.loginbonusticket.bukkit.listeners

import java.time.LocalDate

import com.github.unchama.seichiassist.DefaultEffectEnvironment
import com.github.unchama.seichiassist.subsystems.loginbonusticket.bukkit.itemstack.BukkitLoginBonusTicketItemStack.loginBonusTicket
import com.github.unchama.seichiassist.util.Util.grantItemStacksEffect
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.ChatColor.AQUA

class LoginBonusGifter[F[_] : ConcurrentEffect : NonServerThreadContextShift]
  (implicit effectEnvironment: EffectEnvironment, repository: LastQuitPersistenceRepository[F, UUID]) extends Listener {
  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit = {
    val player = event.getPlayer

    // TODO lastquitの日付をとる
    val lastQuitDate = LocalDate.now().minusDays(1)
    if (!lastQuitDate.isBefore(LocalDate.now())) return;

    DefaultEffectEnvironment.runEffectAsync(
      "ログインボーナスチケットを付与する",
      grantItemStacksEffect(loginBonusTicket).run(player)
    )
    player.sendMessage(s"${AQUA}今日1回目のログインのため、ログインボーナスチケットを配布しました。")

  }
}