package com.github.unchama.seasonalevents.seizonsiki

import java.text.{ParseException, SimpleDateFormat}
import java.util.Date

import org.bukkit.plugin.Plugin

class Seizonsiki(private val plugin: Plugin) {
  private val DROPDAY = "2017-01-16"
  private val FINISH = "2017-01-22"

  try {
    val format = new SimpleDateFormat("yyyy-MM-dd")
    val finishDate = format.parse(FINISH)
    val dropDate = format.parse(DROPDAY)

    val now = new Date
    // イベント開催中か判定
    if (now.before(finishDate)) plugin.getServer.getPluginManager.registerEvents(new SeizonsikiListener(), plugin)
    if (now.before(dropDate)) Seizonsiki.isDrop = true
  } catch {
    case e: ParseException => e.printStackTrace()
  }
}

object Seizonsiki {
  val FINISHDISP = "2017/01/21"
  val DROPDAYDISP = "2017/01/15"
  val FINISH = "2017-01-22"
  var isDrop = false
}