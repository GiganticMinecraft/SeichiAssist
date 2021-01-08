package com.github.unchama.buildassist.menu

import cats.effect.{IO, SyncIO}
import com.github.unchama.menuinventory.LayoutPreparationContext
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.minecraft.actions.MinecraftServerThreadShift
import com.github.unchama.seichiassist.subsystems

trait BuildAssistMenuRouter[F[_]] {
  implicit val canOpenBlockPlacementSkillMenu: F CanOpen BlockPlacementSkillMenu.type
  implicit val canOpenBuildMainMenu: F CanOpen BuildMainMenu.type
  implicit val canOpenMineStackMassCraftMenu: F CanOpen MineStackMassCraftMenu
}

object BuildAssistMenuRouter {

  def apply(implicit
            flyState: subsystems.managedfly.InternalState[SyncIO],
            layoutPreparationContext: LayoutPreparationContext,
            syncShift: MinecraftServerThreadShift[IO]): BuildAssistMenuRouter[IO] = {
    new BuildAssistMenuRouter[IO] {
      implicit lazy val blockPlacementSkillMenuEnvironment: BlockPlacementSkillMenu.Environment =
        new BlockPlacementSkillMenu.Environment
      implicit lazy val buildMainMenuEnvironment: BuildMainMenu.Environment =
        new BuildMainMenu.Environment
      implicit lazy val mineStackMassCraftMenuEnvironment: MineStackMassCraftMenu.Environment =
        new MineStackMassCraftMenu.Environment

      override implicit lazy val canOpenBlockPlacementSkillMenu: CanOpen[IO, BlockPlacementSkillMenu.type] =
        _.open
      override implicit lazy val canOpenBuildMainMenu: CanOpen[IO, BuildMainMenu.type] =
        _.open
      override implicit lazy val canOpenMineStackMassCraftMenu: CanOpen[IO, MineStackMassCraftMenu] =
        _.open
    }
  }

}
