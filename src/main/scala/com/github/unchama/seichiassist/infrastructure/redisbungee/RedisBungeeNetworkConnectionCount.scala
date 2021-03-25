package com.github.unchama.seichiassist.infrastructure.redisbungee

import cats.effect.Sync
import com.github.unchama.minecraft.actions.MinecraftServerThreadShift
import com.github.unchama.seichiassist.domain.actions.GetNetworkConnectionCount

class RedisBungeeNetworkConnectionCount[F[_] : Sync : MinecraftServerThreadShift] extends GetNetworkConnectionCount[F] {

  import cats.implicits._

  override val now: F[Int] = MinecraftServerThreadShift[F].shift >> Sync[F].delay {
    // TODO RedisBungeeからデータを掠め取るような実装に変更する
    1
  }
}
