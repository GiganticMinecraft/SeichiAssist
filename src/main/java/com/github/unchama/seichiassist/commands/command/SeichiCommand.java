package com.github.unchama.seichiassist.commands.command;

import com.github.unchama.seichiassist.commands.command.context.ArgumentSatisfier;
import com.github.unchama.seichiassist.commands.command.context.CommandContext;
import com.github.unchama.seichiassist.commands.command.result.CommandResult;
import com.github.unchama.seichiassist.commands.command.result.ExecutedOnConsole;
import com.github.unchama.seichiassist.commands.command.result.IllegalArgument;
import com.github.unchama.seichiassist.commands.command.result.NotEnoughArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author unicroak
 */
public final class SeichiCommand {

    private final List<ArgumentSatisfier> satisfierList;
    private final Map<String, Consumer<CommandContext>> resultActionMap;
    private boolean mustBeExecutedByPlayer;
    private Function<CommandContext, CommandResult> action;

    private SeichiCommand() {
        this.satisfierList = new ArrayList<>(64);
        this.resultActionMap = new HashMap<>();
    }

    static boolean execute(Consumer<SeichiCommand> creator, CommandSender sender, String[] rawArguments) {
        SeichiCommand seichiCommand = new SeichiCommand();
        CommandContext commandContext = new CommandContext(sender, Arrays.asList(rawArguments), seichiCommand.satisfierList);

        CommandResult result;
        if (seichiCommand.mustBeExecutedByPlayer && !(sender instanceof Player)) {
            result = new ExecutedOnConsole();
        } else {
            try {
                creator.accept(seichiCommand);
                seichiCommand.action.apply(commandContext);

                result = new CommandResult.Success();
            } catch (IllegalArgumentException | ClassCastException ex) {
                result = new IllegalArgument();
            } catch (IndexOutOfBoundsException ex) {
                result = new NotEnoughArgument();
            }
        }

        seichiCommand
                .resultActionMap
                .getOrDefault(result.getClass().getName(), context -> {
                })
                .accept(commandContext);

        return result.isSuccess();
    }

    @SuppressWarnings("unchecked")
    public <A> SeichiCommand satisfyArgumentAt(int order, Function<String, A> transform, Predicate<A> binding) {
        this.satisfierList.add(order, new ArgumentSatisfier(transform, binding));
        return this;
    }

    public <A> SeichiCommand satisfyArgumentAt(int order, Function<String, A> transform) {
        return this.satisfyArgumentAt(order, transform, argument -> true);
    }

    // TODO: refactor
    public <R extends CommandResult> SeichiCommand ifOn(Class<R> clazz, Consumer<CommandContext> action) {
        String clazzName = clazz.getName();
        resultActionMap.put(clazzName, action);
        return this;
    }

    public SeichiCommand mustBeExecutedByPlayer(boolean mustBeExecutedByPlayer) {
        this.mustBeExecutedByPlayer = mustBeExecutedByPlayer;
        return this;
    }

    public SeichiCommand execute(Function<CommandContext, CommandResult> action) {
        this.action = action;
        return this;
    }

    public SeichiCommand withChild(String label, Function<ArgumentSatisfier, SeichiCommand> childCommand) {
        return this; // TODO: implement
    }

}
