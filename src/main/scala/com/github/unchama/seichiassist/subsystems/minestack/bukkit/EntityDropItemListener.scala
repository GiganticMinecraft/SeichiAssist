package com.github.unchama.seichiassist.subsystems.minestack.bukkit

import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.seichiassist.subsystems.minestack.domain.MineStackRepository
import org.bukkit.GameMode
import org.bukkit.inventory.ItemStack
import cats.effect.concurrent.Ref
import com.github.unchama.seichiassist.subsystems.minestack.domain.AutoCollectPreference
import org.bukkit.entity.Player
import cats.effect.Effect
import com.github.unchama.generic.ApplicativeExtra.whenAOrElse
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import org.bukkit.Sound
import org.bukkit.entity.EntityType

class EntityDropItemListener[F[_]: Effect](
  implicit autoCollectPreferenceRepository: PlayerDataRepository[Ref[F, AutoCollectPreference]],
  mineStackRepository: MineStackRepository[F, Player, ItemStack],
  effectEnvironment: EffectEnvironment
) extends Listener {

  import scala.jdk.CollectionConverters._
  import cats.implicits._

  @EventHandler
  def entityDropItem(event: EntityDeathEvent): Unit = {
    val killer = Option(event.getEntity().getKiller())

    killer match {
      case Some(player) =>
        if (
          player.getGameMode != GameMode.SURVIVAL || event.getEntityType() == EntityType.PLAYER
        ) return

        val drops = event.getDrops.asScala.toVector
        val program = for {
          currentAutoMineStackState <-
            autoCollectPreferenceRepository(player).get
          intoSucceedItemStacksAndFailedItemStacks <- whenAOrElse(
            currentAutoMineStackState.isEnabled
          )(mineStackRepository.tryIntoMineStack(player, drops), (drops, Vector.empty))
          _ <- Effect[F].delay {
            if (!event.getDrops().isEmpty()) {
              player.playSound(player.getLocation, Sound.ENTITY_ITEM_PICKUP, 1f, 1f)
            }

            intoSucceedItemStacksAndFailedItemStacks
              ._1
              .foreach(itemStack => player.getWorld.dropItem(player.getLocation, itemStack))
            event.getDrops.clear()
          }
        } yield ()

        effectEnvironment.unsafeRunEffectAsync(
          "プレイヤーが倒したエンティティのドロップアイテムをマインスタックに格納またはドロップする",
          program
        )

      case None =>
    }
  }

}
