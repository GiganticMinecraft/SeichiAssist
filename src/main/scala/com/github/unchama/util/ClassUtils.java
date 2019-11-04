package com.github.unchama.util;

public class ClassUtils {
    private ClassUtils() {
    }

    /**
     * 引数で与えられたクラスローダーを現在のスレッドの{@code ContextClassLoader}に指定してから、与えられたタスクを実行する。
     * スレッドのクラスローダーはこの関数を呼び出す前と呼び出す後で同一であることが保証される。
     */
    public static void withThreadContextClassLoaderAs(final ClassLoader classLoader, final Runnable task) {
        final ClassLoader initialThreadClassLoader = Thread.currentThread().getContextClassLoader();

        Thread.currentThread().setContextClassLoader(classLoader);

        task.run();

        Thread.currentThread().setContextClassLoader(initialThreadClassLoader);
    }
}
