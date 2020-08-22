package com.github.unchama.concurrent.bukkit

import cats.effect.IO
import com.github.unchama.concurrent.MinecraftServerThreadIOShift
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

import scala.concurrent.ExecutionContext

/**
 * Bukkitのメインスレッドへ実行をシフトするような `MinecraftMainServerThreadShift` の実装。
 *
 * Bukkitのスケジューラにタスクを投げるような `ExecutionContext` から
 * `ContextShift[IO]` を作ることはできるが、この実装だと一つ問題がある。
 * 具体的には、`IO` のキャンセルがタスクの実行がキューされるまでしか可能でなく、
 * 一度キューされたら実行が始まるのを待ち続けないといけない点にある。
 * cats-effectの言葉で言えば、キャンセルが可能な非同期境界がキュー以前までしか無い。
 *
 * この問題点はBukkitと組み合わせると次の不都合がある。
 * 例えば、Bukkitサーバーが停まるまでに必ず終了（正常終了かキャンセル）
 * されなければならないプログラムがあったとする。
 * また、このプログラムは途中で同期スレッドに実行をシフトするロジックを含むとする。
 *
 * サーバーの停止時、 `onDisable` にてこのプログラムの `Fiber` をcancelする必要がある。
 * しかし、 `onDisable` が呼ばれた後はサーバーティックはもう来ない。
 * よって、もし `onDisable` が同期スレッドへのシフトがキューされた後、
 * かつ同期スレッドでのシフト後の処理が走り出す前に呼び出されてしまった場合、
 * プログラムをキャンセルする手立てがない。デッドロックのような状況が発生し、
 * リソースの安全な解放ができない可能性がある。
 *
 * `BukkitSyncIOShift` はこの問題を解決するため、
 * shiftの実行時点でキャンセルが入ったときはBukkitのスケジューラ上で
 * キューしたタスクもろともキャンセルするような実装になっている。
 */
class BukkitServerThreadIOShift(implicit hostPlugin: JavaPlugin) extends MinecraftServerThreadIOShift {
  override def shift: IO[Unit] =
    IO.cancelable { cb =>
      val run = new Runnable {
        def run(): Unit = cb(Right(()))
      }
      val scheduledTask = Bukkit.getScheduler.runTask(hostPlugin, run)

      IO(scheduledTask.cancel())
    }

  override def evalOn[A](ec: ExecutionContext)(fa: IO[A]): IO[A] =
    IO.shift(ec).bracket(_ => fa)(_ => shift)
}
