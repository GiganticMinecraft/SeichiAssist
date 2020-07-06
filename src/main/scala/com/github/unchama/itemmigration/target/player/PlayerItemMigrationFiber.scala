package com.github.unchama.itemmigration.target.player

import cats.effect.IO
import com.github.unchama.generic.effect.TryableFiber
import org.bukkit.entity.Player

/**
 * プレーヤーのアイテムマイグレーションのプロセスそのものへの参照。
 *
 * このtraitを持つオブジェクトは、往々にして`Player` のインスタンスがあって初めて処理を続行できる。
 * `resumeWith` にて、処理を続行するために必要な `Player` をプロセスに渡すことができる。
 */
trait PlayerItemMigrationFiber {
  /**
   * マイグレーション処理を `player` にて続行する `IO` を返す。
   */
  def resumeWith(player: Player): IO[Unit]

  /**
   * マイグレーション処理への参照
   */
  val fiber: TryableFiber[IO, Unit]
}
