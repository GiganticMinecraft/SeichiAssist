package com.github.unchama.seichiassist.achievement

import com.github.unchama.seichiassist.achievement.NicknameMapping.NicknameCombination
import org.scalatest.wordspec.AnyWordSpec

class NickNameMappingSpec extends AnyWordSpec {
  "mapping" should {
    "not map to an empty combination" in {
      SeichiAchievement.values.foreach { achievement =>
        val NicknameCombination(first, second, third) = NicknameMapping.getNicknameCombinationFor(achievement)
        assert(List(first, second, third).flatten.nonEmpty)
      }
    }

    "refer only to the existing nicknames" in {
      def referenceExists(id: AchievementId, partSelector: NicknamesToBeUnlocked => Option[String]) =
        Nicknames.getNicknameFor(id).flatMap(partSelector).nonEmpty

      SeichiAchievement.values.foreach { achievement =>
        val NicknameCombination(first, second, third) = NicknameMapping.getNicknameCombinationFor(achievement)

        val selectFirst: NicknamesToBeUnlocked => Option[String] = _.head()
        val selectSecond: NicknamesToBeUnlocked => Option[String] = _.middle()
        val selectThird: NicknamesToBeUnlocked => Option[String] = _.tail()

        info(s"for achievement $achievement")

        assert {
          Seq(
            (first, selectFirst), (second, selectSecond), (third, selectThird)
          ).forall {
            case (Some(reference), selector) => referenceExists(reference, selector)
            case (None, _) => true
          }
        }
      }
    }
  }
}
