package com.github.unchama.seichiassist

import cats.effect.IO
import org.bukkit.entity.Player

package object achievement {
  type AchievementId = Int
  type PartialNickname = String
  type Nickname = String
  type PlayerPredicate = Player => IO[Boolean]
  type ParameterizedText[A] = A => String
}
