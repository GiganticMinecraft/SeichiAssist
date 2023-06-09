package com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.bukkit

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.domain.{
  BreakSkillTargetConfig,
  BreakSkillTargetConfigKey,
  BreakSkillTargetConfigRepository
}
import org.bukkit.entity.Player

class BukkitBreakSkillTargetConfigRepository[F[_]: Sync](
  implicit breakFlagRepository: PlayerDataRepository[Ref[F, Set[BreakSkillTargetConfig]]]
) extends BreakSkillTargetConfigRepository[F, Player] {

  import cats.implicits._

  override def toggleBreakSkillTargetConfig(
    player: Player,
    configKey: BreakSkillTargetConfigKey
  ): F[Unit] = for {
    breakFlag <- breakSkillTargetConfig(player, configKey)
    _ <- breakFlagRepository(player).update { breakFlags =>
      breakFlags.filterNot(_.configKey == configKey) + BreakSkillTargetConfig(
        configKey,
        includes = !breakFlag
      )
    }
  } yield ()

  override def breakSkillTargetConfig(
    player: Player,
    configKey: BreakSkillTargetConfigKey
  ): F[Boolean] = for {
    flags <- breakFlagRepository(player).get
  } yield flags.find(_.configKey == configKey).fold(true)(_.includes)

}
