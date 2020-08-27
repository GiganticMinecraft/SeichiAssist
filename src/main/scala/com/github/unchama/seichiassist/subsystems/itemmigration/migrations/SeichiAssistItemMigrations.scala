package com.github.unchama.seichiassist.subsystems.itemmigration.migrations

import cats.effect.SyncIO
import com.github.unchama.itemmigration.domain.ItemMigrations
import com.github.unchama.seichiassist.subsystems.itemmigration.domain.minecraft.UuidRepository
import org.slf4j.Logger

object SeichiAssistItemMigrations {

  // SeichiAssistが実施するアイテム変換の列
  def seq(implicit uuidRepository: UuidRepository[SyncIO], logger: Logger): ItemMigrations = ItemMigrations(IndexedSeq(
    V1_0_0_MigrateMebiusToNewCodec.migration
  ))

}
