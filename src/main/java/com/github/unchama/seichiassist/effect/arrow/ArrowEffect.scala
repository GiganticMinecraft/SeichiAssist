package com.github.unchama.seichiassist.effect.arrow

import com.github.unchama.seichiassist.effect.FixedMetadataValues
import com.github.unchama.seichiassist.{Schedulers, SeichiAssist}
import com.github.unchama.targetedeffect
import com.github.unchama.targetedeffect.TargetedEffect.TargetedEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.{EmptyEffect, UnfocusedEffect}
import org.bukkit.entity.{Player, Projectile, Snowball, ThrownPotion}
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionType
import org.bukkit.{Bukkit, Material, Sound}

object ArrowEffects {
  def arrowEffect[reified P <: Projectile](
    spawnConfiguration: ProjectileSpawnConfiguration,
    sound: Sound? = null,
  projectileModifier: P => Unit
  =
  {}
  ): targetedeffect.TargetedEffect[Player] =
    sequentialEffect(
      if (sound != null) FocusedSoundEffect(sound, 1.0f, 1.3f) else EmptyEffect,
      computedEffect { player =>
        val playerLocation = player.location.clone()

        UnfocusedEffect {
          val spawnLocation = playerLocation.clone()
            .add(playerLocation.direction)
            .add(spawnConfiguration.offset)

          withContext(Schedulers.sync) {
            val projectile = playerLocation.world.spawn(spawnLocation, P::class.java)
            .apply {
              shooter = player
              setGravity(spawnConfiguration.gravity)
              setMetadata("ArrowSkill", FixedMetadataValues.TRUE)
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

  val singleArrowBlizzardEffect: TargetedEffect[Player] = arrowEffect[Snowball](
      ProjectileSpawnConfiguration(
          1.0,
          Triple(0.0, 1.6, 0.0),
          false
      ),
      Sound.ENTITY_SNOWBALL_THROW
  )

  val singleArrowMagicEffect: TargetedEffect[Player] = run {
    val thrownPotionItem = ItemStack(Material.SPLASH_POTION).apply {
      itemMeta = (Bukkit.getItemFactory().getItemMeta(Material.SPLASH_POTION).asInstanceOf[PotionMeta]).apply {
        basePotionData = PotionData(PotionType.INSTANT_HEAL)
      }
    }

    arrowEffect[ThrownPotion](
        ProjectileSpawnConfiguration(
            0.8,
            Triple(0.0, 1.6, 0.0),
            false
        ),
        Sound.ENTITY_WITCH_THROW
    ) { item = thrownPotionItem }
  }

  val singleArrowMeteoEffect: TargetedEffect[Player] = arrowEffect[ThrownPotion](
      ProjectileSpawnConfiguration(
          1.0,
          Triple(0.0, 1.6, 0.0),
          false
      ),
      Sound.ENTITY_ARROW_SHOOT
  ) { isGlowing = true }

  val singleArrowExplosionEffect: TargetedEffect[Player] = arrowEffect[SmallFireball](
      ProjectileSpawnConfiguration(
          0.4,
          Triple(0.0, 1.6, 0.0),
          false
      ),
      Sound.ENTITY_GHAST_SHOOT
  )
}
