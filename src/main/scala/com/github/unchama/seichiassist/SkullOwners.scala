package com.github.unchama.seichiassist

import com.github.unchama.itemstackbuilder.SkullOwnerUuidWithNameWithTextureUrl

import java.util.UUID

/**
 * プレーヤーヘッドにownerとして設定されるプレーヤー達に関する定数を保持するオブジェクト
 */
object SkullOwners {
  val whitecat_haru: SkullOwnerUuidWithNameWithTextureUrl =
    SkullOwnerUuidWithNameWithTextureUrl(
      UUID.fromString("394f76df-883d-4855-9e6a-d1a800c1ab1c"),
      "whitecat_haru",
      "http://textures.minecraft.net/texture/2e3a634a9276303c0fa480460391ea16fc50363913ba6cad078163398435b3dd"
    )
  val unchama: SkullOwnerUuidWithNameWithTextureUrl =
    SkullOwnerUuidWithNameWithTextureUrl(
      UUID.fromString("b66cc3f6-a045-42ad-b4b8-320f20caf140"),
      "unchama",
      "http://textures.minecraft.net/texture/7abe76c114f44d0b114db30a49a5539f53fefad03c683306f5af9f3e76bfd36d"
    )

  val MHF_ArrowUp: SkullOwnerUuidWithNameWithTextureUrl = SkullOwnerUuidWithNameWithTextureUrl(
    UUID.fromString("fef039ef-e6cd-4987-9c84-26a3e6134277"),
    "MHF_ArrowUp",
    "http://textures.minecraft.net/texture/a156b31cbf8f774547dc3f9713a770ecc5c727d967cb0093f26546b920457387"
  )

  val MHF_ArrowDown: SkullOwnerUuidWithNameWithTextureUrl =
    SkullOwnerUuidWithNameWithTextureUrl(
      UUID.fromString("68f59b9b-5b0b-4b05-a9f2-e1d1405aa348"),
      "MHF_ArrowDown",
      "http://textures.minecraft.net/texture/fe3d755cecbb13a39e8e9354823a9a02a01dce0aca68ffd42e3ea9a9d29e2df2"
    )

  val MHF_ArrowLeft: SkullOwnerUuidWithNameWithTextureUrl =
    SkullOwnerUuidWithNameWithTextureUrl(
      UUID.fromString("a68f0b64-8d14-4000-a95f-4b9ba14f8df9"),
      "MHF_ArrowLeft",
      "http://textures.minecraft.net/texture/f7aacad193e2226971ed95302dba433438be4644fbab5ebf818054061667fbe2"
    )

  val MHF_ArrowRight: SkullOwnerUuidWithNameWithTextureUrl =
    SkullOwnerUuidWithNameWithTextureUrl(
      UUID.fromString("50c8510b-5ea0-4d60-be9a-7d542d6cd156"),
      "MHF_ArrowRight",
      "http://textures.minecraft.net/texture/d34ef0638537222b20f480694dadc0f85fbe0759d581aa7fcdf2e43139377158"
    )

  val MHF_Exclamation: SkullOwnerUuidWithNameWithTextureUrl =
    SkullOwnerUuidWithNameWithTextureUrl(
      UUID.fromString("d3c47f6f-ae3a-45c1-ad7c-e2c762b03ae6"),
      "MHF_Exclamation",
      "http://textures.minecraft.net/texture/40b05e699d28b3a278a92d169dca9d57c0791d07994d82de3f9ed4a48afe0e1d"
    )

  val MHF_Villager: SkullOwnerUuidWithNameWithTextureUrl = SkullOwnerUuidWithNameWithTextureUrl(
    UUID.fromString("bd482739-767c-45dc-a1f8-c33c40530952"),
    "MHF_Villager",
    "http://textures.minecraft.net/texture/b4bd832813ac38e68648938d7a32f6ba29801aaf317404367f214b78b4d4754c"
  )

  val MHF_TNT: SkullOwnerUuidWithNameWithTextureUrl =
    SkullOwnerUuidWithNameWithTextureUrl(
      UUID.fromString("d43af93c-c330-4a3d-bab8-ee74234a011a"),
      "MHF_TNT",
      "http://textures.minecraft.net/texture/f92408fe8d0a3ef5531065e9f566c31aa6eb37484031a46e4466615daf64f705"
    )
}
