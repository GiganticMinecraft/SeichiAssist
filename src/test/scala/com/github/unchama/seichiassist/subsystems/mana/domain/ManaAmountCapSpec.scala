package com.github.unchama.seichiassist.subsystems.mana.domain

import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.{
  SeichiLevel,
  SeichiLevelTable
}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ManaAmountCapSpec extends AnyWordSpec with ScalaCheckPropertyChecks with Matchers {

  import cats.implicits._

  val checkpoints = Map(
    SeichiLevel.ofPositive(9) -> ManaAmount(0),
    SeichiLevel.ofPositive(10) -> ManaAmount(100),
    SeichiLevel.ofPositive(19) -> ManaAmount(190),
    SeichiLevel.ofPositive(200) -> ManaAmount(206400)
  )

  "ManaAmountCap" should {
    "agree on certain checkpoints" in {
      checkpoints.foreach {
        case (level, amount) =>
          assertResult(amount)(ManaAmountCap.at(level))
      }
    }
    "be increasing" in {
      val caps = SeichiLevelTable.table.levelRange.map(ManaAmountCap.at)
      assertResult(caps.sorted)(caps)
    }
  }
}
