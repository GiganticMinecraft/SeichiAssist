package com.github.unchama.buildassist.listener

import org.bukkit.event.Event
import org.bukkit.event.Listener

/**
 * ラムダ用リスナー
 * [org.bukkit.event.EventHandler]は手動で付ける必要があります
 */
interface TypedEventListener<E : Event> : Listener {
  fun onEvent(event: E)
}