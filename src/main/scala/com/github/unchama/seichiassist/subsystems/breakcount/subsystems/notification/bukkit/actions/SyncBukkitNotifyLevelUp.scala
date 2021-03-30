package com.github.unchama.seichiassist.subsystems.breakcount.subsystems.notification.bukkit.actions

import cats.effect.Sync
import com.github.unchama.generic.Diff
import com.github.unchama.minecraft.actions.MinecraftServerThreadShift
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.{SeichiLevel, SeichiStarLevel}
import com.github.unchama.seichiassist.subsystems.breakcount.subsystems.notification.application.actions.NotifyLevelUp
import com.github.unchama.seichiassist.util.Util
import org.bukkit.ChatColor.GOLD
import org.bukkit.entity.Player

object SyncBukkitNotifyLevelUp {

  import cats.implicits._

  def apply[
    F[_] : MinecraftServerThreadShift
  ](implicit F: Sync[F]): NotifyLevelUp[F, Player] = new NotifyLevelUp[F, Player] {
    override def ofSeichiLevelTo(player: Player)(diff: Diff[SeichiLevel]): F[Unit] = {
      val Diff(oldLevel, newLevel) = diff

      val titleMessage = s"Lv${oldLevel.level} -> Lv${newLevel.level}"
      val subtitleMessage = s"${GOLD}ﾑﾑｯwwwwwwwﾚﾍﾞﾙｱｯﾌﾟwwwwwww"

      if (oldLevel < newLevel) {
        MinecraftServerThreadShift[F].shift >> F.delay {
          player.sendTitle(titleMessage, subtitleMessage, 1, 20 * 5, 1)
          Util.launchFireWorks(player.getLocation)
        }
      } else F.unit
    }

    override def ofSeichiStarLevelTo(player: Player)(diff: Diff[SeichiStarLevel]): F[Unit] = {
      val Diff(oldStars, newStars) = diff
      val message = {
        if (oldStars == SeichiStarLevel.zero) {
          s"$GOLD★☆★ﾑﾑｯwwwwwwwﾚﾍﾞﾙｱｯﾌﾟwwwwwww★☆★【Lv199→Lv200(☆(${newStars.level}))】"
        } else {
          s"$GOLD★☆★ﾑﾑｯwwwwwwwﾚﾍﾞﾙｱｯﾌﾟwwwwwww★☆★【Lv200(☆(${oldStars.level}))→Lv200(☆(${newStars.level}))】"
        }
      }

      if (oldStars < newStars) F.delay(player.sendMessage(message))
      else F.unit
    }
  }

}
