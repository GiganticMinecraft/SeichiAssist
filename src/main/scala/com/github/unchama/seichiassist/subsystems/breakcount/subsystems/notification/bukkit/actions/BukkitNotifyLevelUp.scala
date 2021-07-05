package com.github.unchama.seichiassist.subsystems.breakcount.subsystems.notification.bukkit.actions

import cats.Applicative
import cats.effect.{Sync, SyncIO}
import com.github.unchama.generic.Diff
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.{SeichiLevel, SeichiStarLevel}
import com.github.unchama.seichiassist.subsystems.breakcount.subsystems.notification.application.actions.NotifyLevelUp
import com.github.unchama.seichiassist.util.Util
import org.bukkit.ChatColor.GOLD
import org.bukkit.entity.Player

object BukkitNotifyLevelUp {

  import cats.implicits._

  def apply[
    F[_] : OnMinecraftServerThread : Sync,
  ]: NotifyLevelUp[F, Player] = new NotifyLevelUp[F, Player] {
    override def ofSeichiLevelTo(player: Player)(diff: Diff[SeichiLevel]): F[Unit] = {
      val Diff(oldLevel, newLevel) = diff

      val titleMessage = s"【Lv${oldLevel.level}→Lv${newLevel.level}】"
      val subtitleMessage = s"${GOLD}ﾑﾑｯwwwwwwwﾚﾍﾞﾙｱｯﾌﾟwwwwwww"

      if (oldLevel < newLevel) {
        OnMinecraftServerThread[F].runAction(SyncIO {
          player.sendTitle(titleMessage, subtitleMessage, 1, 20 * 5, 1)
          player.sendMessage(s"$subtitleMessage$titleMessage")
          Util.launchFireWorks(player.getLocation)
        })
      } else Applicative[F].unit
    }

    override def ofSeichiStarLevelTo(player: Player)(diff: Diff[SeichiStarLevel]): F[Unit] = {
      val Diff(oldStars, newStars) = diff
      val titleMessage = {
        if (oldStars == SeichiStarLevel.zero) {
          s"【Lv199→Lv200(☆(${newStars.level}))】"
        } else {
          s"【Lv200(☆(${oldStars.level}))→Lv200(☆(${newStars.level}))】"
        }
      }
      val subTitleMessage = s"$GOLD★☆★ﾑﾑｯwwwwwwwﾚﾍﾞﾙｱｯﾌﾟwwwwwww★☆★"

      if (oldStars < newStars) Sync[F].delay {
        player.sendTitle(titleMessage, subTitleMessage, 10, 70, 20)
        player.sendMessage(s"$subTitleMessage$titleMessage")
        Util.launchFireWorks(player.getLocation)
      }
      else Applicative[F].unit
    }
  }

}
