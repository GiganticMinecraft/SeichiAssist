package com.github.unchama.seichiassist.commands.command.context;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * @author unicroak
 */
public final class CommandContext {

    private final CommandSender commandSender;
    private final List<String> rawArgumentList;
    private final List<ArgumentSatisfier> argumentList;

    public CommandContext(CommandSender commandSender, List<String> rawArgumentList, List<ArgumentSatisfier> argumentList) {
        this.commandSender = commandSender;
        this.rawArgumentList = rawArgumentList;
        this.argumentList = argumentList;
    }

    public CommandSender getSender() {
        return commandSender;
    }

    @SuppressWarnings("unchecked")
    public <T> T getArgumentAt(int order) {
        String rawArgument = rawArgumentList.get(order);

        return (T) argumentList.get(order).transform(rawArgument);
    }

}
