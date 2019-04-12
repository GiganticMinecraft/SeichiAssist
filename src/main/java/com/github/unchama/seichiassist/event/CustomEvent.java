package com.github.unchama.seichiassist.event;

import org.bukkit.event.*;

/**
 * Created by karayuu on 2018/04/18
 */
abstract class CustomEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Override
    public HandlerList getHandlers() { return HANDLER_LIST; }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
