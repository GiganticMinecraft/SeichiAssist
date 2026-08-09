package com.github.unchama.seichiassist.tools.pastgacha

import org.scalatest.wordspec.AnyWordSpec

class PastGachaGiveCommandConverterSpec extends AnyWordSpec:
  private val pluto112 =
    "/give @p minecraft:diamond_sword 1 0 " +
      "{display:{Name:\"§c§l§oP§6§l§oL§e§l§oU§a§l§oT§9§l§oO §f§l§o5thAnniv.\"," +
      "Lore:[\"\",\"§b攻撃時に敵をウィザー状態にします\",\"§a最大体力§f(小)§a増加\"," +
      "\"§a移動速度§e(中)§a増加\",\"§a攻撃のクールタイム減少\",\"§aノックバック耐性\"," +
      "\"\",\"§e金床 §c不可\",\"§e修繕 §b可\",\"\",\"テクスチャ製作者：nubasu\",\"\"]}," +
      "HideFlags:2,RepairCost:100,ench:[{id:16,lvl:10},{id:21,lvl:5},{id:34,lvl:10}," +
      "{id:70,lvl:1}],AttributeModifiers:[" +
      "{AttributeName:\"generic.maxHealth\",Name:\"generic.maxHealth\",Amount:4,Operation:0," +
      "UUIDLeast:591650,UUIDMost:374371}," +
      "{AttributeName:\"generic.knockbackResistance\",Name:\"generic.knockbackResistance\"," +
      "Amount:10,Operation:0,UUIDLeast:329560,UUIDMost:23227}," +
      "{AttributeName:\"generic.movementSpeed\",Name:\"generic.movementSpeed\",Amount:0.08," +
      "Operation:0,UUIDLeast:759764,UUIDMost:823744}," +
      "{AttributeName:\"generic.attackDamage\",Name:\"generic.attackDamage\",Amount:17," +
      "Operation:0,UUIDLeast:384867,UUIDMost:253468}," +
      "{AttributeName:\"generic.attackSpeed\",Name:\"generic.attackSpeed\",Amount:-1," +
      "Operation:0,UUIDLeast:149252,UUIDMost:96488}]}"

  "PastGachaGiveCommandConverter" should:
    "装飾、修繕、属性を含むPLUTO景品を1.18.2形式へ変換する" in:
      val converted = PastGachaGiveCommandConverter.convert(pluto112)

      assert(converted.isRight)
      val command = converted.toOption.get
      assert(command.startsWith("/give @p minecraft:diamond_sword{"))
      assert(command.endsWith("} 1"))
      assert(
        command.contains(
          "Name:'[\"\",{\"text\":\"P\",\"color\":\"red\",\"bold\":true,\"italic\":true}"
        )
      )
      assert(
        command.contains(
          "Lore:['{\"text\":\"\"}','{\"text\":\"攻撃時に敵をウィザー状態にします\",\"color\":\"aqua\",\"italic\":false}'"
        )
      )
      assert(
        command.contains(
          "'[\"\",{\"text\":\"最大体力\",\"color\":\"green\",\"italic\":false}," +
            "{\"text\":\"(小)\",\"color\":\"white\",\"italic\":false}," +
            "{\"text\":\"増加\",\"color\":\"green\",\"italic\":false}]'"
        )
      )
      assert(command.contains("{id:\"minecraft:mending\",lvl:1s}"))
      assert(!command.contains("binding_curse"))
      assert(!command.contains("§"))
      assert(!command.contains("ench:"))
      assert(command.contains("AttributeName:\"generic.attack_speed\""))
      assert(command.contains("UUID:[I;0,96488,0,149252]"))
      assert(!command.contains("UUIDMost"))
      assert(!command.contains("UUIDLeast"))

    "色コードが後続の装飾をリセットし、複数componentの書式継承を防ぐ" in:
      val command = "/give @p minecraft:paper 1 0 {display:{Name:\"§l太字§c赤§o斜体\"}}"

      assert(
        PastGachaGiveCommandConverter
          .convert(command)
          .contains(
            "/give @p minecraft:paper{display:{Name:'[\"\",{\"text\":\"太字\",\"bold\":true}," +
              "{\"text\":\"赤\",\"color\":\"red\",\"italic\":false}," +
              "{\"text\":\"斜体\",\"color\":\"red\",\"italic\":true}]'}} 1"
          )
      )

    "エンチャントID 10を束縛の呪い、70を修繕へ変換する" in:
      val command =
        "/give @p minecraft:book 1 0 {ench:[{id:10,lvl:1},{id:70,lvl:2}]}"

      assert(
        PastGachaGiveCommandConverter
          .convert(command)
          .contains(
            "/give @p minecraft:book{Enchantments:[{id:\"minecraft:binding_curse\",lvl:1s}," +
              "{id:\"minecraft:mending\",lvl:2s}]} 1"
          )
      )

    "エンチャント本のStoredEnchantmentsを維持して数値IDだけを変換する" in:
      val command =
        "/give @p minecraft:enchanted_book 1 0 {StoredEnchantments:[{id:70,lvl:1s}]}"

      assert(
        PastGachaGiveCommandConverter
          .convert(command)
          .contains(
            "/give @p minecraft:enchanted_book{StoredEnchantments:[{id:\"minecraft:mending\",lvl:1s}]} 1"
          )
      )

    "UUIDMostとUUIDLeastの全64bitを符号付きint配列へ変換する" in:
      val command =
        "/give @p minecraft:stick 1 0 {AttributeModifiers:[{UUIDMost:-1L,UUIDLeast:-9223372036854775808L}]}"

      assert(
        PastGachaGiveCommandConverter
          .convert(command)
          .contains(
            "/give @p minecraft:stick{AttributeModifiers:[{UUID:[I;-1,-1,-2147483648,0]}]} 1"
          )
      )

    "プレイヤーヘッドのIDとSkullOwner UUIDを変換する" in:
      val command =
        "/give @p minecraft:skull 1 3 {SkullOwner:{Id:\"069a79f4-44e9-4726-a5be-fca90e38aaf5\"}}"

      assert(
        PastGachaGiveCommandConverter
          .convert(command)
          .contains(
            "/give @p minecraft:player_head{SkullOwner:{Id:[I;110787060,1156138790,-1514210135,238594805]}} 1"
          )
      )

    "壊れるアイテムの非ゼロデータ値をDamageへ移す" in:
      val command = "/give @p minecraft:diamond_pickaxe 2 123 {Unbreakable:1b}"

      assert(
        PastGachaGiveCommandConverter
          .convert(command)
          .contains("/give @p minecraft:diamond_pickaxe{Unbreakable:1b,Damage:123} 2")
      )

    "種類を表すデータ値とレコードIDをフラット化する" in:
      assert(
        PastGachaGiveCommandConverter
          .convert("/give @p minecraft:golden_apple 1 1")
          .contains("/give @p minecraft:enchanted_golden_apple{} 1")
      )
      assert(
        PastGachaGiveCommandConverter
          .convert("/give @p minecraft:record_13 1 0")
          .contains("/give @p minecraft:music_disc_13{} 1")
      )

    "意味を判定できない非ゼロデータ値をエラーにする" in:
      assert(
        PastGachaGiveCommandConverter.convert("/give @p minecraft:stone 1 3") ==
          Left(GiveCommandConversionError.UnsupportedDataValue("minecraft:stone", 3))
      )

    "未知の数値エンチャントIDをエラーにする" in:
      assert(
        PastGachaGiveCommandConverter
          .convert("/give @p minecraft:book 1 0 {ench:[{id:999,lvl:1}]}") ==
          Left(GiveCommandConversionError.UnsupportedEnchantmentId(999))
      )

    "エンチャントレベルが数値でなければエラーにする" in:
      val converted = PastGachaGiveCommandConverter.convert(
        "/give @p minecraft:book 1 0 {ench:[{id:70,lvl:high}]}"
      )

      assert(converted.isLeft)
