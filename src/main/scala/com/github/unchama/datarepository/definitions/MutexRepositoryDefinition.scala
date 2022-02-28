package com.github.unchama.datarepository.definitions

import cats.effect.{Concurrent, Sync}
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.concurrent.Mutex

object MutexRepositoryDefinition {

  def over[F[_]: Concurrent, G[_]: Sync: ContextCoercion[*[_], F], Player, R](
    underlying: RepositoryDefinition.Phased[G, Player, R]
  ): underlying.Self[Mutex[F, G, R]] =
    underlying.flatXmap(r => Mutex.of[F, G, R](r))(_.readLatest)

}
