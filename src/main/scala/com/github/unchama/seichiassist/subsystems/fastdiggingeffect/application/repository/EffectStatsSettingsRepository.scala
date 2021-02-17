package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.repository

import cats.effect.concurrent.{Deferred, Ref}
import cats.effect.{Concurrent, Effect, Fiber, Sync}
import com.github.unchama.datarepository.template.{RefDictBackedRepositoryInitialization, RepositoryFinalization, SinglePhasedRepositoryInitialization}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.stats.FastDiggingEffectStatsSettings

object EffectStatsSettingsRepository {

  /**
   * [[FastDiggingEffectStatsSettings]] と、それをトピックに60秒に一度通知するプロセスの組
   */
  type RepositoryValue[F[_], G[_]] = (Ref[G, FastDiggingEffectStatsSettings], Deferred[F, Fiber[F, Nothing]])

  def initialization[
    F[_] : Concurrent,
    G[_] : Sync : ContextCoercion[*[_], F]
  ]: SinglePhasedRepositoryInitialization[G, RepositoryValue[F, G]] =
    RefDictBackedRepositoryInitialization.usingUuidRefDict(???)(???)

  def tappingAction[
    F[_] : Effect,
    G[_] : Sync,
    Player
  ]: (Player, RepositoryValue[F, G]) => G[Unit] = ??? // TODO start the process

  def finalization[
    F[_] : Concurrent,
    G[_] : Sync : ContextCoercion[*[_], F],
    Player
  ]: RepositoryFinalization[F, Player, RepositoryValue[F, G]] =
    ???

}
