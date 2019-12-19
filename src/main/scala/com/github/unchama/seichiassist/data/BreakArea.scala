package com.github.unchama.seichiassist.data

import java.util

import com.github.unchama.seichiassist.ActiveSkill
import com.github.unchama.seichiassist.util.BreakUtil
import org.bukkit.entity.Player

class BreakArea(val player: Player,
                //スキルタイプ番号
                var `type`: Int,
                //スキルレベル
                var level: Int,
                //フラグ
                var mineflagnum: Int,
                //アサルトスキルの時true
                var assaultflag: Boolean) {
  private val skill: ActiveSkill = ActiveSkill.values.apply(`type` - 1)

  //初期範囲設定
  makeArea()

  //南向きを基準として破壊の範囲座標
  private val breakLength = skill.getBreakLength(level)
  //破壊回数
  private val breakNum = skill.getRepeatTimes(level)

  //向いている方角
  private var dir = BreakUtil.getCardinalDirection(player)

  //破壊範囲を示す相対座標リスト
  private val startList = new util.ArrayList[XYZTuple]
  private val endList = new util.ArrayList[XYZTuple]

  //変数として利用する相対座標
  private var start: XYZTuple = null
  private var end: XYZTuple = null

  def getStartList: util.ArrayList[XYZTuple] = startList

  def getEndList: util.ArrayList[XYZTuple] = endList

  def getDir: String = dir

  def setDir(dir: String): Unit = this.dir = dir

  //破壊範囲の設定
  def makeArea(): Unit = {
    import syntax._

    startList.clear()
    endList.clear()

    //中心座標(0,0,0)のスタートとエンドを仮取得
    end = (breakLength - XYZTuple(1, 1, 1)) / 2.0
    start = end.negative

    if (assaultflag && `type` == 6 && level == 10) {
      //アサルトスキルの時
      shift(0, (breakLength.y - 1) / 2 - 1, 0)
    } else if (dir == "U" || dir == "D" && (assaultflag || level >= 3)) {
      //上向きまたは下向きの時
      shift(0, (breakLength.y - 1) / 2, 0)
    } else {
      //それ以外の範囲
      shift(0, (breakLength.y - 1) / 2 - 1, (breakLength.z - 1) / 2)
    }

    if (`type` == ActiveSkill.BREAK.gettypenum && level < 3)
      end = new XYZTuple(end.x, end.y + 1, end.z)
    if (`type` == ActiveSkill.BREAK.gettypenum && level < 3 && mineflagnum == 1)
      shift(0, 1, 0)

    //スタートリストに追加
    startList.add(start)
    endList.add(end)

    //破壊回数だけリストに追加
    (1 until breakNum).foreach { _ =>
      dir match {
        case "N" | "E" | "S" | "W" =>
          shift(0, 0, breakLength.z)
        case "U" | "D" if assaultflag || level >= 3 =>
          shift(0, breakLength.y, 0)
      }
      startList.add(start)
      endList.add(end)
    }

    dir match {
      case "N" => rotateXZ(180)
      case "E" => rotateXZ(270)
      case "S" =>
      case "W" => rotateXZ(90)
      case "U" =>
      case "D" if !assaultflag => multiply_Y(-1)
    }
  }

  private def multiply_Y(i: Int): Unit = {
    (1 to breakNum).foreach { count =>
      val start = startList.get(count)
      val end = endList.get(count)

      val (newS, newE) =
        if (i >= 0)
          (XYZTuple(start.x, start.y * i, start.z), XYZTuple(end.x, end.y * i, end.z))
        else
          (XYZTuple(start.x, end.y * i, start.z), XYZTuple(end.x, start.y * i, end.z))

      startList.set(count, newS)
      endList.set(count, newE)
    }
  }

  private def shift(x: Int, y: Int, z: Int): Unit = {
    start = new XYZTuple(start.x + x, start.y + y, start.z + z)
    end = new XYZTuple(end.x + x, end.y + y, end.z + z)
  }

  private def rotateXZ(d: Int): Unit = {
    (0 until breakNum).foreach { count =>
      val start = startList.get(count)
      val end = endList.get(count)
      d match {
        case 90 =>
          startList.set(count, new XYZTuple(-end.z, start.y, start.x))
          endList.set(count, new XYZTuple(-start.z, end.y, end.x))
        case 180 =>
          startList.set(count, new XYZTuple(start.x, start.y, -end.z))
          endList.set(count, new XYZTuple(end.x, end.y, -start.z))
        case 270 =>
          startList.set(count, new XYZTuple(start.z, start.y, start.x))
          endList.set(count, new XYZTuple(end.z, end.y, end.x))
        case 360 =>
      }
    }
  }

  def getBreakLength: XYZTuple = breakLength

  def getBreakNum: Int = breakNum
}