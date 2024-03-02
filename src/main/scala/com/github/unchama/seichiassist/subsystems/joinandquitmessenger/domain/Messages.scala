package com.github.unchama.seichiassist.subsystems.joinandquitmessenger.domain

object Messages {

  def joinMessage(playerName: String): String = s"$playerName がログインしました"

  // Note: 整地鯖では退出メッセージを表示しない
  def quitMessage(playerName: String): String = ""

}
