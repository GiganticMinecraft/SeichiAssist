package com.github.unchama.seichiassist.subsystems.autosave.bukkit.task

import java.lang.reflect.Field

import org.bukkit.{Bukkit, World}

object WorldSaveTask {
  def saveWorld(world: World): Unit = {
    // WARNを防ぐためMinecraftサーバーデフォルトの自動セーブは無効化
    val server = getFieldAsAccessibleField(Bukkit.getServer.getClass, "console").getOrElse(return).get(Bukkit.getServer)
    getFieldAsAccessibleField(server.getClass, "autosavePeriod").getOrElse(return).set(server, 0)

    world.save()
  }

  private def getFieldAsAccessibleField(clazz: Class[_], name: String): Option[Field] = {  
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
}
