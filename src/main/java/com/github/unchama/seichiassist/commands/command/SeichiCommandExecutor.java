package com.github.unchama.seichiassist.commands.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author unicroak
 */
public abstract class SeichiCommandExecutor implements TabExecutor {

    public abstract Consumer<SeichiCommand> createCommand();

    @Override
    public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        SeichiCommand.execute(createCommand(), sender, args);

        return true;
    }

    @Override
    public final List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return null;
    }

}
