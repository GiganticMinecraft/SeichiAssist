package com.github.unchama.seichiassist.util

import com.github.unchama.seichiassist.ManagedWorld._
import com.github.unchama.seichiassist.SeichiAssist
import org.bukkit.World
import org.scalamock.scalatest.MockFactory
import org.scalatest.Inspectors.forAll
import org.scalatest.wordspec.AnyWordSpec

import scala.annotation.nowarn

/**
 * Test for BreakUtil methods
 */
class BreakUtilSpec extends AnyWordSpec with MockFactory {
  
  // Mock SeichiAssist config for testing
  val mockSeichiAssistConfig = mock[com.github.unchama.seichiassist.SeichiAssistConfig]
  
  // Test data for getHalfBlockLayerYCoordinate
  val halfBlockLayerYCoordinateMap: Map[String, Int] = Map(
    "world_SW" -> 5,
    "world_SW_2" -> 5,
    "world_SW_3" -> 5, // Will be 1 for server 5, but we're testing with default server
    "world_SW_4" -> 5,
    "world_SW_nether" -> 5,
    "world_SW_the_end" -> 0,
    "world_2" -> 5,
    "world_spawn" -> 5,
    "world_build" -> 5,
    "world_dot" -> 5
  )
  
  // Test data for getManualHalfBlockBreakYLimit
  val manualHalfBlockBreakYLimitMap: Map[String, Int] = Map(
    "world_SW" -> -59,
    "world_SW_2" -> -59,
    "world_SW_3" -> -59, // Will be 1 for server 5, but we're testing with default server
    "world_SW_4" -> -59,
    "world_SW_nether" -> 5,
    "world_SW_the_end" -> 0,
    "world_2" -> -59,
    "world_spawn" -> -59,
    "world_build" -> -59,
    "world_dot" -> -59
  )

  "BreakUtil.getHalfBlockLayerYCoordinate" should {
    "return the appropriate Y coordinate for each world" in {
      forAll(halfBlockLayerYCoordinateMap) {
        case (worldName, expectedY) =>
          @nowarn
          val world = mock[World]
          (world.getName _).expects().returning(worldName)
          assert(BreakUtil.getHalfBlockLayerYCoordinate(world) == expectedY)
      }
    }
  }

  "BreakUtil.getManualHalfBlockBreakYLimit" should {
    "return the appropriate Y limit for each world" in {
      forAll(manualHalfBlockBreakYLimitMap) {
        case (worldName, expectedYLimit) =>
          @nowarn
          val world = mock[World]
          (world.getName _).expects().returning(worldName)
          assert(BreakUtil.getManualHalfBlockBreakYLimit(world) == expectedYLimit)
      }
    }
  }
}