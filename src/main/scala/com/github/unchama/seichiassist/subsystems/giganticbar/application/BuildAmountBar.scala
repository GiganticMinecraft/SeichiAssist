package com.github.unchama.seichiassist.subsystems.giganticbar.application

import cats.Applicative
import com.github.unchama.minecraft.objects.MinecraftBossBar
import org.bukkit.ChatColor._

object BuildAmountBar {
  def write[F[_] : Applicative](total: Int, nextLvCap: Option[Int], bossBar: MinecraftBossBar[F]): F[Unit] = {
    val showText = nextLvCap.fold(s"${GREEN}${BOLD}総建築量: $total") { cap =>
      s"${GREEN}${BOLD}総建築量: $total | 次のレベルまで: ${cap - total}"
    }
    import cats.implicits._
    List(
      bossBar.visibility.write(true),
      bossBar.progress.write(nextLvCap.fold(1.0)(total.toDouble / _)),
      bossBar.title.write(showText)
    ).sequence.as(())
  }
}
