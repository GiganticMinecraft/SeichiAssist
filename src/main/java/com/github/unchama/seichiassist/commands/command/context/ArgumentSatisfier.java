package com.github.unchama.seichiassist.commands.command.context;

import org.bukkit.ChatColor;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author unicroak
 */
public final class ArgumentSatisfier<T> {

    private final Function<String, Optional<T>> transform;
    private final Predicate<T> binding;
    private final Consumer<CommandContext> failOnTransform;
    private final Consumer<CommandContext> failOnBind;

    private ArgumentSatisfier(Function<String, Optional<T>> transform,
                              Predicate<T> binding,
                              Consumer<CommandContext> failOnTransform,
                              Consumer<CommandContext> failOnBind) {
        this.transform = transform;
        this.binding = binding;
        this.failOnTransform = failOnTransform;
        this.failOnBind = failOnBind;
    }

    public static <T> ArgumentSatisfier<T> of(Function<String, Optional<T>> transform,
                                              Predicate<T> binding,
                                              Consumer<CommandContext> failOnTransform,
                                              Consumer<CommandContext> failOnBind) {
        return new ArgumentSatisfier<>(transform, binding, failOnTransform, failOnBind);
    }

    public static <T> ArgumentSatisfier<T> of(Function<String, Optional<T>> transform, Predicate<T> binding) {
        return ArgumentSatisfier.of(
                transform,
                binding,
                context -> context.getSender().sendMessage(ChatColor.RED + "コマンドの引数に不正な値が存在します"),
                context -> context.getSender().sendMessage(ChatColor.RED + "コマンドの値が範囲外に到達しています")
        );
    }

    public static <T> ArgumentSatisfier<T> of(Function<String, Optional<T>> transform) {
        return ArgumentSatisfier.of(transform, argument -> true);
    }

    public Optional<T> tryTransform(String rawArgument) {
        return this.transform.apply(rawArgument).filter(binding);
    }

    public boolean confirmBound(T argument) {
        return binding.test(argument);
    }

    public void ifFailOnTransform(CommandContext context) {
        failOnTransform.accept(context);
    }

    public void ifFailOnBind(CommandContext context) {
        failOnBind.accept(context);
    }

}
