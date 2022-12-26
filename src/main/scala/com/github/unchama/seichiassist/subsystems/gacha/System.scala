package com.github.unchama.seichiassist.subsystems.gacha

import cats.effect.ConcurrentEffect
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.{DrawGacha, GrantGachaPrize}
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions.{BukkitDrawGacha, BukkitGrantGachaPrize}
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.{GachaCommand, PlayerPullGachaListener}
import com.github.unchama.seichiassist.subsystems.gacha.domain.LotteryOfGachaItems
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.GachaTicketAPI
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.{CanBeSignedAsGachaPrize, StaticGachaPrizeFactory}
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

trait System[F[_], Player] extends Subsystem[F] {

  val api: GachaDrawAPI[F, Player]

}

object System {

  import cats.implicits._

  def wired[F[_]: ConcurrentEffect: OnMinecraftServerThread: NonServerThreadContextShift](
    implicit gachaPrizeAPI: GachaPrizeAPI[F, ItemStack, Player],
    gachaTicketAPI: GachaTicketAPI[F],
    mineStackAPI: MineStackAPI[F, Player, ItemStack]
  ): F[System[F, Player]] = {
    implicit val canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
      gachaPrizeAPI.canBeSignedAsGachaPrize
    implicit val grantGachaPrize: GrantGachaPrize[F, ItemStack] =
      new BukkitGrantGachaPrize[F]
    implicit val staticGachaPrizeFactory: StaticGachaPrizeFactory[ItemStack] =
      gachaPrizeAPI.staticGachaPrizeFactory
    implicit val lotteryOfGachaItems: LotteryOfGachaItems[F, ItemStack] =
      new LotteryOfGachaItems[F, ItemStack]

    for {
      gachaPrizesListReference <- gachaPrizeAPI.listOfNow
    } yield {
      implicit val drawGacha: DrawGacha[F, Player] =
        new BukkitDrawGacha[F](gachaPrizesListReference)

      new System[F, Player] {
        override val api: GachaDrawAPI[F, Player] = (player: Player, draws: Int) => {
          drawGacha.draw(player, draws)
        }

        override val commands: Map[String, TabExecutor] = Map(
          "gacha" -> new GachaCommand[F].executor
        )

        override val listeners: Seq[Listener] = Seq(new PlayerPullGachaListener[F])
      }
    }
  }

}
