package com.github.unchama.seichiassist.subsystems.autosave.bukkit.instances

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.autosave.application.CanSaveWorlds
import org.bukkit.{Bukkit, World}

import java.lang.reflect.Field
import scala.annotation.tailrec

object SyncCanSaveBukkitWorlds {

  def apply[F[_] : Sync]: CanSaveWorlds[F] = new CanSaveWorlds[F] {
    override val saveAllWorlds: F[Unit] = Sync[F].delay {
      def saveWorld(world: World): Unit = {
        // WARNを防ぐためMinecraftサーバーデフォルトの自動セーブは無効化
        val server = getFieldAsAccessibleField(Bukkit.getServer.getClass, "console")
          .getOrElse(return)
          .get(Bukkit.getServer)

        getFieldAsAccessibleField(server.getClass, "autosavePeriod")
          .getOrElse(return)
          .set(server, 0)

        world.save()
      }

      @tailrec
      def getFieldAsAccessibleField(clazz: Class[_], name: String): Option[Field] = {
        clazz.getDeclaredFields.find(_.getName.equals(name)) match {
          case s@Some(field) =>
            field.setAccessible(true)
            s
          case None =>
            clazz.getSuperclass match {
              case null => None
              case s => getFieldAsAccessibleField(s, name)
            }
        }
      }

      import scala.jdk.CollectionConverters._

      Bukkit.getServer.getWorlds.asScala.foreach(saveWorld)
    }
  }

}
