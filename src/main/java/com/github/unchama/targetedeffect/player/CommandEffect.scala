package com.github.unchama.targetedeffect.player

/**
 * Created by karayuu on 2019/06/23
 */

def String.asCommandEffect() = TargetedEffect<Player> { it.performCommand(this) }
