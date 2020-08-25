package com.github.unchama.seichiassist.subsystems.expbottlestack.bukkit.listeners

import cats.effect.Effect
import com.github.unchama.generic.effect.ResourceScope
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.subsystems.expbottlestack.bukkit.Resources
import com.github.unchama.seichiassist.subsystems.expbottlestack.domain.BottleCount
import org.bukkit.Material
import org.bukkit.entity.ThrownExpBottle
import org.bukkit.event.block.Action
import org.bukkit.event.entity.ExpBottleEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack

class ExpBottleStackUsageController[F[_]](implicit managedBottleScope: ResourceScope[F, F, ThrownExpBottle],
                                          effectEnvironment: EffectEnvironment,
                                          F: Effect[F]) extends Listener {

  @EventHandler
  def onExpBottleHitBlock(event: ExpBottleEvent): Unit = {
    val bottle = event.getEntity

    if (F.toIO(managedBottleScope.isTracked(bottle)).unsafeRunSync()) {
      event.setExperience(0)
      F.toIO(managedBottleScope.getReleaseAction(event.getEntity)).unsafeRunSync()
    }
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
      val bottleResource = Resources.bottleResourceSpawningAt[F](player.getLocation, bottleCount)

      effectEnvironment.runEffectAsync(
        "経験値瓶の消費を待つ",
        managedBottleScope.useTracked[ThrownExpBottle, Nothing](bottleResource) { _ => F.never }
      )

      playerInventory.setItemInMainHand(new ItemStack(Material.AIR))
      event.setCancelled(true)
    }
  }
}
