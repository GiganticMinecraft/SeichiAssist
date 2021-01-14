package com.github.unchama.seichiassist.subsystems.breakcount

import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import org.bukkit.entity.Player

/**
 * 整地量データを管理するシステム。
 * このシステムは次の責務を持つ。
 *
 *  - 整地量データを永続化する
 *  - 整地量データの読み取りとインクリメント操作を他システムへ露出する
 *  - 整地量データの変更を他システムやプレーヤーへ通知する
 */
trait System[F[_], G[_]] extends Subsystem[F] {

  val api: BreakCountAPI[F, G, Player]

}

object System {


}
