package com.github.unchama.datarepository.template.initialization

import cats.effect.Sync
import scalikejdbc._

object RepositoryInitializationExt {
  implicit class ForSinglePhased[F[_]: Sync, R](self: SinglePhasedRepositoryInitialization[F, R]) {
    def overwriteWithDatabaseValue(key: String, default: => R)
                                  (extractor: WrappedResultSet => R): SinglePhasedRepositoryInitialization[F, R] = {
      // TODO: ゲームサーバが終了したときには一時的に格納された抜ける前のpermit countを全部破棄しても良い
      self.extendPreparation { case (uuid, _ ) =>
        (_: R) => Sync[F].delay {
          DB.readOnly { implicit session =>
            sql"""SELECT * FROM player_rate_limit where uuid = $uuid and rate_limit_name = $key"""
              .map(extractor)
              .single()
              .apply()
          }.getOrElse(default)
        }
      }
    }
  }
}

