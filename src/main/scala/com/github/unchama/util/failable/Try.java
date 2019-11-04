package com.github.unchama.util.failable;

import com.github.unchama.util.ActionStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.unchama.util.ActionStatus.Fail;
import static com.github.unchama.util.ActionStatus.Ok;

/**
 * @param <F> 失敗時に記録される値の型
 * @author kory
 * <p>
 * 失敗する可能性のある処理を逐次実行するための構造を備えたオブジェクトのクラス
 */
public abstract class Try<F> {
    private static <F> Try<F> succeed() {
        return new SuccessfulTry<>();
    }

    public static <F> Try<F> sequence(List<FailableAction<F>> actions) {
        Try<F> currentTry = Try.succeed();

        for (final FailableAction<F> action : actions) {
            currentTry = currentTry.ifOkThen(action.failValue, action.action);
        }

        return currentTry;
    }

    @SafeVarargs
    public static <F> Try<F> sequence(FailableAction<F>... actions) {
        return sequence(Arrays.asList(actions));
    }

    /**
     * このインスタンスが成功状態ならば、{@code action}を実行し、
     * 失敗状態ならば{@code action}は実行せず内部状態を保つ。
     *
     * @param action    実行する処理
     * @param failValue 失敗したときに{@link FailedTry}の構築に使用される値
     * @return {@code action}が実行された場合、結果が{@link ActionStatus#Ok}ならば
     * {@link SuccessfulTry}を返し、そうでなければ{@code failValue}が入った
     * {@link FailedTry}を返す。
     */
    protected abstract Try<F> ifOkThen(F failValue, Supplier<ActionStatus> action);

    /**
     * @return このインスタンスが {@link FailedTry} ならば失敗時の値を含んだ {@link Optional} を、
     * そうでなければ {@link Optional#empty()} を返す。
     */
    public abstract Optional<F> failedValue();

    /**
     * @return このインスタンスが失敗を表現するならば、失敗時の値を {@code function} で変換した失敗したTryを、
     * そうでなければ成功しているTryを返す。
     */
    public <U> Try<U> mapFailed(Function<? super F, U> function) {
        return failedValue()
                .map((value) -> (Try<U>) new FailedTry<>(function.apply(value)))
                .orElse(new SuccessfulTry<>());
    }

    public ActionStatus overallStatus() {
        return failedValue().map((_value) -> Fail).orElse(Ok);
    }

    static final class SuccessfulTry<F> extends Try<F> {
        private SuccessfulTry() {
        }

        @Override
        public Try<F> ifOkThen(F failValue, Supplier<ActionStatus> action) {
            return action.get() == Ok ? this : new FailedTry<>(failValue);
        }

        @Override
        public Optional<F> failedValue() {
            return Optional.empty();
        }
    }

    static final class FailedTry<F> extends Try<F> {
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
}
