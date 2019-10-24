package com.github.unchama.seichiassist.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * 参照を直接渡して破壊的変更が行われないようにするためのクラス。
 * このクラスは、コピー・ファクトリで安全にもともとの参照と異なる参照を新しく生成しなければならない。
 * また、呼び出し側は、もともとの参照と異なる参照が確実に作られることが保証されるコピー・ファクトリを実引数として与えなければならない。
 * このクラスは、ボクシングされるプリミティブの値を保持するのには無意味である。
 *
 * @param <V> 参照の型
 */
public class ValueHolder<V> {
    private final V value;
    private final UnaryOperator<V> copyFactory;

    /**
     * 新しいインスタンスを生成する。コピー・ファクトリは、{@code value}に影響を与えないファクトリでなければならない。したがって、
     * <pre>
     *     new ValueHolder&lt;&gt;(o, t -> t)
     * </pre>
     * などといったもともとの参照に影響を与えることができるような参照を作成するコピー・ファクトリを与えるのは安全ではない。
     *
     * @param value       参照の型
     * @param copyFactory 新しい参照を作成するコピー・ファクトリ
     */
    public ValueHolder(V value, @NotNull UnaryOperator<V> copyFactory) {
        this.value = value;
        this.copyFactory = copyFactory;
    }

    public V getValue() {
        return copyFactory.apply(value);
    }

    public UnaryOperator<V> getCopyFactory() {
        return copyFactory;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof ValueHolder) {
            final ValueHolder<?> c = (ValueHolder<?>) obj;
            return Objects.equals(c.value, this.value) && Objects.equals(c.getCopyFactory(), this.getCopyFactory());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, copyFactory);
    }
}
