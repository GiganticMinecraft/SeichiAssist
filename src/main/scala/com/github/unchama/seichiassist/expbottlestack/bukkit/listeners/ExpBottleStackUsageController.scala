package com.github.unchama.seichiassist.expbottlestack.bukkit.listeners

import cats.effect.{IO, Resource}
import com.github.unchama.generic.effect.ResourceScope
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.expbottlestack.domain.BottleCount
import org.bukkit.entity.{ExperienceOrb, ThrownExpBottle}
import org.bukkit.event.block.Action
import org.bukkit.event.entity.ExpBottleEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack
import org.bukkit.{Location, Material}

class ExpBottleStackUsageController(implicit managedBottleScope: ResourceScope[IO, ThrownExpBottle],
                                    effectEnvironment: EffectEnvironment) extends Listener {

  private def spawnOrbAt(location: Location)(expAmount: Int): IO[Unit] = IO {
    val orb = location.getWorld.spawn(location, classOf[ExperienceOrb])
    orb.setExperience(expAmount)
  }

  private def bottleResourceSpawningAt(loc: Location, originalCount: BottleCount): Resource[IO, ThrownExpBottle] = {
    Resource
      .make(
        IO(loc.getWorld.spawn(loc, classOf[ThrownExpBottle]))
      ) { e =>
        for {
          expAmount <- originalCount.randomlyGenerateExpAmount[IO]
          _ <- spawnOrbAt(loc)(expAmount)
          _ <- IO(e.remove())
        } yield ()
      }
  }

  @EventHandler
  def onExpBottleHitBlock(event: ExpBottleEvent): Unit = {
    managedBottleScope.release(event.getEntity).unsafeRunSync()
  }

  //　経験値瓶を持った状態でのShift右クリック…一括使用
  @EventHandler
  def onPlayerRightClickExpBottleEvent(event: PlayerInteractEvent): Unit = {
    val player = event.getPlayer
    val playerInventory = player.getInventory
    val action = event.getAction

    // 経験値瓶を持った状態でShift右クリックをした場合
    if (player.isSneaking
      && playerInventory.getItemInMainHand != null
      && playerInventory.getItemInMainHand.getType == Material.EXP_BOTTLE
      && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {

      val bottleCount = BottleCount(playerInventory.getItemInMainHand.getAmount)
      val bottleResource = bottleResourceSpawningAt(player.getLocation, bottleCount)

      effectEnvironment.runEffectAsync(
        "経験値瓶の消費を待つ",
        managedBottleScope.useTracked(bottleResource) { _ => IO.never }
      )

      playerInventory.setItemInMainHand(new ItemStack(Material.AIR))
      event.setCancelled(true)
    }
  }
}
