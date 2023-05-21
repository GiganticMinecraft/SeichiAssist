package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.gachaprize.bukkit.factories.BukkitStaticGachaPrizeFactory
import com.github.unchama.seichiassist.util.InventoryOperations
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class MineHeadCommand(implicit ioOnMainThread: OnMinecraftServerThread[IO]) {

  import com.github.unchama.targetedeffect._

  val effect: TargetedEffect[Player] =
    SequentialEffect(
      InventoryOperations.grantItemStacksEffect(BukkitStaticGachaPrizeFactory.mineHeadItem),
      MessageEffect(s"${GREEN}専用アイテムを付与しました。")
    )

  val executor: TabExecutor =
    playerCommandBuilder[Nothing].execution { _ => IO.pure(effect) }.build().asNonBlockingTabExecutor()
}
