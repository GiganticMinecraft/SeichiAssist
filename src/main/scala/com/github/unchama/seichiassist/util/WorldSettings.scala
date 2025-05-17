package com.github.unchama.seichiassist.util

import cats.effect.IO
import org.bukkit.{Bukkit, Difficulty}

object WorldSettings {
  import cats.implicits._

  def setDifficulty(worldNameList: List[String], difficulty: Difficulty): IO[Unit] = {
    worldNameList.traverse { name =>
      IO {
        val world = Bukkit.getWorld(name)

        if (world == null)
          Bukkit.getLogger.warning(name + "という名前のワールドは存在しません。")
        else
          world.setDifficulty(difficulty)
      }
    }.void
  }
}
