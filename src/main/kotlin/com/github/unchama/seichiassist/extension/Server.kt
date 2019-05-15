package com.github.unchama.seichiassist.extension

import com.github.unchama.seichiassist.SeichiAssist
import org.bukkit.scheduler.BukkitRunnable

/**
 * @author tar0ss
 * @author unchama
 */
fun runTaskLater(delay: Long, action: () -> Unit) {
    object : BukkitRunnable() {
        override fun run() {
            action()
        }
    }.runTaskLater(SeichiAssist.instance, delay)
}

fun runTaskAsync(action: () -> Unit) {
    object : BukkitRunnable() {
        override fun run() {
            action()
        }
    }.runTaskAsynchronously(SeichiAssist.instance)
}

fun runTaskLaterAsync(delay: Long, action: () -> Unit) {
    object : BukkitRunnable() {
        override fun run() {
            action()
        }
    }.runTaskLaterAsynchronously(SeichiAssist.instance, delay)
}