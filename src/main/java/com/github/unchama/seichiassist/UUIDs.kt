package com.github.unchama.seichiassist

import java.util.*

/**
 * コード内から直接参照されるようなプレーヤーのUUIDを持つオブジェクト
 */
object UUIDs {
  val whitecat_haru: UUID = UUID.fromString("394f76df-883d-4855-9e6a-d1a800c1ab1c")
  val unchama: UUID = UUID.fromString("b66cc3f6-a045-42ad-b4b8-320f20caf140")

  val MHF_ArrowUp: UUID = UUID.fromString("fef039ef-e6cd-4987-9c84-26a3e6134277")
  val MHF_ArrowDown: UUID = UUID.fromString("68f59b9b-5b0b-4b05-a9f2-e1d1405aa348")
  val MHF_ArrowLeft: UUID = UUID.fromString("a68f0b64-8d14-4000-a95f-4b9ba14f8df9")
  val MHF_ArrowRight: UUID = UUID.fromString("50c8510b-5ea0-4d60-be9a-7d542d6cd156")

  val MHF_Villager: UUID = UUID.fromString("bd482739-767c-45dc-a1f8-c33c40530952")
}
