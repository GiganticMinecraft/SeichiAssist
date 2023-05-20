package com.github.unchama.seichiassist.infrastructure.redisbungee

import akka.actor.ActorSystem
import cats.effect.{ContextShift, Effect, IO}
import com.github.unchama.seichiassist.domain.actions.GetNetworkConnectionCount
import com.github.unchama.seichiassist.domain.configuration.RedisBungeeRedisConfiguration
import io.chrisdavenport.log4cats.ErrorLogger
import redis.RedisClient
import redis.api.scripting.RedisScript
import redis.protocol.{Bulk, MultiBulk}

class RedisBungeeNetworkConnectionCount[F[_]: Effect: ErrorLogger](
  connectionContextShift: ContextShift[IO]
)(implicit configuration: RedisBungeeRedisConfiguration, actorSystem: ActorSystem)
    extends GetNetworkConnectionCount[F] {

  import cats.implicits._

  implicit private val _cs: ContextShift[IO] = connectionContextShift

  private val redisClient = RedisClient(
    host = configuration.redisHost,
    port = configuration.redisPort,
    password = configuration.redisPassword
  )

  /**
   * RedisBungee 0.5.1の仕様として、
   *   - proxy:{serverName}:usersOnline に serverName でオンラインであるプレーヤーのUUIDがsetで置かれており
   *   - player:{uuid} の server フィールドに uuid を持つプレーヤーが参加しているサーバーの名前が置かれている
   *     ので、プレーヤーが参加しているサーバー名の重複ありのリストを返すluaスクリプトを投げればよい
   */
  private val script = RedisScript("""
local proxy_keys = redis.call('keys', 'proxy:*:usersOnline')
local player_keys = {}

for i, proxy_key in ipairs(proxy_keys) do
  local proxy_player_keys = redis.call('smembers', proxy_key)
  for j, proxy_player_key in ipairs(proxy_player_keys) do
    player_keys[#player_keys + 1] = proxy_player_key
  end
end

local result = {}
for i, player_key in ipairs(player_keys) do
  local server = redis.call('hget', 'player:' .. player_key, 'server')
  result[#result + 1] = server
end
return result""")

  private val seichiServers = Set("s1", "s2", "s3", "s5", "s7")

  override val now: F[Int] =
    Effect[F]
      .liftIO {
        IO.fromFuture(IO(redisClient.evalshaOrEval(script))).flatMap {
          case MultiBulk(Some(vector)) =>
            IO.pure {
              vector.count {
                case Bulk(Some(byteString)) => seichiServers.contains(byteString.utf8String)
                case _                      => false
              }
            }
          case _ => IO.raiseError(new RuntimeException("Expected MultiBulk response"))
        }
      }
      .handleErrorWith { error =>
        ErrorLogger[F].error(error)("RedisBungee関連のデータを取得するのに失敗しました").as(0)
      }
}
