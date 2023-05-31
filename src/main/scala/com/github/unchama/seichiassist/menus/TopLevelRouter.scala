package com.github.unchama.seichiassist.menus

import cats.effect.{IO, SyncIO}
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.menuinventory.LayoutPreparationContext
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.menus.achievement.group.AchievementGroupMenu
import com.github.unchama.seichiassist.menus.achievement.{
  AchievementCategoryMenu,
  AchievementMenu
}
import com.github.unchama.seichiassist.menus.home.{ConfirmationMenuEnvironment, HomeMenu}
import com.github.unchama.seichiassist.menus.minestack.{
  CategorizedMineStackMenu,
  MineStackMainMenu,
  MineStackSelectItemColorMenu
}
import com.github.unchama.seichiassist.menus.nicknames.NickNameMenu
import com.github.unchama.seichiassist.menus.ranking.{RankingMenu, RankingRootMenu}
import com.github.unchama.seichiassist.menus.skill.{
  ActiveSkillEffectMenu,
  ActiveSkillMenu,
  PassiveSkillMenu,
  PremiumPointTransactionHistoryMenu
}
import com.github.unchama.seichiassist.menus.stickmenu.{FirstPage, SecondPage}
import com.github.unchama.seichiassist.subsystems.anywhereender.AnywhereEnderChestAPI
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.breakcountbar.BreakCountBarAPI
import com.github.unchama.seichiassist.subsystems.breakflags.BreakFlagAPI
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData
import com.github.unchama.seichiassist.subsystems.discordnotification.DiscordNotificationAPI
import com.github.unchama.seichiassist.subsystems.donate.DonatePremiumPointAPI
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.{
  FastDiggingEffectApi,
  FastDiggingSettingsApi
}
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.FourDimensionalPocketApi
import com.github.unchama.seichiassist.subsystems.gacha.GachaDrawAPI
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.consumegachaticket.ConsumeGachaTicketAPI
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.GachaTicketAPI
import com.github.unchama.seichiassist.subsystems.gachapoint.GachaPointApi
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.home.HomeReadAPI
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.subsystems.ranking.api.AssortedRankingApi
import com.github.unchama.seichiassist.subsystems.ranking.domain.values.{LoginTime, VoteCount}
import com.github.unchama.seichiassist.subsystems.sharedinventory.SharedInventoryAPI
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairyspeech.FairySpeechAPI
import io.chrisdavenport.cats.effect.time.JavaTime
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

trait TopLevelRouter[F[_]] {

  implicit val canOpenStickMenu: F CanOpen FirstPage.type

  implicit val canOpenAchievementMenu: F CanOpen AchievementMenu.type

  implicit val ioCanOpenMineStackMenu: F CanOpen MineStackMainMenu.type

  implicit val ioCanOpenCategorizedMineStackMenu: F CanOpen CategorizedMineStackMenu

  implicit val ioCanOpenNickNameMenu: F CanOpen NickNameMenu.type

}

object TopLevelRouter {

  def apply(
    implicit javaTime: JavaTime[IO],
    layoutPreparationContext: LayoutPreparationContext,
    onMainThread: OnMinecraftServerThread[IO],
    nonServerThreadContextShift: NonServerThreadContextShift[IO],
    breakCountApi: BreakCountAPI[IO, SyncIO, Player],
    breakCountBarAPI: BreakCountBarAPI[SyncIO, Player],
    manaApi: ManaApi[IO, SyncIO, Player],
    assortedRankingApi: AssortedRankingApi[IO],
    gachaPointApi: GachaPointApi[IO, SyncIO, Player],
    fastDiggingEffectApi: FastDiggingEffectApi[IO, Player],
    fastDiggingSettingsApi: FastDiggingSettingsApi[IO, Player],
    fourDimensionalPocketApi: FourDimensionalPocketApi[IO, Player],
    globalNotification: DiscordNotificationAPI[IO],
    homeReadApi: HomeReadAPI[IO],
    enderChestAccessApi: AnywhereEnderChestAPI[IO],
    sharedInventoryAPI: SharedInventoryAPI[IO, Player],
    voteAPI: VoteAPI[IO, Player],
    fairyAPI: FairyAPI[IO, SyncIO, Player],
    donateAPI: DonatePremiumPointAPI[IO],
    gachaTicketAPI: GachaTicketAPI[IO],
    gachaPrizeAPI: GachaPrizeAPI[IO, ItemStack, Player],
    mineStackAPI: MineStackAPI[IO, Player, ItemStack],
    gachaDrawAPI: GachaDrawAPI[IO, Player],
    consumeGachaTicketAPI: ConsumeGachaTicketAPI[IO, Player],
    fairySpeechAPI: FairySpeechAPI[IO, Player],
    breakFlagAPI: BreakFlagAPI[IO, Player]
  ): TopLevelRouter[IO] = new TopLevelRouter[IO] {
    import assortedRankingApi._

    implicit lazy val secondPageEnv: SecondPage.Environment = new SecondPage.Environment
    implicit lazy val mineStackMainMenuEnv: MineStackMainMenu.Environment =
      new MineStackMainMenu.Environment
    implicit lazy val categorizedMineStackMenuEnv: CategorizedMineStackMenu.Environment =
      new CategorizedMineStackMenu.Environment
    implicit lazy val regionMenuEnv: RegionMenu.Environment = ()
    implicit lazy val activeSkillMenuEnv: ActiveSkillMenu.Environment =
      new ActiveSkillMenu.Environment
    implicit lazy val activeSkillEffectMenuEnv: ActiveSkillEffectMenu.Environment =
      new ActiveSkillEffectMenu.Environment
    implicit lazy val premiumPointTransactionHistoryMenuEnv
      : PremiumPointTransactionHistoryMenu.Environment =
      new PremiumPointTransactionHistoryMenu.Environment
    implicit lazy val serverSwitchMenuEnv: ServerSwitchMenu.Environment =
      new ServerSwitchMenu.Environment
    implicit lazy val nickNameMenuEnv: NickNameMenu.Environment =
      new NickNameMenu.Environment
    implicit lazy val achievementMenuEnv: AchievementMenu.Environment =
      new AchievementMenu.Environment
    implicit lazy val homeMenuEnv: HomeMenu.Environment = new HomeMenu.Environment
    implicit lazy val homeConfirmationMenuEnv: home.ConfirmationMenuEnvironment.Environment =
      new ConfirmationMenuEnvironment.Environment
    implicit lazy val achievementCategoryMenuEnv: AchievementCategoryMenu.Environment =
      new AchievementCategoryMenu.Environment
    implicit lazy val achievementGroupMenuEnv: AchievementGroupMenu.Environment =
      new AchievementGroupMenu.Environment
    implicit lazy val passiveSkillMenuEnv: PassiveSkillMenu.Environment =
      new PassiveSkillMenu.Environment
    implicit lazy val mineStackSelectItemColorMenuEnv
      : MineStackSelectItemColorMenu.Environment =
      new MineStackSelectItemColorMenu.Environment

    implicit lazy val seichiRankingMenuEnv: RankingMenu[SeichiAmountData]#Environment =
      new RankingMenu.Environment
    implicit lazy val buildRankingMenuEnv: RankingMenu[BuildAmountData]#Environment =
      new RankingMenu.Environment
    implicit lazy val loginTimeRankingMenuEnv: RankingMenu[LoginTime]#Environment =
      new RankingMenu.Environment
    implicit lazy val voteCountRankingMenuEnv: RankingMenu[VoteCount]#Environment =
      new RankingMenu.Environment

    implicit lazy val rankingRootMenuEnv: RankingRootMenu.Environment =
      new RankingRootMenu.Environment

    implicit lazy val voteMenuEnv: VoteMenu.Environment = new VoteMenu.Environment

    implicit lazy val stickMenuEnv: FirstPage.Environment = new FirstPage.Environment

    implicit lazy val ioCanOpenVoteMenu: IO CanOpen VoteMenu.type = _.open

    implicit lazy val ioCanOpenNickNameMenu: IO CanOpen NickNameMenu.type = _.open

    implicit lazy val ioCanOpenSelectItemColorMenu: IO CanOpen MineStackSelectItemColorMenu =
      _.open
    implicit lazy val ioCanOpenAchievementGroupMenu: IO CanOpen AchievementGroupMenu = _.open
    implicit lazy val ioCanOpenHomeConfirmationMenu
      : IO CanOpen home.HomeChangeConfirmationMenu =
      _.open
    implicit lazy val ioCanOpenAchievementCategoryMenu: IO CanOpen AchievementCategoryMenu =
      _.open
    implicit lazy val ioCanOpenPremiumPointTransactionHistoryMenu
      : IO CanOpen PremiumPointTransactionHistoryMenu = _.open
    implicit lazy val ioCanOpenActiveSkillEffectMenu: IO CanOpen ActiveSkillEffectMenu.type =
      _.open
    override implicit lazy val ioCanOpenCategorizedMineStackMenu
      : IO CanOpen CategorizedMineStackMenu =
      _.open
    implicit lazy val ioCanOpenSecondPage: IO CanOpen SecondPage.type = _.open
    implicit lazy val ioCanOpenMineStackMenu: IO CanOpen MineStackMainMenu.type = _.open
    implicit lazy val ioCanOpenRegionMenu: IO CanOpen RegionMenu.type = _.open
    implicit lazy val ioCanOpenActiveSkillMenu: IO CanOpen ActiveSkillMenu.type = _.open
    implicit lazy val ioCanOpenServerSwitchMenu: IO CanOpen ServerSwitchMenu.type = _.open
    implicit lazy val ioCanOpenHomeMenu: IO CanOpen home.HomeMenu = _.open
    implicit lazy val ioCanOpenHomeConfirmMenu: IO CanOpen home.HomeRemoveConfirmationMenu =
      _.open
    implicit lazy val ioCanOpenPassiveSkillMenu: IO CanOpen PassiveSkillMenu.type = _.open

    implicit lazy val ioCanOpenSeichiRankingMenu: IO CanOpen RankingMenu[SeichiAmountData] =
      _.open
    implicit lazy val ioCanOpenBuildRankingMenu: IO CanOpen RankingMenu[BuildAmountData] =
      _.open
    implicit lazy val ioCanOpenLoginTimeRankingMenu: IO CanOpen RankingMenu[LoginTime] = _.open
    implicit lazy val ioCanOpenVoteCountRankingMenu: IO CanOpen RankingMenu[VoteCount] = _.open
    implicit lazy val ioCanOpenRankingRootMenu: IO CanOpen RankingRootMenu.type = _.open

    override implicit lazy val canOpenStickMenu: IO CanOpen FirstPage.type = _.open
    override implicit lazy val canOpenAchievementMenu: IO CanOpen AchievementMenu.type = _.open
  }

}
