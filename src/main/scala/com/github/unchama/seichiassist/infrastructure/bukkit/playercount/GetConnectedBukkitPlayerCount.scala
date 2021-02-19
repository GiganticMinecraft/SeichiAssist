package com.github.unchama.seichiassist.infrastructure.bukkit.playercount

import cats.effect.Sync
import com.github.unchama.seichiassist.domain.playercount.GetConnectedPlayerCount
import org.bukkit.Bukkit

class GetConnectedBukkitPlayerCount[F[_] : Sync] extends GetConnectedPlayerCount[F] {

  /**
   * 現在サーバーに接続しているプレーヤー数を取得する。
   */
  override def now: F[Int] = Sync[F].delay {
    Bukkit.getOnlinePlayers.size()
  }

}
