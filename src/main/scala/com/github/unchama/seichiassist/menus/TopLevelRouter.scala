package com.github.unchama.seichiassist.menus

import cats.effect.IO
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.menuinventory.LayoutPreparationContext
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.menus.achievement.group.AchievementGroupMenu
import com.github.unchama.seichiassist.menus.achievement.{AchievementCategoryMenu, AchievementMenu}
import com.github.unchama.seichiassist.menus.home.{ConfirmationMenuEnvironment, HomeMenu}
import com.github.unchama.seichiassist.menus.minestack.{CategorizedMineStackMenu, MineStackMainMenu, MineStackSelectItemColorMenu}
import com.github.unchama.seichiassist.menus.ranking.{RankingMenu, RankingRootMenu}
import com.github.unchama.seichiassist.menus.skill.{ActiveSkillEffectMenu, ActiveSkillMenu, PassiveSkillMenu, PremiumPointTransactionHistoryMenu}
import com.github.unchama.seichiassist.menus.stickmenu.{FirstPage, SecondPage}
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData
import com.github.unchama.seichiassist.subsystems.ranking.api.AssortedRankingApi
import com.github.unchama.seichiassist.subsystems.ranking.domain.values.{LoginTime, VoteCount}
import io.chrisdavenport.cats.effect.time.JavaTime

trait TopLevelRouter[F[_]] {

  implicit val canOpenStickMenu: F CanOpen FirstPage.type

  implicit val canOpenAchievementMenu: F CanOpen AchievementMenu.type

  implicit val ioCanOpenCategorizedMineStackMenu: F CanOpen CategorizedMineStackMenu

}

object TopLevelRouter {

  def apply(
    implicit javaTime: JavaTime[IO],
    layoutPreparationContext: LayoutPreparationContext,
    onMainThread: OnMinecraftServerThread[IO],
    assortedRankingApi: AssortedRankingApi[IO],
    nonServerThreadContextShift: NonServerThreadContextShift[IO]
  ): TopLevelRouter[IO] = new TopLevelRouter[IO] {
    val seichiAssist = SeichiAssist.instance
    import assortedRankingApi._
    import seichiAssist.Apis._

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

    implicit lazy val stickMenuEnv: FirstPage.Environment = new FirstPage.Environment

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
