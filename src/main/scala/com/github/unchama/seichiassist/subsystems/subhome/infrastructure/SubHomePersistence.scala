package com.github.unchama.seichiassist.subsystems.subhome.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.subhome.domain.SubHome
import org.bukkit.{Bukkit, Location}
import scalikejdbc._

import java.util.UUID

class SubHomePersistence[F[_]: Sync] extends SubHomeReadAPI[F] with SubHomeWriteAPI[F] {
  final val table = "seichiassist.sub_home"
  private val serverId = SeichiAssist.seichiAssistConfig.getServerNum
  override def get(player: UUID, id: SubHome.ID): F[Option[SubHome]] = {
    Sync[F].delay {
      DB.readOnly { implicit session =>
        sql"""SELECT id, name, location_x, location_y, location_z, world_name FROM $table
             WHERE player = ${player.toString} AND server_id = $serverId AND id = $id"""
          .map(extractSubHome)
          // もしかすると見つからないかもしれない
          .first()
          .apply()
      }
    }
  }

  override def list(player: UUID): F[Map[SubHome.ID, SubHome]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"""SELECT id, name, location_x, location_y, location_z, world_name FROM $table"""
        .map(rs => {
          (
            rs.int("id"),
            extractSubHome(rs)
          )
        })
        .list()
        .apply()
    }.toMap
  }

  override def updateLocation(player: UUID, id: SubHome.ID, location: Location): F[Unit] =
    Sync[F].delay {
      val x = location.getX.toInt
      val y = location.getY.toInt
      val z = location.getZ.toInt
      val worldName = location.getWorld.getName
      DB.localTx { implicit session =>
        // 重複したとき、もとのエントリを残す必要はないので黙って上書きする
        sql"""insert into $table
             |(player_uuid,server_id,id,location_x,location_y,location_z,world_name) values
             |(${player.toString},$serverId,$id,$x,$y,$z,$worldName)
             |on duplicate key update
             |location_x = values(location_x),
             |location_y = values(location_y),
             |location_z = values(location_z),
             |world_name = values(world_name)"""
          .stripMargin('|')
          .execute()
          .apply()
      }
    }

  override def updateName(player: UUID, number: SubHome.ID, name: String): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"""insert into $table
             |(player_uuid,server_id,id,name) values
             |(${player.toString},$serverId,$number,$name)
             |on duplicate key update
             |name = values(name)"""
          .stripMargin('|')
          .execute()
          .apply()
      }
    }

  private def extractSubHome(rs: WrappedResultSet): SubHome = {
    val x = rs.int("location_x")
    val y = rs.int("location_y")
    val z = rs.int("location_z")
    val world = Bukkit.getWorld(rs.string("world_name"))
    val homeName = rs.string("name")
    new SubHome(new Location(world, x, y, z), homeName)
  }
}
