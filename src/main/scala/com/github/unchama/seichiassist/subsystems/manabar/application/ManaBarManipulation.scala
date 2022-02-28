package com.github.unchama.seichiassist.subsystems.manabar.application

import cats.Applicative
import com.github.unchama.minecraft.objects.MinecraftBossBar
import com.github.unchama.seichiassist.subsystems.mana.domain.{
  LevelCappedManaAmount,
  ManaAmount
}
import org.bukkit.ChatColor.{AQUA, BOLD}

import java.text.DecimalFormat

object ManaBarManipulation {

  import cats.implicits._

  // 3桁毎カンマ区切り、小数点以下一桁を表示
  private def formatAmount(manaAmount: ManaAmount): String = {
    val decimalFormat = new DecimalFormat("#,##0.0")
    decimalFormat.format(manaAmount.value)
  }

  def write[F[_]: Applicative](
    amount: LevelCappedManaAmount,
    bossBar: MinecraftBossBar[F]
  ): F[Unit] = {
    amount.ratioToCap match {
      case Some(fraction) =>
        List(
          bossBar.visibility.write(true),
          bossBar.progress.write(fraction),
          bossBar
            .title
            .write(
              s"$AQUA${BOLD}マナ(${formatAmount(amount.manaAmount)}/${formatAmount(amount.cap)})"
            )
        ).sequence.as(())
      case None =>
        bossBar.visibility.write(false)
    }
  }

}
