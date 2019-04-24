package com.github.unchama.seichiassist.commands.command;

import com.github.unchama.seichiassist.commands.command.context.ArgumentSatisfier;
import com.github.unchama.seichiassist.commands.command.context.CommandContext;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author unicroak
 */
public final class SeichiCommand {

    private final List<ArgumentSatisfier> satisfierList;
    private final Map<String, Consumer<SeichiCommand>> childCommandMap;

    private Optional<Consumer<CommandContext>> action;

    private boolean mustBeExecutedByPlayer;
    private Consumer<CommandContext> failByExecutingOnConsole;

    private SeichiCommand() {
        this.satisfierList = new ArrayList<>(64);
        this.childCommandMap = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    static boolean execute(Consumer<SeichiCommand> builder, CommandSender sender, String[] rawArguments) {
        final SeichiCommand command = new SeichiCommand();
        builder.accept(command);
        final CommandContext context = new CommandContext(sender, Arrays.asList(rawArguments), command.satisfierList);

        if (command.mustBeExecutedByPlayer && !(sender instanceof Player)) {
            command.failByExecutingOnConsole.accept(context);
            return true;
        }

        if (rawArguments.length != 0) {
            Optional<String> labelOptional = Arrays.stream(rawArguments)
                    .map(String::toUpperCase)
                    .filter(command.childCommandMap::containsKey)
                    .findFirst();

            if (labelOptional.isPresent()) {
                SeichiCommand.execute(command.childCommandMap.get(labelOptional.get()), sender, rawArguments);
                return true;
            }
        }

        if (rawArguments.length != command.satisfierList.size()) {
            return false;
        }

        for (int index = 0; index < rawArguments.length; index++) {
            final String rawArgument = rawArguments[index];
            final ArgumentSatisfier satisfier = command.satisfierList.get(index);

            final Optional argumentOptional = satisfier.tryTransform(rawArgument);
            if (!argumentOptional.isPresent()) {
                satisfier.ifFailOnTransform(context);
                return true;
            } else if (!satisfier.confirmBound(argumentOptional.get())) {
                satisfier.ifFailOnBind(context);
                return true;
            }
        }

        if (!command.action.isPresent()) {
            return false;
        } else {
            command.action.get().accept(context);
            return true;
        }
    }

    public SeichiCommand mustBeExecutedByPlayer(boolean mustBeExecutedByPlayer, Consumer<CommandContext> ifFail) {
        this.mustBeExecutedByPlayer = mustBeExecutedByPlayer;
        this.failByExecutingOnConsole = ifFail;

        return this;
    }

    public SeichiCommand mustBeExecutedByPlayer(boolean mustBeExecutedByPlayer) {
        this.mustBeExecutedByPlayer(
                mustBeExecutedByPlayer,
                context -> context.getSender().sendMessage(ChatColor.RED + "This command must be executed by player!")
        );
        return this;
    }

    @SuppressWarnings("unchecked")
    public <S> SeichiCommand satisfyArgumentAt(int order, ArgumentSatisfier<S> satisfier) {
        this.satisfierList.add(order, satisfier);
        return this;
    }

    public SeichiCommand execute(Consumer<CommandContext> action) {
        this.action = Optional.of(action);
        return this;
    }

    public SeichiCommand withChild(String label, Consumer<SeichiCommand> childCommandBuilder) {
        childCommandMap.put(label.toUpperCase(), childCommandBuilder);
        return this;
    }

}
