package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.process

import cats.MonadError
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.actions.GrantFastDiggingEffect
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.FastDiggingEffectList
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings.FastDiggingEffectSuppressionState
import io.chrisdavenport.cats.effect.time.JavaTime

object SynchronizationProcess {

  import cats.implicits._

  def using[
    F[_] : GrantFastDiggingEffect[*[_], Player] : MonadError[*[_], Throwable] : JavaTime,
    Player
  ](suppressionState: KeyedDataRepository[Player, ReadOnlyRef[F, FastDiggingEffectSuppressionState]],
    effectClock: fs2.Stream[F, (Player, FastDiggingEffectList)]): fs2.Stream[F, Unit] =
    effectClock
      .evalTap { case (player, list) =>
        // TODO ここでのレポジトリアクセスはプレーヤーが退出した後のストリーム処理により例外を吐く可能性がある。
        //      attemptで潰せるが、そうならないように設計できないか？
        val program = for {
          state <- suppressionState(player).read
          totalAmplifier <- list.totalEffectAmplifier[F](state)

          _ <- GrantFastDiggingEffect[F, Player].forASecond(player)(totalAmplifier)
        } yield ()

        program.attempt
      }
      .as(())

}
