package com.github.unchama.seichiassist.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by karayuu on 2018/04/18
 */
abstract class CustomEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
