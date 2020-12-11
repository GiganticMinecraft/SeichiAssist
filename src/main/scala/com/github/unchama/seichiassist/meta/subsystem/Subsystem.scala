package com.github.unchama.seichiassist.meta.subsystem

import cats.~>
import com.github.unchama.bungeesemaphoreresponder.domain.PlayerDataFinalizer
import com.github.unchama.generic.ContextCoercion
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener

/**
 * サブシステムのtrait。
 *
 * 幾つかのプラグイン機能は他の概念と関連性が薄く、独立した一つのシステムとして機能することがある。
 * このtraitは、それらのシステムが親Bukkitプラグインに露出すべき情報をまとめている。
 *
 * @tparam F ファイナライザの作用のコンテキスト
 */
trait Subsystem[F[_]] {

  /**
   * サブシステムが持つリスナ
   */
  val listeners: Seq[Listener]

  /**
   * サブシステムが管理するデータのファイナライザ。
   * プレーヤーが退出する時、これらのファイナライザが `F` の文脈で実行されることを想定している。
   */
  val managedFinalizers: Seq[PlayerDataFinalizer[F, Player]]

  /**
   * サブシステムが管理するコマンド
   */
  val commands: Map[String, TabExecutor]

  def transformFinalizationContext[G[_]](trans: F ~> G): Subsystem[G] = new Subsystem[G] {
    override val listeners: Seq[Listener] = Subsystem.this.listeners
    override val managedFinalizers: Seq[PlayerDataFinalizer[G, Player]] =
      Subsystem.this.managedFinalizers.map(_.transformContext(trans))
    override val commands: Map[String, TabExecutor] = Subsystem.this.commands
  }

  def coerceFinalizationContextTo[G[_] : ContextCoercion[F, *[_]]]: Subsystem[G] = transformFinalizationContext(implicitly)

}

object Subsystem {

  def apply[F[_]](listenersToBeRegistered: Seq[Listener],
                  finalizersToBeManaged: Seq[PlayerDataFinalizer[F, Player]],
                  commandsToBeRegistered: Map[String, TabExecutor]): Subsystem[F] = new Subsystem[F] {
    override val listeners: Seq[Listener] = listenersToBeRegistered
    override val commands: Map[String, TabExecutor] = commandsToBeRegistered
    override val managedFinalizers: Seq[PlayerDataFinalizer[F, Player]] = finalizersToBeManaged
  }

}
