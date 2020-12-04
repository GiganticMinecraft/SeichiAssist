package com.github.unchama.util

object ClassUtils {
  /**
   * 引数で与えられたクラスローダーを現在のスレッドの{@code ContextClassLoader}に指定してから、与えられたタスクを実行する。
   * スレッドのクラスローダーはこの関数を呼び出す前と呼び出す後で同一であることが保証される。
   */
  def withThreadContextClassLoaderAs(classLoader: ClassLoader, task: Runnable): Unit = {
    val initialThreadClassLoader = Thread.currentThread().getContextClassLoader
    Thread.currentThread().setContextClassLoader(classLoader)
    task.run()
    Thread.currentThread().setContextClassLoader(initialThreadClassLoader)
  }
}
