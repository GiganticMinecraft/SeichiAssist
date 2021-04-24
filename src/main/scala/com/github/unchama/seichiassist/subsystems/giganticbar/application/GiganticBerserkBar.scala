package com.github.unchama.seichiassist.subsystems.giganticbar.application

import cats.Applicative
import com.github.unchama.minecraft.objects.MinecraftBossBar
import org.bukkit.ChatColor._

object GiganticBerserkBar {
  def write[F[_] : Applicative](totalOpt: Option[Int], nextLevelCapOpt: Option[Int], bossBar: MinecraftBossBar[F]): F[Unit] = {
    import cats.implicits._

    totalOpt match {
      case Some(value) =>
        val showText = nextLevelCapOpt match {
          case Some(nextLvCap) =>
            if (nextLvCap <= value) {
              s"$RED${BOLD}GiganticBerserk 討伐数 $value | 進化可能"
            } else {
              s"$RED${BOLD}GiganticBerserk 討伐数 $value | 次のレベルまで: ${nextLvCap - value}"
            }
          case None => s"$RED${BOLD}GiganticBerserk 討伐数 $value"
        }

        List(
          bossBar.visibility.write(true),
          bossBar.progress.write(nextLevelCapOpt.fold(1.0)(value.toDouble / _)),
          bossBar.title.write(showText)
        ).sequence.as(())
      case None => bossBar.visibility.write(false)
    }
  }
}
