package com.github.unchama.seichiassist

import cats.effect.Async
import cats.effect.std.Dispatcher
import com.github.unchama.generic.effect.unsafe.EffectEnvironment

// the error log should report that the error comes from SeichiAssist. To achieve this,
// TODO prepare alternative environment that uses dedicated Logger for effect execution
final class DefaultEffectEnvironment[F[_]](dispatcher: Dispatcher[F])(implicit F: Async[F])
    extends EffectEnvironment[F] {

  override def unsafeRunEffectAsync[U](context: String, program: F[U]): Unit = {
    import cats.syntax.all._

    dispatcher.unsafeRunAndForget {
      program.void.handleErrorWith { error =>
        F.delay {
          println(s"${context}最中にエラーが発生しました。")
          error.printStackTrace()
        }
      }
    }
  }

}

object DefaultEffectEnvironment {

  def apply[F[_]: Async](dispatcher: Dispatcher[F]): EffectEnvironment[F] =
    new DefaultEffectEnvironment[F](dispatcher)

}
