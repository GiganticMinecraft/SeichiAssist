package com.github.unchama.seichiassist.meta.subsystem

import com.github.unchama.bungeesemaphoreresponder.domain.PlayerDataFinalizer
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener

/**
 * サブシステムのtrait。
 *
 * 幾つかのプラグイン機能は他の概念と関連性が薄く、独立した一つのシステムとして機能することがある。
 * このtraitは、それらのシステムが親Bukkitプラグインに露出すべき情報をまとめている。
 *
 * @tparam F
 *   ファイナライザの作用のコンテキスト
 */
trait Subsystem[F[_]] {

  /**
   * サブシステムが持つリスナ
   */
  val listeners: Seq[Listener] = Nil

  /**
   * サブシステムが管理するデータリポジトリ群。
   */
  val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] = Nil

  /**
   * データリポジトリ以外のプレーヤーデータの終了処理。
   */
  val managedFinalizers: Seq[PlayerDataFinalizer[F, Player]] = Nil

  /**
   * サブシステムが管理するコマンド
   */
  val commands: Map[String, TabExecutor] = Map.empty
}
