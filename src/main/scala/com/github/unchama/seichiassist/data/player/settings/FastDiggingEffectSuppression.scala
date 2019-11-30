package com.github.unchama.seichiassist.data.player.settings

import cats.effect.IO
import com.github.unchama.targetedeffect
import org.bukkit.ChatColor._
import org.bukkit.command.CommandSender

class FastDiggingEffectSuppression {

  import com.github.unchama.targetedeffect._
  import com.github.unchama.targetedeffect.syntax._

  private var internalValue = 0

  def currentStatus(): IO[String] = IO {
    s"$RESET" + {
      internalValue match {
        case 0 => s"${GREEN}現在有効です(無制限)"
        case 1 => s"${GREEN}現在有効です$YELLOW(127制限)"
        case 2 => s"${GREEN}現在有効です$YELLOW(200制限)"
        case 3 => s"${GREEN}現在有効です$YELLOW(400制限)"
        case 4 => s"${GREEN}現在有効です$YELLOW(600制限)"
        case _ => s"${RED}現在OFFです"
      }
    }
  }

  def nextToggledStatus(): IO[String] = IO {
    internalValue match {
      case 0 => "127制限"
      case 1 => "200制限"
      case 2 => "400制限"
      case 3 => "600制限"
      case 4 => "OFF"
      case _ => "無制限"
    }
  }

  def isSuppressionActive: IO[Boolean] = IO {
    (0 to 4).contains(internalValue)
  }

  def serialized(): IO[Int] = IO {
    internalValue
  }

  def setStateFromSerializedValue(value: Int): IO[Unit] = IO {
    internalValue = value
  }

  def maximumAllowedEffectAmplifier(): IO[Int] = IO {
    internalValue match {
      case 0 => 25565
      case 1 => 127
      case 2 => 200
      case 3 => 400
      case 4 => 600
      case _ => 0
    }
  }

  val suppressionDegreeToggleEffect: TargetedEffect[CommandSender] = {
    import com.github.unchama.generic.syntax._

    targetedeffect.UnfocusedEffect {
      internalValue = (internalValue + 1) % 6
    }.followedBy {
      deferredEffect(IO {
        {
          internalValue match {
            case 0 => s"${GREEN}採掘速度上昇効果:ON(無制限)"
            case 1 => s"${GREEN}採掘速度上昇効果:ON(127制限)"
            case 2 => s"${GREEN}採掘速度上昇効果:ON(200制限)"
            case 3 => s"${GREEN}採掘速度上昇効果:ON(400制限)"
            case 4 => s"${GREEN}採掘速度上昇効果:ON(600制限)"
            case _ => s"${RED}採掘速度上昇効果:OFF"
          }
          }.asMessageEffect()
      })
    }
  }
}