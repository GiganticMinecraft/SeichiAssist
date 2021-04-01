package com.github.unchama.buildassist.menu

import cats.effect.{IO, SyncIO}
import com.github.unchama.menuinventory.LayoutPreparationContext
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.managedfly.ManagedFlyApi
import org.bukkit.entity.Player

trait BuildAssistMenuRouter[F[_]] {
  implicit val canOpenBuildMainMenu: F CanOpen BuildMainMenu.type
}

object BuildAssistMenuRouter {
  def apply(implicit
            flyApi: ManagedFlyApi[SyncIO, Player],
            layoutPreparationContext: LayoutPreparationContext,
            onMainThread: OnMinecraftServerThread[IO]): BuildAssistMenuRouter[IO] = new BuildAssistMenuRouter[IO] {
    implicit lazy val blockPlacementSkillMenuEnvironment: BlockPlacementSkillMenu.Environment = new BlockPlacementSkillMenu.Environment
    implicit lazy val buildMainMenuEnvironment: BuildMainMenu.Environment = new BuildMainMenu.Environment
    implicit lazy val mineStackMassCraftMenuEnvironment: MineStackMassCraftMenu.Environment = new MineStackMassCraftMenu.Environment
    implicit lazy val blockLinePlacementSkillMenuEnvironment: BlockLinePlacementSkillMenu.Environment = new BlockLinePlacementSkillMenu.Environment

    implicit lazy val canOpenBlockPlacementSkillMenu: CanOpen[IO, BlockPlacementSkillMenu.type] = _.open
    implicit lazy val canOpenMineStackMassCraftMenu: CanOpen[IO, MineStackMassCraftMenu] = _.open
    implicit lazy val canOpenLinePlacementSkillMenuEnvironment: CanOpen[IO, BlockLinePlacementSkillMenu] = _.open
    override implicit lazy val canOpenBuildMainMenu: CanOpen[IO, BuildMainMenu.type] = _.open
  }
}
