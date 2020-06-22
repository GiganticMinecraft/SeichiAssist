package com.github.unchama.targetedeffect

import com.github.unchama.generic.tag.tag.@@
import org.bukkit.entity.Player

package object player {
  type ForcedPotionEffect = TargetedEffect[Player] @@ ForcedPotionEffect.Tag
}
