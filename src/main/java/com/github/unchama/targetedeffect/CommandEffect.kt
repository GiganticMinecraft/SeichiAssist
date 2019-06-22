package com.github.unchama.targetedeffect

import org.bukkit.entity.Player

/**
 * Created by karayuu on 2019/06/23
 */

fun String.asCommandEffect() =
    TargetedEffect { player: Player ->
      player.chat(this)
    }
