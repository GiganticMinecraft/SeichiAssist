package com.github.unchama.seichiassist.activeskill.effect.arrow

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.util.effect.BukkitResources
import org.bukkit.entity._
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.{PotionData, PotionType}
import org.bukkit.{Bukkit, Material, Sound}

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

object ArrowEffects {

  import cats.implicits._
  import com.github.unchama.concurrent.syntax._
  import com.github.unchama.targetedeffect._
  import com.github.unchama.util.syntax._

  implicit val plugin: JavaPlugin = SeichiAssist.instance

  val normalArrowEffect: TargetedEffect[Player] = arrowEffect[Arrow](
    ProjectileSpawnConfiguration(
      1.0,
      (0.0, 1.6, 0.0)
    ),
    Some(Sound.ENTITY_ARROW_SHOOT)
  )

  val singleArrowBlizzardEffect: TargetedEffect[Player] = arrowEffect[Snowball](
    ProjectileSpawnConfiguration(
      1.0,
      (0.0, 1.6, 0.0)
    ),
    Some(Sound.ENTITY_SNOWBALL_THROW)
  )
  val singleArrowMagicEffect: TargetedEffect[Player] = {
    val thrownPotionItem = new ItemStack(Material.SPLASH_POTION).modify { itemStack =>
      itemStack.setItemMeta {
        Bukkit.getItemFactory
          .getItemMeta(Material.SPLASH_POTION)
          .asInstanceOf[PotionMeta]
          .modify(_.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL)))
      }
    }

    arrowEffect[ThrownPotion](
      ProjectileSpawnConfiguration(
        0.8,
        (0.0, 1.6, 0.0)
      ),
      Some(Sound.ENTITY_WITCH_THROW),
      _.setItem(thrownPotionItem)
    )
  }

  val singleArrowMeteoEffect: TargetedEffect[Player] =
    arrowEffect[Arrow](
      ProjectileSpawnConfiguration(
        1.0,
        (0.0, 1.6, 0.0)
      ),
      Some(Sound.ENTITY_ARROW_SHOOT),
      _.setGlowing(true)
    )

  val singleArrowExplosionEffect: TargetedEffect[Player] =
    arrowEffect[SmallFireball](
      ProjectileSpawnConfiguration(
        0.4,
        (0.0, 1.6, 0.0)
      ),
      Some(Sound.ENTITY_GHAST_SHOOT)
    )

  def arrowEffect[P <: Projectile : ClassTag](spawnConfiguration: ProjectileSpawnConfiguration,
                                              sound: Option[Sound] = None,
                                              projectileModifier: P => Unit = (_: P) => ()): TargetedEffect[Player] = {

    val runtimeClass = implicitly[ClassTag[P]].runtimeClass.asInstanceOf[Class[P]]

    val soundEffect = sound.map(FocusedSoundEffect(_, 1.0f, 1.3f)).getOrElse(emptyEffect)

    val waitForCollision = IO.sleep(100.ticks)(IO.timer(ExecutionContext.global))

    sequentialEffect(
      soundEffect,
      Kleisli(player =>
        for {
          _ <- PluginExecutionContexts.syncShift.shift
          playerLocation <- IO { player.getLocation.clone() }
          spawnLocation = playerLocation.clone()
            .add(playerLocation.getDirection)
            .add(spawnConfiguration.offset)

          modifyProjectile = (projectile: P) => IO {
            projectile.modify { p => import p._
              setShooter(player)
              setGravity(spawnConfiguration.gravity)
              setVelocity(playerLocation.getDirection.clone().multiply(spawnConfiguration.speed))
            }
          }

          /**
           * 100ティック衝突を待ってから開放する。
           *
           * 飛翔体をスコープ内でのリソースとしているのは、
           * サーバーが停止したときにも開放するためである。
           */
          _ <- SeichiAssist.instance.arrowSkillProjectileScope
            .useTracked(BukkitResources.vanishingEntityResource(spawnLocation, runtimeClass)) { projectile =>
              modifyProjectile(projectile) *> waitForCollision
            }
        } yield ()
      )
    )
  }
}
