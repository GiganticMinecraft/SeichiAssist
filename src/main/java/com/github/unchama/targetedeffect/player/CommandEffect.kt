package com.github.unchama.targetedeffect.player

import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player

/**
 * Created by karayuu on 2019/06/23
 */

fun String.asCommandEffect() = TargetedEffect<Player> { it.performCommand(this) }
