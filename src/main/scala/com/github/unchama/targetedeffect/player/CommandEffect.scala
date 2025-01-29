package com.github.unchama.targetedeffect.player

import cats.data.Kleisli
import cats.effect.{IO, SyncIO}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player

object CommandEffect {
  def apply(
    string: String
  )(implicit ioOnMainThread: OnMinecraftServerThread[IO]): TargetedEffect[Player] =
    Kleisli { p =>
      // 非同期スレッドからchatを呼ぶとコマンドがそのスレッドで実行される(Spigot 1.12.2)。
      // コマンドの実行は基本的に同期スレッドで行ってほしいのでメインスレッドまで処理を飛ばす。
      ioOnMainThread.runAction(SyncIO {
        p.chat(s"/$string")
      })
    }
}

object CommandEffectF {
  def apply[F[_]: OnMinecraftServerThread](string: String): Kleisli[F, Player, Unit] =
    Kleisli { player =>
      OnMinecraftServerThread[F].runAction(SyncIO {
        player.chat(s"/$string")
      })
    }
}
