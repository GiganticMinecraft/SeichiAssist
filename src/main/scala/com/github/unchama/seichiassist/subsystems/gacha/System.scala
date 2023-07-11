package com.github.unchama.seichiassist.subsystems.gacha

import cats.data.Kleisli
import cats.effect.ConcurrentEffect
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.minecraft.bukkit.algebra.CloneableBukkitItemStack.instance
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.{
  DrawGacha,
  GrantGachaPrize
}
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions.{
  BukkitDrawGacha,
  BukkitGrantGachaPrize
}
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.{
  GachaCommand,
  PlayerPullGachaListener
}
import com.github.unchama.seichiassist.subsystems.gacha.domain.LotteryOfGachaItems
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.GachaTicketAPI
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.{
  CanBeSignedAsGachaPrize,
  StaticGachaPrizeFactory
}
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

trait System[F[_], Player] extends Subsystem[F] {

  val api: GachaDrawAPI[F, Player]

}

object System {

  def wired[F[_]: ConcurrentEffect: OnMinecraftServerThread](
    implicit gachaPrizeAPI: GachaPrizeAPI[F, ItemStack, Player],
    gachaTicketAPI: GachaTicketAPI[F],
    mineStackAPI: MineStackAPI[F, Player, ItemStack]
  ): System[F, Player] = {
    implicit val canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
      gachaPrizeAPI.canBeSignedAsGachaPrize
    implicit val grantGachaPrize: GrantGachaPrize[F, ItemStack, Player] =
      new BukkitGrantGachaPrize[F]
    implicit val staticGachaPrizeFactory: StaticGachaPrizeFactory[ItemStack] =
      gachaPrizeAPI.staticGachaPrizeFactory
    implicit val lotteryOfGachaItems: LotteryOfGachaItems[F, ItemStack] =
      new LotteryOfGachaItems[F, ItemStack]
    implicit val drawGacha: DrawGacha[F, Player] = new BukkitDrawGacha[F]

    new System[F, Player] {
      override val api: GachaDrawAPI[F, Player] = (draws: Int) =>
        Kleisli { player => drawGacha.draw(player, draws) }

      override val commands: Map[String, TabExecutor] = Map(
        "gacha" -> new GachaCommand[F].executor
      )

      override val listeners: Seq[Listener] = Seq(new PlayerPullGachaListener[F])
    }
  }

}
