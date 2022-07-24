package com.github.unchama.seichiassist.menus

import cats.effect.{ConcurrentEffect, IO, SyncIO}
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.bukkit.actions.BukkitReceiveVoteBenefits
import com.github.unchama.seichiassist.task.VotingFairyTask
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.player.PlayerEffects.closeInventoryEffect
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffect, UnfocusedEffect}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.{Material, Sound}

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

    def receiveVoteBenefitsButton(uuid: UUID)(
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
            .enchanted()
            .build(),
          LeftClickButtonEffect {
            implicit val ioCE: ConcurrentEffect[IO] =
              IO.ioConcurrentEffect(PluginExecutionContexts.asyncShift)
            SequentialEffect(
              TargetedEffect.delay { player =>
                BukkitReceiveVoteBenefits[IO, SyncIO].receive(player).unsafeRunAsyncAndForget()
              },
              MessageEffect(
                s"${GOLD}投票特典$WHITE(${voteCounter.value - benefits.value}票分)を受け取りました"
              )
            )
          }
        )
      }
    }.unsafeRunSync()

    val showVoteURLButton: Button = Button(
      new IconItemStackBuilder(Material.BOOK_AND_QUILL)
        .title(s"$YELLOW$UNDERLINE${BOLD}投票ページにアクセス")
        .lore(
          List(
            s"$RESET${GREEN}投票すると様々な特典が！",
            s"$RESET${GREEN}1日1回投票できます",
            s"$RESET${DARK_GRAY}クリックするとチャット欄に",
            s"$RESET${DARK_GRAY}URLが表示されますので",
            s"$RESET${DARK_GRAY}Tキーを押してから",
            s"$RESET${DARK_GRAY}そのURLをクリックしてください"
          )
        )
        .build(),
      LeftClickButtonEffect {
        SequentialEffect(
          MessageEffect(
            List(
              s"$RED${UNDERLINE}https://minecraft.jp/servers/54d3529e4ddda180780041a7/vote",
              s"$RED${UNDERLINE}https://monocraft.net/servers/Cf3BffNIRMERDNbAfWQm"
            )
          ),
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          closeInventoryEffect
        )
      }
    )

    def fairySummonTimeToggleButton(uuid: UUID): Button = {
      val playerData = SeichiAssist.playermap(uuid)
      Button(
        new IconItemStackBuilder(Material.WATCH)
          .title(s"$AQUA$UNDERLINE${BOLD}マナ妖精 時間設定")
          .lore(
            List(
              s"$RESET$GREEN$BOLD${VotingFairyTask.dispToggleVFTime(playerData.toggleVotingFairy)}",
              "",
              s"$RESET${GRAY}コスト",
              s"$RESET$RED$BOLD${playerData.toggleVotingFairy * 2}投票pt",
              "",
              s"$RESET$DARK_RED${UNDERLINE}クリックで切り替え"
            )
          )
          .build(),
        LeftClickButtonEffect {
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            UnfocusedEffect(playerData.toggleVotingFairy = playerData.toggleVotingFairy % 4 + 1)
          )
        }
      )
    }

    def fairyContractSettingToggle(uuid: UUID): Button = {
      val playerData = SeichiAssist.playermap(uuid)
      Button(new IconItemStackBuilder(Material.PAPER).title(s"$GOLD$UNDERLINE${BOLD}妖精とのお約束"))
    }

  }
}
