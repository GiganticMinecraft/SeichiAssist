package com.github.unchama.seichiassist

import com.github.unchama.seichiassist.ManagedWorld._
import org.bukkit.World
import org.scalamock.scalatest.MockFactory
import org.scalatest.Inspectors.forAll
import org.scalatest.wordspec.AnyWordSpec

/**
 * Created by karayuu on 2020/10/07
 */
class ManagedWorldSpec extends AnyWordSpec with MockFactory {
  val blockLineUpSkillEnableMap: Map[String, Boolean] = Map(
    "world_spawn" -> false,
    "world_2" -> true,
    "world_SW" -> true,
    "world_SW_2" -> true,
    "world_SW_3" -> true,
    "world_SW_4" -> true,
    "world_SW_nether" -> true,
    "world_SW_the_end" -> true,
    "world_dot" -> true,
    "world_build" -> true
  )

  val inTrackedWorldMap: Map[String, Boolean] = Map(
    "world_spawn" -> false,
    "world_2" -> true,
    "world_SW" -> true,
    "world_SW_2" -> true,
    "world_SW_3" -> true,
    "world_SW_4" -> true,
    "world_SW_nether" -> true,
    "world_SW_the_end" -> true,
    "world_dot" -> true,
    "world_build" -> true
  )

  "World.isBlockLineUpSkillEnable" should {
    "return the appropreate truth-value" in {
      forAll(blockLineUpSkillEnableMap) { case (worldName, value) =>
        val world = mock[World]
        (world.getName _).expects().returning(worldName)
        assert(world.isBlockLineUpSkillEnable == value)
      }
    }
  }

  "World.isTrackedBuildBlockWorld" should {
    "return the approprate truth-value" in {
      forAll(inTrackedWorldMap) { case (worldName, value) =>
        val world = mock[World]
        (world.getName _).expects().returning(worldName)
        assert(world.isTrackedBuildBlockWorld == value)
      }
    }
  }
}
