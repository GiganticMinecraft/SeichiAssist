package com.github.unchama.menuinventory

import cats.data
import cats.effect.IO
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player

/**
 * 「メニュー」のtrait.
 *
 * このtraitを実装するオブジェクトは, インベントリ上で展開される意味づけされたUIの情報を持っている. これらのUIをメニューインベントリ, または単にメニューと呼ぶこととする.
 */
trait Menu {

  /**
   * メニューを開く操作に必要な環境情報の型。 例えば、メニューが利用するAPIなどをここを通して渡すことができる。
   */
  type Environment

  /**
   * メニューのサイズとタイトルに関する情報
   */
  val frame: MenuFrame

  /**
   * @return
   * `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  def computeMenuLayout(player: Player)(implicit environment: Environment): IO[MenuSlotLayout]

  /**
   * メニューを[Player]に開かせる[TargetedEffect].
   */
  def open(
    implicit environment: Environment,
    ctx: LayoutPreparationContext,
    onMainThread: OnMinecraftServerThread[IO]
  ): TargetedEffect[Player] = data.Kleisli { player =>
    for {
      session <- MenuSession.createNewSessionWith[IO](frame)
      _ <- session.openInventory.run(player)
      _ <- IO.shift(ctx)
      layout <- computeMenuLayout(player)
      _ <- session.overwriteViewWith(layout)
    } yield ()
  }

}
