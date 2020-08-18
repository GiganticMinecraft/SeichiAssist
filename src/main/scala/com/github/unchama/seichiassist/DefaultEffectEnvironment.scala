package com.github.unchama.seichiassist

import cats.effect.{Effect, IO}
import com.github.unchama.generic.effect.unsafe.EffectEnvironment

// the error log should report that the error comes from SeichiAssist. To achieve this,
// TODO prepare alternative environment that uses dedicated Logger for effect execution
object DefaultEffectEnvironment extends EffectEnvironment {

  override def runEffectAsync[U, F[_] : Effect](context: String, program: F[U]): Unit = {
    import cats.effect.implicits._

    program
      .runAsync {
        case Left(error) => IO {
          println(s"${context}最中にエラーが発生しました。")
          error.printStackTrace()
        }
        case Right(_) => IO.unit
      }
      .unsafeRunSync()
  }

}
