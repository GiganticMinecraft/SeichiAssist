package com.github.unchama.seichiassist.subsystems.itemmigration.migrations

import cats.effect.SyncIO
import com.github.unchama.itemmigration.domain.ItemMigrations
import com.github.unchama.seichiassist.subsystems.itemmigration.domain.minecraft.UuidRepository
import org.slf4j.Logger

object SeichiAssistItemMigrations {

  /**
   * SeichiAssistが実施するアイテム変換の列。
   *
   * 注意：エラー等で不完全なマイグレーションが走った時、同じ版のマイグレーションを複数回行いたいケースがある。 これに備え、マイグレーションはできるだけ冪等な処理として記述すべきである。
   */
  def seq(implicit uuidRepository: UuidRepository[SyncIO], logger: Logger): ItemMigrations =
    ItemMigrations(
      IndexedSeq(
        V1_0_0_MigrateMebiusToNewCodec.migration,
        V1_1_0_AddUnbreakableToNarutoRemake.migration,
        V1_2_0_FixTypoOf4thAnniversaryGT.migration,
        V1_3_0_RemoveUnnecessaryLoreOfHalloweenItem.migration,
        V1_4_0_AddEnchantsTo2_1billionRewardItems.migration
      )
    )

}
