package com.github.unchama.seichiassist.subsystems.buildcount.application.actions

import cats.{Applicative, ~>}
import com.github.unchama.generic.Diff
import com.github.unchama.minecraft.actions.SendMinecraftMessage
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.{BuildAssistExpTable, BuildLevel}
import org.bukkit.ChatColor.GOLD

/**
 * 建築レベルの変化を通知する機構。
 *
 * TODO Tagless algebraにするべきぽい
 */
case class LevelUpNotifier[F[_], Player]()(
  implicit F: Applicative[F],
  send: SendMinecraftMessage[F, Player]
) {

  def notifyTo(player: Player)(diff: Diff[BuildLevel]): F[Unit] = {
    import cats.implicits._

    val Diff(oldLevel, newLevel) = diff
    if (newLevel eqv BuildAssistExpTable.maxLevel)
      SendMinecraftMessage[F, Player].string(player, s"${GOLD}最大Lvに到達したよ(`･ω･´)")
    else if (oldLevel < newLevel)
      SendMinecraftMessage[F, Player].string(
        player,
        s"${GOLD}ﾑﾑｯﾚﾍﾞﾙｱｯﾌﾟ∩( ・ω・)∩【建築Lv(${oldLevel.level})→建築Lv(${newLevel.level})】"
      )
    else
      Applicative[F].unit
  }

  def mapK[G[_]: Applicative](fg: F ~> G): LevelUpNotifier[G, Player] = {
    implicit val e: SendMinecraftMessage[G, Player] = send.mapK(fg)

    new LevelUpNotifier[G, Player]()
  }

}
