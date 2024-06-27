package com.github.unchama.buildassist.menu

import cats.effect.{IO, SyncIO}
import com.github.unchama.menuinventory.LayoutPreparationContext
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.menus.BuildMainMenu
import com.github.unchama.seichiassist.subsystems.managedfly.ManagedFlyApi
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

trait BuildAssistMenuRouter[F[_]] {
  implicit val canOpenBuildMainMenu: F CanOpen BuildMainMenu.type
}

object BuildAssistMenuRouter {
  def apply(
    implicit flyApi: ManagedFlyApi[SyncIO, Player],
    mineStackAPI: MineStackAPI[IO, Player, ItemStack],
    layoutPreparationContext: LayoutPreparationContext,
    onMainThread: OnMinecraftServerThread[IO],
    playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]
  ): BuildAssistMenuRouter[IO] = new BuildAssistMenuRouter[IO] {
    implicit lazy val blockPlacementSkillMenuEnvironment: BlockPlacementSkillMenu.Environment =
      new BlockPlacementSkillMenu.Environment
    implicit lazy val buildMainMenuEnvironment: BuildMainMenu.Environment =
      new BuildMainMenu.Environment
    implicit lazy val mineStackMassCraftMenuEnvironment: MineStackMassCraftMenu.Environment =
      new MineStackMassCraftMenu.Environment

    implicit lazy val canOpenBlockPlacementSkillMenu
      : CanOpen[IO, BlockPlacementSkillMenu.type] = _.open
    implicit lazy val canOpenMineStackMassCraftMenu: CanOpen[IO, MineStackMassCraftMenu] =
      _.open

    override implicit lazy val canOpenBuildMainMenu: CanOpen[IO, BuildMainMenu.type] = _.open
  }
}
