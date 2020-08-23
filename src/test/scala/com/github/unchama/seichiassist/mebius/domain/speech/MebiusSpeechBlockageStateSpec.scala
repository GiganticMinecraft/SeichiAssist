package com.github.unchama.seichiassist.mebius.domain.speech

import cats.effect.IO
import com.github.unchama.seichiassist.subsystems.mebius.domain.speech.MebiusSpeechBlockageState
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Random

class MebiusSpeechBlockageStateSpec extends AnyWordSpec {

  "BlockageState" should {
    "block whenever set to block" in {
      val trialCount = 1000
      val stateRef = new MebiusSpeechBlockageState[IO]

      List.fill[Boolean](trialCount)(Random.nextBoolean()).foreach { shouldBlock =>
        val blockAction = if (shouldBlock) stateRef.block else stateRef.unblock

        blockAction.unsafeRunSync()

        assert {
          !shouldBlock || stateRef.shouldBlock.unsafeRunSync()
        }
      }
    }
  }

}
