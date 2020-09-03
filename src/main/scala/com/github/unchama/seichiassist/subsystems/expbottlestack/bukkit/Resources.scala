package com.github.unchama.seichiassist.subsystems.expbottlestack.bukkit

import cats.effect.{Resource, Sync}
import com.github.unchama.seichiassist.subsystems.expbottlestack.domain.BottleCount
import com.github.unchama.util.bukkit.EntityUtil
import org.bukkit.Location
import org.bukkit.entity.{ExperienceOrb, ThrownExpBottle}

object Resources {
  def bottleResourceSpawningAt[F[_]](loc: Location, originalCount: BottleCount)
                                    (implicit F: Sync[F]): Resource[F, ThrownExpBottle] = {
    import cats.implicits._

    Resource
      .make(
        EntityUtil.spawn[F, ThrownExpBottle](loc)
      ) { bottle =>
        for {
          _ <- F.delay {
            bottle.remove()
          }
          expAmount <- originalCount.randomlyGenerateExpAmount[F]
          orb <- EntityUtil.spawn[F, ExperienceOrb](loc)
          _ <- F.delay {
            orb.setExperience(expAmount)
          }
        } yield ()
      }
  }
}
