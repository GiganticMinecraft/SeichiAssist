package com.github.unchama.seichiassist.data


import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.util.TypeConverter
import net.md_5.bungee.api.ChatColor
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import java.util.UUID


class Mana @deprecated() //引数なしのコンストラクタ
{value = 0
setMax (0)
loadflag = false
//マナの値
private var value: java.lang.Double = .0
//マックスの値
private var max: java.lang.Double = .0
//バークラス
private var bar: BossBar = null
//読み込まれているかどうかのフラグ
private var loadflag: Boolean = false
//必ず最初のみ実行してほしいメソッド
def initialize (player: Player, level: Int): Unit = { //現在のレベルでのマナ上限値を計算しバーに表示
//mの値は既に得られているはず。
loadflag = true
calcAndSetMax (player, level)
display (player, level)
}
//現在マナをマナバーに表示させる
def display (player: Player, level: Int): Unit = {if (! (loadflag) ) {return}
hide ()
setBar (player, level)
}
private def setBar (player: Player, level: Int): Unit = {bar = player.getServer.createBossBar (ChatColor.AQUA + "" + ChatColor.BOLD + "マナ(" + TypeConverter.Decimal (value) + "/" + getMax + ")", BarColor.BLUE, BarStyle.SOLID)
val beforeResetMax: Double = getMax
if (value < 0 || beforeResetMax < 0 || value > beforeResetMax) {reset (player, level)
player.sendMessage (ChatColor.RED + "不正な値がマナとして保存されていたためリセットしました。")
}
val trueMax: Double = getMax
if (trueMax <= 0) {return
}
bar.setProgress (value / trueMax)
bar.addPlayer (player)
}
private def reset (player: Player, level: Int): Unit = {calcAndSetMax (player, level)
if (value < 0.0) {value = 0}
if (value > getMax) {value = getMax}
}
//現在のバーを削除する（更新するときは不要）
def hide (): Unit = {if (bar != null) {bar.removeAll
}
}
private def ensureNotOverflow (): Unit = {if (value > getMax) {value = getMax}
}
def increase (amount: Double, player: Player, level: Int): Unit = {value += amount
ensureNotOverflow ()
display (player, level)
}
def decrease (amount: Double, player: Player, level: Int): Unit = {value -= amount
if (value < 0) {value = 0}
if (SeichiAssist.DEBUG) {value = getMax}
display (player, level)
}
def has (amount: Double): Boolean = {return value >= amount
}
//レベルアップするときに実行したい関数
def onLevelUp (player: Player, level: Int): Unit = {calcAndSetMax (player, level)
setFull (player, level)
}
//マナ最大値を計算する処理
def calcAndSetMax (player: Player, level: Int): Unit = { //UUIDを取得
val uuid: UUID = player.getUniqueId
//playerdataを取得
val playerdata: PlayerData = SeichiAssist.playermap.getOrElse (uuid, null)
if (SeichiAssist.DEBUG) {setMax (100000)
return
}
//レベルが10行っていない時レベルの値で処理を終了(修正:マナは0)
if (level < 10) { //this.max = level;
setMax (0.0)
return
}
//１０行ってる時の処理
var t_max: Double = 100
var increase: Int = 10
var inc_inc: Int = 2
//１１以降の処理
for (i <- 10 until level) {if (i % 10 == 0 && i <= 110 && i != 10) {increase += inc_inc
inc_inc *= 2
}
t_max += increase
}
//貢献度ptの上昇値
t_max += playerdata.added_mana * SeichiAssist.seichiAssistConfig.getContributeAddedMana
setMax (t_max)
}

/**
 * @param level レベル
 * @return 最大マナ
 */
def calcMaxManaOnly (player: Player, level: Int): Double = {val uuid: UUID = player.getUniqueId
val playerdata: PlayerData = SeichiAssist.playermap.getOrElse (uuid, null)
/*
		if(SeichiAssist.DEBUG){
			max = 100000;
			return;
		}
		*/
//レベルが10行っていない時レベルの値で処理を終了
if (level < 10) { //temp_max = level;
setMax (0.0)
return 0.0
}
var t_max: Double = 100
var increase: Int = 10
var inc_inc: Int = 2
for (i <- 10 until level) {if (i % 10 == 0 && i <= 110 && i != 10) {increase += inc_inc
inc_inc *= 2
}
t_max += increase
}
t_max += playerdata.added_mana * SeichiAssist.seichiAssistConfig.getContributeAddedMana
setMax (t_max)
return t_max
}
//マナを最大値まで回復する処理
def setFull (player: Player, level: Int): Unit = {value = getMax
display (player, level)
}
def getMana: Double = {return value
}
def setMana (m: Double): Unit = {value = m
}
def isLoaded: Boolean = {return loadflag
}
def getMax: Double = {return max
}
private def setMax (max: Double): Unit = {this.max = max
}
}
