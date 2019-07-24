package com.github.unchama.seichiassist.effect.arrow

import com.github.unchama.seichiassist.Schedulers
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.effect.FixedMetadataValueHolder
import com.github.unchama.targetedeffect.*
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

inline fun <reified P: Projectile> arrowEffect(spawnConfiguration: ProjectileSpawnConfiguration,
                                                                 sound: Sound? = null,
                                                                 crossinline projectileModifier: P.() -> Unit = {}): TargetedEffect<Player> =
    sequentialEffect(
        if (sound != null) FocusedSoundEffect(sound, 1.0f, 1.3f) else EmptyEffect,
        computedEffect { player ->
          val playerLocation = player.location.clone()

          unfocusedEffect {
            val spawnLocation = playerLocation.clone()
                .add(playerLocation.direction)
                .add(spawnConfiguration.offset)

            withContext(Schedulers.sync) {
              val projectile = playerLocation.world.spawn(spawnLocation, P::class.java)
                  .apply {
                    shooter = player
                    setGravity(spawnConfiguration.gravity)
                    setMetadata("ArrowSkill", FixedMetadataValueHolder.TRUE)
                    velocity = playerLocation.direction.clone().multiply(spawnConfiguration.speed)
                  }
                  .apply(projectileModifier)
                  .also { SeichiAssist.entitylist += it }

              delay(100 * 50)
              projectile.remove()
              SeichiAssist.entitylist -= projectile
            }
          }
        }
    )

object ArrowEffects {
  val singleArrowBlizzardEffect: TargetedEffect<Player> = arrowEffect<Snowball>(
      ProjectileSpawnConfiguration(
          1.0,
          Triple(0.0, 1.6, 0.0),
          false
      ),
      Sound.ENTITY_SNOWBALL_THROW
  )

  val singleArrowMagicEffect: TargetedEffect<Player> = run {
    val thrownPotionItem = ItemStack(Material.SPLASH_POTION).apply {
      itemMeta = (Bukkit.getItemFactory().getItemMeta(Material.SPLASH_POTION) as PotionMeta).apply {
        basePotionData = PotionData(PotionType.INSTANT_HEAL)
      }
    }

    arrowEffect<ThrownPotion>(
        ProjectileSpawnConfiguration(
            0.8,
            Triple(0.0, 1.6, 0.0),
            false
        ),
        Sound.ENTITY_WITCH_THROW
    ) { item = thrownPotionItem }
  }

  val singleArrowMeteoEffect: TargetedEffect<Player> = arrowEffect<ThrownPotion>(
      ProjectileSpawnConfiguration(
          1.0,
          Triple(0.0, 1.6, 0.0),
          false
      ),
      Sound.ENTITY_ARROW_SHOOT
  ) { isGlowing = true }

  val singleArrowExplosionEffect: TargetedEffect<Player> = arrowEffect<SmallFireball>(
      ProjectileSpawnConfiguration(
          0.4,
          Triple(0.0, 1.6, 0.0),
          false
      ),
      Sound.ENTITY_GHAST_SHOOT
  )
}
