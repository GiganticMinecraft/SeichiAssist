package com.github.unchama.buildassist.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * ラムダ用通常優先度のリスナー
 */
interface TypedEventListener<E> : Listener {
  @EventHandler
  fun onEvent(event: E)
}