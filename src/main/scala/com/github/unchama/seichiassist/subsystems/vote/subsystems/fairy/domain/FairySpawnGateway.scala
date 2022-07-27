package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

import cats.effect.{ConcurrentEffect, IO, SyncIO}
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.service.FairySpeechService

trait FairySpawnGateway[F[_], Player] {

  /**
   * 妖精をスポーンさせる作用
   */
  def spawn(player: Player)(
    implicit breakCountAPI: BreakCountAPI[IO, SyncIO, Player],
    fairyAPI: FairyAPI[IO, Player],
    voteAPI: VoteAPI[IO],
    manaApi: ManaApi[IO, SyncIO, Player],
    serviceRepository: PlayerDataRepository[FairySpeechService[SyncIO]],
    concurrentEffect: ConcurrentEffect[IO]
  ): F[Unit]

  /**
   * 妖精をデスポーンさせる作用
   */
  def despawn(): F[Unit]

}
