package com.github.unchama.seichiassist.subsystems.itemmigration.migrations

import org.scalatest.wordspec.AnyWordSpec

class V1_5_0_MigrateGiganticGiftTicketLoreSpec extends AnyWordSpec {

  private val giftTicketName =
    """{"extra":[{"bold":true,"italic":true,"underlined":false,"strikethrough":false,"obfuscated":false,"color":"green","text":"ギガンティック・ギフト券"}],"text":""}"""

  private val oldBlankLore = """{"text":" "}"""
  private val newBlankLore = """{"extra":[{"text":" "}],"text":""}"""

  private val descriptionLore = Vector(
    """{"extra":[{"bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"color":"gray","text":"公共施設鯖にある"}],"text":""}""",
    """{"extra":[{"bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"color":"gray","text":"デパートで買い物ができます"}],"text":""}"""
  )

  private val oldLore = Vector(oldBlankLore) ++ descriptionLore :+ oldBlankLore
  private val newLore = Vector(newBlankLore) ++ descriptionLore :+ newBlankLore

  "ギガンティック・ギフト券のLore移行" should {
    "Issueの旧形式を1.18形式に変換する" in {
      assert(
        V1_5_0_MigrateGiganticGiftTicketLore
          .migratedLore(giftTicketName, oldLore)
          .contains(newLore)
      )
    }

    "片方だけ旧形式でも残りを変換する" in {
      val oldFirst = oldLore.updated(3, newBlankLore)
      val oldLast = oldLore.updated(0, newBlankLore)

      assert(
        V1_5_0_MigrateGiganticGiftTicketLore
          .migratedLore(giftTicketName, oldFirst)
          .contains(newLore)
      )
      assert(
        V1_5_0_MigrateGiganticGiftTicketLore
          .migratedLore(giftTicketName, oldLast)
          .contains(newLore)
      )
    }

    "既に1.18形式の券は変更しない" in {
      assert(V1_5_0_MigrateGiganticGiftTicketLore.migratedLore(giftTicketName, newLore).isEmpty)
    }

    "券名や説明文が異なるアイテムと不完全なLoreは変更しない" in {
      val migration = V1_5_0_MigrateGiganticGiftTicketLore

      assert(migration.migratedLore("別の券", oldLore).isEmpty)
      assert(migration.migratedLore(giftTicketName, oldLore.updated(1, "別の説明")).isEmpty)
      assert(migration.migratedLore(giftTicketName, oldLore.dropRight(1)).isEmpty)
    }
  }
}
