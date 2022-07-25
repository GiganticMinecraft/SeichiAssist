package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions

import cats.effect.{Concurrent, ConcurrentEffect}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.FairySpeakTask
import org.bukkit.entity.Player

object BukkitFairySpeakTask {

  def apply[F[_]: ConcurrentEffect](player: Player): FairySpeakTask[F, Player] =
    (player: Player) => {}

}
