package com.github.unchama.seichiassist.data;


import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.data.XYZTuple;
import com.github.unchama.seichiassist.util.BreakUtil;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BreakArea {
    //スキルタイプ番号
    int type;
    //スキルレベル
    int level;
    //フラグ
    int mineflagnum;
    //南向きを基準として破壊の範囲座標
    XYZTuple breaklength;
    //破壊回数
    int breaknum;
    //向いている方角
    String dir;
    //破壊範囲を示す相対座標リスト
    List<XYZTuple> startlist, endlist;
    //アサルトスキルの時true
    boolean assaultflag;
    //変数として利用する相対座標
    private XYZTuple start, end;


    public BreakArea(Player player, int type, int skilllevel, int mineflagnum, boolean assaultflag) {
        this.type = type;
        this.level = skilllevel;
        this.mineflagnum = mineflagnum;
        this.assaultflag = assaultflag;
        this.dir = BreakUtil.getCardinalDirection(player);
        this.startlist = new ArrayList<>();
        this.endlist = new ArrayList<>();
        //初期化
        ActiveSkill[] as = ActiveSkill.values();
        this.breaklength = as[type - 1].getBreakLength(level);
        this.breaknum = as[type - 1].getRepeatTimes(level);

        //初期範囲設定
        makeArea();
    }

    public List<XYZTuple> getStartList() {
        return startlist;
    }

    public List<XYZTuple> getEndList() {
        return endlist;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    //破壊範囲の設定
    public void makeArea() {
        startlist.clear();
        endlist.clear();
        //中心座標(0,0,0)のスタートとエンドを仮取得
        start = new XYZTuple(-(breaklength.x() - 1) / 2, -(breaklength.y() - 1) / 2, -(breaklength.z() - 1) / 2);
        end = new XYZTuple((breaklength.x() - 1) / 2, (breaklength.y() - 1) / 2, (breaklength.z() - 1) / 2);
        //アサルトスキルの時
        if (assaultflag) {
            if (type == 6 && level == 10) {
                shift(0, (breaklength.y() - 1) / 2 - 1, 0);
            }
        }
        //上向きまたは下向きの時
        else if (dir.equals("U") || dir.equals("D")) {
            if (!assaultflag && level < 3) {
            } else {
                shift(0, (breaklength.y() - 1) / 2, 0);
            }
        }
        //それ以外の範囲
        else {
            shift(0, (breaklength.y() - 1) / 2 - 1, (breaklength.z() - 1) / 2);

        }

        if (type == ActiveSkill.BREAK.gettypenum() && level < 3) {
            end = new XYZTuple(end.x(), end.y() + 1, end.z());
        }
        if (type == ActiveSkill.BREAK.gettypenum() && level < 3 && mineflagnum == 1) {
            shift(0, 1, 0);
        }

        //スタートリストに追加
        startlist.add(start);
        endlist.add(end);

        //破壊回数だけリストに追加

        for (int count = 1; count < breaknum; count++) {
            switch (dir) {
                case "N":
                case "E":
                case "S":
                case "W":
                    shift(0, 0, breaklength.z());
                    break;
                case "U":
                case "D":
                    if (!assaultflag && level < 3) {
                    } else {
                        shift(0, breaklength.y(), 0);
                    }
                    break;

            }
            startlist.add(start);
            endlist.add(end);
        }


        switch (dir) {
            case "N":
                rotateXZ(180);
                break;
            case "E":
                rotateXZ(270);
                break;
            case "S":
                break;
            case "W":
                rotateXZ(90);
                break;
            case "U":
                break;
            case "D":
                if (!assaultflag) multiply_Y(-1);
                break;
        }
    }

    private void multiply_Y(int i) {
        for (int count = 0; count < breaknum; count++) {
            XYZTuple start = startlist.get(count);
            XYZTuple end = endlist.get(count);
            if (i >= 0) {
                startlist.set(count, new XYZTuple(start.x(), start.y() * i, start.z()));
                endlist.set(count, new XYZTuple(end.x(), end.y() * i, end.z()));
            } else {
                startlist.set(count, new XYZTuple(start.x(), end.y() * i, start.z()));
                endlist.set(count, new XYZTuple(end.x(), start.y() * i, end.z()));
            }
        }
    }

    private void shift(int x, int y, int z) {
        start = new XYZTuple(start.x() + x, start.y() + y, start.z() + z);
        end = new XYZTuple(end.x() + x,  end.y() + y, end.z() + z);
    }

    private void rotateXZ(int d) {
        for (int count = 0; count < breaknum; count++) {
            XYZTuple start = startlist.get(count);
            XYZTuple end = endlist.get(count);
            switch (d) {
                case 90:
                    startlist.set(count, new XYZTuple(-end.z(), start.y(), start.x()));
                    endlist.set(count, new XYZTuple(-start.z(), end.y(), end.x()));
                    break;
                case 180:
                    startlist.set(count, new XYZTuple(start.x(), start.y(), -end.z()));
                    endlist.set(count, new XYZTuple(end.x(), end.y(), -start.z()));
                    break;
                case 270:
                    startlist.set(count, new XYZTuple(start.z(), start.y(), start.x()));
                    endlist.set(count, new XYZTuple(end.z(), end.y(), end.x()));
                    break;
                case 360:
                    break;
            }
        }
    }

    public XYZTuple getBreakLength() {
        return breaklength;
    }

    public int getBreakNum() {
        return breaknum;
    }


}
