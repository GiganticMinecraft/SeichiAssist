package com.github.unchama.seichiassist.menus

import cats.effect.{ConcurrentEffect, IO, SyncIO}
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.bukkit.actions.BukkitReceiveVoteBenefits
import com.github.unchama.targetedeffect.{DeferredEffect, SequentialEffect, TargetedEffect}
import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.entity.Player

import java.util.UUID

object VoteMenu extends Menu {

  class Environment(
    implicit val voteAPI: VoteAPI[IO],
    breakCountAPI: BreakCountAPI[IO, SyncIO, Player]
  )

  /**
   * メニューのサイズとタイトルに関する情報
   */
  override val frame: MenuFrame = MenuFrame(4.chestRows, s"$DARK_PURPLE${BOLD}投票ptメニュー")

  /**
   * @return
   * `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  override def computeMenuLayout(player: Player)(
    implicit environment: Environment
  ): IO[MenuSlotLayout] = {}

  private object ConstantButtons {

    def receiveVoteBenefits(uuid: UUID)(
      implicit voteAPI: VoteAPI[IO],
      breakCountAPI: BreakCountAPI[IO, SyncIO, Player]
    ): Button = {
      for {
        benefits <- voteAPI.receivedVoteBenefits(uuid)
        voteCounter <- voteAPI.voteCounter(uuid)
        effectPoint <- voteAPI.effectPoints(uuid)
      } yield {
        Button(
          new IconItemStackBuilder(Material.DIAMOND)
            .title(s"$LIGHT_PURPLE$UNDERLINE${BOLD}クリックで投票特典を受け取れます")
            .lore(
              List(
                s"$RESET${GRAY}投票特典を受け取るには",
                s"$RESET${GRAY}投票ページで投票した後",
                s"$RESET${AQUA}特典受け取り済み投票回数: ${benefits.value}",
                s"$RESET${AQUA}特典未受け取り投票係数: ${voteCounter.value - benefits.value}",
                s"$RESET${AQUA}所有pt: ${effectPoint.value}"
              )
            )
            .build(),
          LeftClickButtonEffect {
            implicit val ioCE: ConcurrentEffect[IO] =
              IO.ioConcurrentEffect(PluginExecutionContexts.asyncShift)
            TargetedEffect.delay { player =>
              BukkitReceiveVoteBenefits[IO, SyncIO].receive(player).unsafeRunAsyncAndForget()
            }
          }
        )
      }
    }.unsafeRunSync()

  }
}
