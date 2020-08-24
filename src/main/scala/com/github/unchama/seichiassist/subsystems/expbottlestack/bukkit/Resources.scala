package com.github.unchama.seichiassist.subsystems.expbottlestack.bukkit

import cats.effect.{IO, Resource}
import com.github.unchama.seichiassist.subsystems.expbottlestack.domain.BottleCount
import com.github.unchama.util.bukkit.EntityUtil
import org.bukkit.Location
import org.bukkit.entity.{ExperienceOrb, ThrownExpBottle}

object Resources {
  def bottleResourceSpawningAt(loc: Location, originalCount: BottleCount): Resource[IO, ThrownExpBottle] = {

    Resource
      .make(
        EntityUtil.spawn[IO, ThrownExpBottle](loc)
      ) { bottle =>
        for {
          _ <- IO {
            bottle.remove()
          }
          expAmount <- originalCount.randomlyGenerateExpAmount[IO]
          orb <- EntityUtil.spawn[IO, ExperienceOrb](loc)
          _ <- IO {
            orb.setExperience(expAmount)
          }
        } yield ()
      }
  }
}
