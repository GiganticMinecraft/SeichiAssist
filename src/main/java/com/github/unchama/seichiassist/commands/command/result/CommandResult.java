package com.github.unchama.seichiassist.commands.command.result;

/**
 * @author unicroak
 */
public interface CommandResult {

    boolean isSuccess();

    /* open */ class Success implements CommandResult {

        @Override
        public boolean isSuccess() {
            return true;
        }

    }

    interface Failure extends CommandResult {

        @Override
        default boolean isSuccess() {
            return false;
        }

    }

}
