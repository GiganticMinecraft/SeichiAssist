package com.github.unchama.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.unchama.util.ActionStatus.Ok;

/**
 * @author kory
 *
 * 失敗する可能性のある処理を実行するための構造を備えたインターフェース
 * @param <F> 失敗時の値
 */
public interface Try<F> {

    /**
     * このインスタンスが{@link SuccessfulTry}ならば、{@code action}を実行し、
     * {@link FailedTry}ならば{@code action}は実行せず内部状態を保つ。
     *
     *
     * @param action 実行する処理
     * @param failValue 失敗したときに{@link FailedTry}の構築に使用される値
     * @return {@code action}が実行された場合、結果が{@link ActionStatus#Ok}ならば
     * {@link SuccessfulTry}を返し、そうでなければ{@code failValue}が入った
     * {@link FailedTry}を返す。
     */
    Try<F> ifOkThen(F failValue, Supplier<ActionStatus> action);

    /**
     * @return このインスタンスが {@link FailedTry} ならば失敗時の値を含んだ {@link Optional} を、
     * そうでなければ {@link Optional#empty()} を返す。
     */
    Optional<F> failedValue();

    /**
     * @return このインスタンスが {@link FailedTry} ならば失敗時の値を {@code function} で変換した値を、
     * そうでなければ {@code defaultValue} を返す。
     */
    default <U> U mapFailValue(U defaultValue, Function<F, U> function) {
        return failedValue().map(function).orElse(defaultValue);
    }

    final class SuccessfulTry<F> implements Try<F> {
        private SuccessfulTry() {}

        @Override
        public Try<F> ifOkThen(F failValue, Supplier<ActionStatus> action) {
            final ActionStatus actionStatus = action.get();

            if (actionStatus == Ok) {
                return new FailedTry<>(failValue);
            }

            return this;
        }

        @Override
        public Optional<F> failedValue() {
            return Optional.empty();
        }
    }

    final class FailedTry<F> implements Try<F> {
        private final F failValue;

        private FailedTry(F failValue) {
            this.failValue = failValue;
        }

        @Override
        public Try<F> ifOkThen(F failValue, Supplier<ActionStatus> action) {
            return this;
        }

        @Override
        public Optional<F> failedValue() {
            return Optional.of(failValue);
        }
    }

    static <F> Try<F> begin(F failValue, Supplier<ActionStatus> action) {
        return new SuccessfulTry<F>().ifOkThen(failValue, action);
    }
}
