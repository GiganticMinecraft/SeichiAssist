package com.github.unchama.util.bukkit

import cats.effect.Sync
import org.bukkit.Location
import org.bukkit.entity.Entity

import scala.reflect.ClassTag

object EntityUtil {

  def spawn[F[_] : Sync, EntityType <: Entity : ClassTag](location: Location): F[EntityType] = {
    val entityClass = implicitly[ClassTag[EntityType]].runtimeClass.asSubclass(classOf[Entity])

    Sync[F].delay {
      location.getWorld.spawn(location, entityClass)
    }
  }

}
