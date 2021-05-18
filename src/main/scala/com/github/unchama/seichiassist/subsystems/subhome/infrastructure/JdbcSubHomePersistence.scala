package com.github.unchama.seichiassist.subsystems.subhome.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.subhome.domain.{SubHome, SubHomeId, SubHomeLocation, SubHomePersistence, SubHomeV2}
import com.github.unchama.seichiassist.subsystems.subhome.{SubHomeReadAPI, SubHomeWriteAPI}
import com.github.unchama.targetedeffect.player
import org.bukkit.{Bukkit, Location}
import scalikejdbc._

import java.util.UUID

class JdbcSubHomePersistence[F[_]: Sync] extends SubHomePersistence[F] {
  private val serverId = SeichiAssist.seichiAssistConfig.getServerNum

  override def upsert(ownerUuid: UUID, id: SubHomeId)(subHome: SubHomeV2): F[Unit] = {
    Sync[F].delay {
      DB.readOnly { implicit session =>
        val SubHomeLocation(worldName, x, y, z) = subHome.location

        sql"""insert into seichiassist.sub_home
             |(player_uuid, server_id, id, name, location_x, location_y, location_z, world_name) values
             |  (${ownerUuid.toString}, $serverId, $id, ${subHome.name}, $x, $y, $z, $worldName)
             |    on duplicate key update
             |      name = values(name),
             |      location_x = values(location_x),
             |      location_y = values(location_y),
             |      location_z = values(location_z),
             |      world_name = values(world_name)"""
          .update()
          .apply()
      }
    }
  }

  override def list(ownerUuid: UUID): F[Map[SubHomeId, SubHomeV2]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"""SELECT id, name, location_x, location_y, location_z, world_name FROM seichiassist.sub_home"""
        .map(rs => (SubHomeId(rs.int("id")), extractSubHome(rs)))
        .list().apply()
    }.toMap
  }

  private def extractSubHome(rs: WrappedResultSet): SubHomeV2 =
    SubHomeV2(
      rs.string("name"),
      SubHomeLocation(
        rs.string("world_name"),
        rs.int("location_x"),
        rs.int("location_y"),
        rs.int("location_z")
      )
    )
}
