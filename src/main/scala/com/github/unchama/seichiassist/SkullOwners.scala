package com.github.unchama.seichiassist

import com.github.unchama.itemstackbuilder.{SkullOwnerReference, SkullOwnerUuidWithName}

import java.util.UUID

/**
 * プレーヤーヘッドにownerとして設定されるプレーヤー達に関する定数を保持するオブジェクト
 */
object SkullOwners {
  val whitecat_haru: SkullOwnerReference = SkullOwnerUuidWithName(
    UUID.fromString("394f76df-883d-4855-9e6a-d1a800c1ab1c"),
    "whitecat_haru"
  )
  val unchama: SkullOwnerReference =
    SkullOwnerUuidWithName(UUID.fromString("b66cc3f6-a045-42ad-b4b8-320f20caf140"), "unchama")

  val MHF_ArrowUp: SkullOwnerReference = SkullOwnerUuidWithName(
    UUID.fromString("fef039ef-e6cd-4987-9c84-26a3e6134277"),
    "MHF_ArrowUp"
  )
  val MHF_ArrowDown: SkullOwnerReference = SkullOwnerUuidWithName(
    UUID.fromString("68f59b9b-5b0b-4b05-a9f2-e1d1405aa348"),
    "MHF_ArrowDown"
  )
  val MHF_ArrowLeft: SkullOwnerReference = SkullOwnerUuidWithName(
    UUID.fromString("a68f0b64-8d14-4000-a95f-4b9ba14f8df9"),
    "MHF_ArrowLeft"
  )
  val MHF_ArrowRight: SkullOwnerReference = SkullOwnerUuidWithName(
    UUID.fromString("50c8510b-5ea0-4d60-be9a-7d542d6cd156"),
    "MHF_ArrowRight"
  )

  val MHF_Exclamation: SkullOwnerReference = SkullOwnerUuidWithName(
    UUID.fromString("d3c47f6f-ae3a-45c1-ad7c-e2c762b03ae6"),
    "MHF_Exclamation"
  )

  val MHF_Villager: SkullOwnerReference = SkullOwnerUuidWithName(
    UUID.fromString("bd482739-767c-45dc-a1f8-c33c40530952"),
    "MHF_Villager"
  )
}
