package com.github.unchama.seichiassist.listener

import cats.effect.{IO, Resource}
import com.github.unchama.generic.effect.ResourceScope
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import org.bukkit.{Location, Material}
import org.bukkit.entity.{ExperienceOrb, ThrownExpBottle}
import org.bukkit.event.block.Action
import org.bukkit.event.entity.ExpBottleEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack

import scala.util.Random

class ExpBottleStackUsageController(managedBottleScope: ResourceScope[IO, ThrownExpBottle])
                                   (implicit effectEnvironment: EffectEnvironment) extends Listener {
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

      // 一つに付きもたらされる経験値量は3..11。ソースはGamepedia
      val exp = {
        val expBottleAmount = playerInventory.getItemInMainHand.getAmount
        (0 until expBottleAmount).map(_ => Random.nextInt(9 /* Exclusive */) + 3).sum
      }

      def spawnOrbAt(location: Location): IO[Unit] = IO {
        val orb = location.getWorld.spawn(
          location,
          classOf[ExperienceOrb]
        )
        orb.setExperience(exp)
      }

      def bottleResourceSpawningAt(loc: Location): Resource[IO, ThrownExpBottle] = {
        import cats.implicits._
        Resource
          .make(
            IO(loc.getWorld.spawn(loc, classOf[ThrownExpBottle]))
          ) { e =>
            spawnOrbAt(e.getLocation) >> IO(e.remove())
          }
      }

      effectEnvironment.runEffectAsync(
        "経験値瓶の消費を待つ",
        managedBottleScope.useTracked(bottleResourceSpawningAt(player.getLocation)) { _ => IO.never }
      )

      playerInventory.setItemInMainHand(new ItemStack(Material.AIR))
      event.setCancelled(true)
    }
  }
}
