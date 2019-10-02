package com.github.unchama.buildassist.listener

import org.bukkit.event.{Event, Listener}

/**
 * ラムダ用リスナー
 * [org.bukkit.event.EventHandler]は手動で付ける必要があります
 */
trait TypedEventListener[E <: Event] extends Listener {
  def onEvent(event: E): Unit
}
