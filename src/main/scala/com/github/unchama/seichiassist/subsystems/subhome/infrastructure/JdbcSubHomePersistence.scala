package com.github.unchama.seichiassist.subsystems.subhome.infrastructure

import cats.effect.Sync
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.subhome.domain.{
  SubHome,
  SubHomeId,
  SubHomeLocation,
  SubHomePersistence
}
import scalikejdbc._

import java.util.UUID

class JdbcSubHomePersistence[F[_]: Sync: NonServerThreadContextShift]
    extends SubHomePersistence[F] {
  private val serverId = SeichiAssist.seichiAssistConfig.getServerNum

  import cats.implicits._

  override def upsert(ownerUuid: UUID, id: SubHomeId)(subHome: SubHome): F[Unit] =
    NonServerThreadContextShift[F].shift >> Sync[F].delay[Unit] {
      DB.localTx { implicit session =>
        val SubHomeLocation(worldName, x, y, z, pitch, yaw) = subHome.location

        // NOTE 2021/05/19: 何故かDB上のIDは1少ない。つまり、ID 1のサブホームはDB上ではid=0である。
        sql"""insert into seichiassist.sub_home
             |(player_uuid, server_id, id, name, location_x, location_y, location_z, world_name, pitch, yaw) values
             |  (${ownerUuid.toString}, $serverId, ${id.value - 1}, ${subHome
              .name
              .orNull}, $x, $y, $z, $worldName)
             |    on duplicate key update
             |      name = ${subHome.name.orNull},
             |      location_x = $x,
             |      location_y = $y,
             |      location_z = $z,
             |      pitch = $pitch,
             |      yaw = $yaw,
             |      world_name = $worldName""".stripMargin.update().apply()
      }
    }

  override def list(ownerUuid: UUID): F[Map[SubHomeId, SubHome]] =
    NonServerThreadContextShift[F].shift >> Sync[F].delay {
      DB.readOnly { implicit session =>
        // NOTE 2021/05/19: 何故かDB上のIDは1少ない。つまり、ID 1のサブホームはDB上ではid=0である。
        sql"""SELECT id, name, location_x, location_y, location_z, world_name, pitch, yaw
             |  FROM seichiassist.sub_home
             |  where server_id = $serverId
             |  and player_uuid = ${ownerUuid.toString}"""
          .stripMargin
          .map(rs =>
            (
              SubHomeId(rs.int("id") + 1),
              SubHome(
                rs.stringOpt("name"),
                SubHomeLocation(
                  rs.string("world_name"),
                  rs.double("location_x"),
                  rs.double("location_y"),
                  rs.double("location_z"),
                  rs.float("pitch"),
                  rs.float("yaw")
                )
              )
            )
          )
          .stripMargin
          .list()
          .apply()
      }.toMap
    }

  override def remove(ownerUuid: UUID, id: SubHomeId): F[Boolean] = {
    NonServerThreadContextShift[F].shift >> Sync[F].delay {
      DB.localTx { implicit session =>
        // NOTE 2022/04/16: 何故かDB上のIDは1少ない。つまり、ID 1のサブホームはDB上ではid=0である。
        sql"""delete from seichiassist.sub_home 
             |  where server_id = $serverId 
             |  and player_uuid = ${ownerUuid.toString} 
             |  and id = ${id.value - 1}""".stripMargin.execute().apply()
      }
    }
  }
}
