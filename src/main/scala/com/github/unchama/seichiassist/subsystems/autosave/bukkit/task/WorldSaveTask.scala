package com.github.unchama.seichiassist.subsystems.autosave.bukkit.task

import java.lang.reflect.Field

import org.bukkit.{Bukkit, World}

object WorldSaveTask {
  def saveWorld(world: World): Unit = {
    // WARNを防ぐためMinecraftサーバーデフォルトの自動セーブは無効化
    val server = getField(Bukkit.getServer.getClass, "console").getOrElse(return).get(Bukkit.getServer)
    getField(server.getClass, "autosavePeriod").getOrElse(return).set(server, 0)

    world.save()
  }

  private def getField(clazz: Class[_], name: String): Option[Field] = {
    var c = clazz
    do for (field <- c.getDeclaredFields) {
      if (field.getName.equals(name)) {
        field.setAccessible(true)
        return Some(field)
      }
    } while ({
      c = c.getSuperclass
      c.getSuperclass != null
    })
    None
  }
}
