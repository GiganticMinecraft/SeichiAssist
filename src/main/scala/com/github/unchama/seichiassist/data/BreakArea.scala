package com.github.unchama.seichiassist.data

import cats.effect.IO
import com.github.unchama.seichiassist.ActiveSkill
import com.github.unchama.seichiassist.util.BreakUtil
import org.bukkit.entity.Player

/**
 * 与えられたアクティブスキルの情報に基づいて、
 * プレーヤーがスキルで破壊する範囲を計算するデータオブジェクト
 * @param `type` スキルタイプ番号
 * @param level スキルレベル
 * @param mineflagnum フラグ
 * @param assaultflag アサルトスキルの時true
 */
class BreakArea(val `type`: Int,
                val level: Int,
                val mineflagnum: Int,
                val assaultflag: Boolean) {
  private val skill: ActiveSkill = ActiveSkill.values.apply(`type` - 1)

  //南向きを基準として破壊の範囲座標
  val breakLength: XYZTuple = skill.getBreakLength(level)
  //破壊回数
  val breakNum: Int = skill.getRepeatTimes(level)

  private def breakAreaListFromDirection(dir: String): List[AxisAlignedCuboid] = {
    import BreakArea.CoordinateManipulation._
    import syntax._

    val firstShift: AxisAlignedCuboid => AxisAlignedCuboid =
      if (assaultflag && `type` == 6 && level == 10) {
        //アサルトスキルの時
        areaShift(XYZTuple(0, (breakLength.y - 1) / 2 - 1, 0))
      } else if (dir == "U" || dir == "D" && (assaultflag || level >= 3)) {
        //上向きまたは下向きの時
        areaShift(XYZTuple(0, (breakLength.y - 1) / 2, 0))
      } else {
        //それ以外の範囲
        areaShift(XYZTuple(0, (breakLength.y - 1) / 2 - 1, (breakLength.z - 1) / 2))
      }

    val secondShift: AxisAlignedCuboid => AxisAlignedCuboid =
      if (`type` == ActiveSkill.BREAK.gettypenum && level < 3)
        incrementYOfEnd
      else
        identity

    val thirdShift: AxisAlignedCuboid => AxisAlignedCuboid =
      if (`type` == ActiveSkill.BREAK.gettypenum && level < 3 && mineflagnum == 1)
        areaShift(XYZTuple(0, 1, 0))
      else
        identity

    val directionalShift: AxisAlignedCuboid => AxisAlignedCuboid =
      dir match {
        case "N" | "E" | "S" | "W" =>
          areaShift(XYZTuple(0, 0, breakLength.z))
        case "U" | "D" if assaultflag || level >= 3 =>
          areaShift(XYZTuple(0, breakLength.y, 0))
        case _ => identity
      }

    val rotation: AxisAlignedCuboid => AxisAlignedCuboid =
      dir match {
        case "N" => rotateXZ(180)
        case "E" => rotateXZ(270)
        case "W" => rotateXZ(90)
        case "D" if !assaultflag => invertY
        // 横向きのスキル発動の場合Sが基準となり、
        // 縦向きの場合Uが基準となっているため回転しないで良い
        case "S" | "U" | _ => identity
      }

    val firstArea = {
      // 中心が(0,0,0)である領域(start = -end)を変形していく。
      val end = (breakLength - XYZTuple(1, 1, 1)) / 2.0
      val start = end.negative

      import scala.util.chaining._

      AxisAlignedCuboid(end, start)
        .pipe(firstShift)
        .pipe(secondShift)
        .pipe(thirdShift)
    }

    LazyList
      .iterate(firstArea)(directionalShift)
      .map(rotation)
      .take(breakNum)
      .toList
  }

  def makeBreakArea(player: Player): IO[List[AxisAlignedCuboid]] =
    BreakArea.getCardinalDirection(player).map(breakAreaListFromDirection)
}

object BreakArea {
  private val getCardinalDirection: Player => IO[String] = { player => IO { BreakUtil.getCardinalDirection(player) } }

  object CoordinateManipulation {
    import syntax._

    val incrementYOfEnd: AxisAlignedCuboid => AxisAlignedCuboid = {
      case area@AxisAlignedCuboid(_, end@XYZTuple(_, y, _)) =>
        area.copy(end = end.copy(y = y + 1))
    }

    val invertY: AxisAlignedCuboid => AxisAlignedCuboid = { case AxisAlignedCuboid(begin, end) =>
      def invertYOfVector(vector: XYZTuple): XYZTuple = XYZTuple(vector.x, -vector.y, vector.z)

      AxisAlignedCuboid(invertYOfVector(begin), invertYOfVector(end))
    }

    def areaShift(vector: XYZTuple): AxisAlignedCuboid => AxisAlignedCuboid = {
      case AxisAlignedCuboid(begin, end) =>
        AxisAlignedCuboid(begin + vector, end + vector)
    }

    def rotateXZ(d: Int): AxisAlignedCuboid => AxisAlignedCuboid = { case AxisAlignedCuboid(begin, end) =>
      d match {
        case 90 =>
          AxisAlignedCuboid(XYZTuple(-end.z, begin.y, begin.x), XYZTuple(-begin.z, end.y, end.x))
        case 180 =>
          AxisAlignedCuboid(XYZTuple(begin.x, begin.y, -end.z), XYZTuple(end.x, end.y, -begin.z))
        case 270 =>
          AxisAlignedCuboid(XYZTuple(begin.z, begin.y, begin.x), XYZTuple(end.z, end.y, end.x))
        case 360 =>
          AxisAlignedCuboid(begin, end)
      }
    }
  }
}
