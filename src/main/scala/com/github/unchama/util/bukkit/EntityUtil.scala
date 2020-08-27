package com.github.unchama.util.bukkit

import cats.effect.Sync
import org.bukkit.Location
import org.bukkit.entity.Entity

import scala.reflect.ClassTag

object EntityUtil {

  def spawn[F[_], EntityType <: Entity : ClassTag](location: Location)
                                                  (implicit F: Sync[F]): F[EntityType] = {
    val classTag = implicitly[ClassTag[EntityType]]
    val entityClass = classTag.runtimeClass.asSubclass(classOf[Entity])

    import cats.implicits._

    for {
      entity <- F.delay {
        location.getWorld.spawn(location, entityClass)
      }
      refinedEntity <- entity match {
        case entity: EntityType =>
          F.pure(entity)
        case _ =>
          F.delay {
            entity.remove()
          } >> F.raiseError(
            new ClassCastException(s"Spawned entity is not an instance of $classTag")
          )
      }
    } yield refinedEntity
  }

}
