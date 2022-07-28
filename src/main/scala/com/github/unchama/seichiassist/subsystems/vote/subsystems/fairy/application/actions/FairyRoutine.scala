package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions

import com.github.unchama.concurrent.RepeatingTaskContext
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI

trait FairyRoutine[F[_], G[_], Player] {

  def start(player: Player)(
    implicit breakCountAPI: BreakCountAPI[F, G, Player],
    fairyAPI: FairyAPI[F, Player],
    voteAPI: VoteAPI[F, Player],
    manaApi: ManaApi[F, G, Player],
    context: RepeatingTaskContext
  ): F[Nothing]

}
