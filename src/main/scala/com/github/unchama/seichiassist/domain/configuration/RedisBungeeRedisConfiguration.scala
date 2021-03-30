package com.github.unchama.seichiassist.domain.configuration

/**
 * RedisBungeeが利用しているRedisインスタンスの接続情報
 */
trait RedisBungeeRedisConfiguration {

  val redisHost: String

  val redisPort: Int

  val redisPassword: Option[String]

}
