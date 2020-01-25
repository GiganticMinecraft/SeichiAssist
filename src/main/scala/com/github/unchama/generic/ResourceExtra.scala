package com.github.unchama.generic

import cats.data.OptionT
import cats.effect.{ExitCase, Resource}
import cats.{Applicative, Defer, Monad}

object ResourceExtra {
  import cats.implicits._

  def unwrapOptionTResource[F[_]: Defer: Monad, R](resource: Resource[OptionT[F, *], R]): Resource[F, Option[R]] = {
    import Resource.{Allocate, Bind, Suspend}

    val unit = Applicative[F].unit
    resource match {
      case Allocate(resource) =>
        Allocate(
          resource.value.map {
            case Some((r, finalizer)) => (Some(r), finalizer.andThen(_.value *> unit))
            case None => (None, (_: ExitCase[Throwable]) => unit)
          }
        )
      case b: Bind[OptionT[F, *], s, R] => b match {
        case Bind(source: Resource[OptionT[F, *], s], fs: (s => Resource[OptionT[F, *], R])) =>
          Bind(
            Suspend(Defer[F].defer(Monad[F].pure(unwrapOptionTResource(source)))),
            (o: Option[s]) => o match {
              case Some(v) => unwrapOptionTResource(fs(v))
              case None => Resource.pure(None)
            }
          )
      }
      case Suspend(resource) =>
        Suspend(resource.value.map {
          case Some(resource) => unwrapOptionTResource(resource)
          case None => Resource.pure(None)
        })
    }
  }
}
