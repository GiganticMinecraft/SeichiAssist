package com.github.unchama.util.failable;

import com.github.unchama.util.ActionStatus;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.unchama.util.ActionStatus.Fail;
import static com.github.unchama.util.ActionStatus.Ok;

/**
 * @author kory
 *
 * 失敗する可能性のある処理を逐次実行するための構造を備えたオブジェクトのクラス
 * @param <F> 失敗時に記録される値の型
 */
public abstract class Try<F> {
    /**
     * このインスタンスが成功状態ならば、{@code action}を実行し、
     * 失敗状態ならば{@code action}は実行せず内部状態を保つ。
     *
     * @param action 実行する処理
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

    private static <F> Try<F> succeed() {
        return new SuccessfulTry<>();
    }

    public static <F> Try<F> sequence(Collection<Pair<F, Supplier<ActionStatus>>> actions) {
        Try<F> currentTry = Try.succeed();

        for (final Pair<F, Supplier<ActionStatus>> action: actions) {
            currentTry = currentTry.ifOkThen(action.getLeft(), action.getRight());
        }

        return currentTry;
    }

    @SafeVarargs
    public static <F> Try<F> sequence(Pair<F, Supplier<ActionStatus>>... actions) {
        return sequence(Arrays.asList(actions));
    }
}
