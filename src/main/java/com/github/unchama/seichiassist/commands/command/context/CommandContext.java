package com.github.unchama.seichiassist.commands.command.context;

import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;

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
    public <T> Optional<T> getArgumentAt(int order) {
        String rawArgument = rawArgumentList.get(order);

        return (Optional<T>) argumentList.get(order).tryTransform(rawArgument);
    }

}
