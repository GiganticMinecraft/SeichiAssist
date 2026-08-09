package com.github.unchama.seichiassist.tools.pastgacha

import io.circe.Json

import java.util.Locale

/**
 * 1.12.2形式の過去ガチャ景品giveコマンドを1.18.2形式へ変換する。
 */
object PastGachaGiveCommandConverter:
  import GiveCommandConversionError.*
  import SnbtValue.*

  private final case class LegacyGiveCommand(
    target: String,
    itemId: String,
    amount: Int,
    dataValue: Int,
    nbt: Compound
  )

  private final case class FlattenedItem(itemId: String, damage: Option[Int])

  private final case class LegacyTextStyle(color: Option[String], formats: Set[LegacyFormat])

  private enum LegacyFormat(val code: Char, val jsonField: String):
    case Bold extends LegacyFormat('l', "bold")
    case Strikethrough extends LegacyFormat('m', "strikethrough")
    case Underlined extends LegacyFormat('n', "underlined")
    case Italic extends LegacyFormat('o', "italic")
    case Obfuscated extends LegacyFormat('k', "obfuscated")

  private final case class TextSegment(text: String, style: LegacyTextStyle)

  private val colorByCode = Map(
    '0' -> "black",
    '1' -> "dark_blue",
    '2' -> "dark_green",
    '3' -> "dark_aqua",
    '4' -> "dark_red",
    '5' -> "dark_purple",
    '6' -> "gold",
    '7' -> "gray",
    '8' -> "dark_gray",
    '9' -> "blue",
    'a' -> "green",
    'b' -> "aqua",
    'c' -> "red",
    'd' -> "light_purple",
    'e' -> "yellow",
    'f' -> "white"
  )

  private val enchantmentByNumericId = Map(
    0 -> "protection",
    1 -> "fire_protection",
    2 -> "feather_falling",
    3 -> "blast_protection",
    4 -> "projectile_protection",
    5 -> "respiration",
    6 -> "aqua_affinity",
    7 -> "thorns",
    8 -> "depth_strider",
    9 -> "frost_walker",
    10 -> "binding_curse",
    16 -> "sharpness",
    17 -> "smite",
    18 -> "bane_of_arthropods",
    19 -> "knockback",
    20 -> "fire_aspect",
    21 -> "looting",
    22 -> "sweeping",
    32 -> "efficiency",
    33 -> "silk_touch",
    34 -> "unbreaking",
    35 -> "fortune",
    48 -> "power",
    49 -> "punch",
    50 -> "flame",
    51 -> "infinity",
    61 -> "luck_of_the_sea",
    62 -> "lure",
    70 -> "mending",
    71 -> "vanishing_curse"
  )

  private val attributeNameConversions = Map(
    "generic.maxHealth" -> "generic.max_health",
    "generic.followRange" -> "generic.follow_range",
    "generic.knockbackResistance" -> "generic.knockback_resistance",
    "generic.movementSpeed" -> "generic.movement_speed",
    "generic.attackDamage" -> "generic.attack_damage",
    "generic.attackSpeed" -> "generic.attack_speed",
    "generic.armor" -> "generic.armor",
    "generic.armorToughness" -> "generic.armor_toughness",
    "generic.luck" -> "generic.luck"
  )

  private val damageableItemNames = Set(
    "wooden_sword",
    "stone_sword",
    "iron_sword",
    "golden_sword",
    "diamond_sword",
    "wooden_shovel",
    "stone_shovel",
    "iron_shovel",
    "golden_shovel",
    "diamond_shovel",
    "wooden_pickaxe",
    "stone_pickaxe",
    "iron_pickaxe",
    "golden_pickaxe",
    "diamond_pickaxe",
    "wooden_axe",
    "stone_axe",
    "iron_axe",
    "golden_axe",
    "diamond_axe",
    "wooden_hoe",
    "stone_hoe",
    "iron_hoe",
    "golden_hoe",
    "diamond_hoe",
    "leather_helmet",
    "leather_chestplate",
    "leather_leggings",
    "leather_boots",
    "chainmail_helmet",
    "chainmail_chestplate",
    "chainmail_leggings",
    "chainmail_boots",
    "iron_helmet",
    "iron_chestplate",
    "iron_leggings",
    "iron_boots",
    "golden_helmet",
    "golden_chestplate",
    "golden_leggings",
    "golden_boots",
    "diamond_helmet",
    "diamond_chestplate",
    "diamond_leggings",
    "diamond_boots",
    "bow",
    "fishing_rod",
    "flint_and_steel",
    "shears",
    "carrot_on_a_stick",
    "shield",
    "elytra"
  )

  private val skullItemByDataValue = Map(
    0 -> "minecraft:skeleton_skull",
    1 -> "minecraft:wither_skeleton_skull",
    2 -> "minecraft:zombie_head",
    3 -> "minecraft:player_head",
    4 -> "minecraft:creeper_head",
    5 -> "minecraft:dragon_head"
  )

  private val musicDiscNames = Set(
    "13",
    "cat",
    "blocks",
    "chirp",
    "far",
    "mall",
    "mellohi",
    "stal",
    "strad",
    "ward",
    "11",
    "wait"
  )

  def convert(command: String): Either[GiveCommandConversionError, String] =
    for
      parsed <- parseCommand(command)
      flattened <- flattenItem(parsed.itemId, parsed.dataValue)
      withDamage <- addDamage(parsed.nbt, flattened.damage)
      withText <- convertDisplayText(withDamage)
      withEnchantments <- convertEnchantments(withText)
      withAttributes <- convertAttributeModifiers(withEnchantments)
      convertedNbt <- convertSkullOwner(withAttributes)
    yield s"/give ${parsed.target} ${flattened.itemId}${convertedNbt.render} ${parsed.amount}"

  private def parseCommand(
    command: String
  ): Either[GiveCommandConversionError, LegacyGiveCommand] =
    val trimmed = command.trim
    val withoutCommandName =
      if trimmed.startsWith("/give ") then Right(trimmed.drop(6))
      else if trimmed.startsWith("give ") then Right(trimmed.drop(5))
      else Left(InvalidCommand("先頭は /give または give である必要があります"))

    withoutCommandName.flatMap: arguments =>
      val (tokens, nbtSource) = splitArgumentsAndNbt(arguments)
      for
        target <- tokens.headOption.toRight(InvalidCommand("対象がありません"))
        itemId <- tokens.lift(1).toRight(InvalidCommand("アイテムIDがありません"))
        _ <- Either.cond(tokens.length <= 4, (), InvalidCommand("引数が多すぎます"))
        amount <- parseOptionalInt(tokens.lift(2), 1, "個数")
        dataValue <- parseOptionalInt(tokens.lift(3), 0, "データ値")
        nbt <- parseNbt(nbtSource)
      yield LegacyGiveCommand(target, normalizeItemId(itemId), amount, dataValue, nbt)

  private def splitArgumentsAndNbt(arguments: String): (Vector[String], Option[String]) =
    val nbtStart = arguments.indexOf('{')
    val (argumentSource, nbtSource) =
      if nbtStart < 0 then (arguments, None)
      else (arguments.take(nbtStart), Some(arguments.drop(nbtStart)))
    (argumentSource.trim.split("\\s+").filter(_.nonEmpty).toVector, nbtSource)

  private def parseOptionalInt(
    rawValue: Option[String],
    default: Int,
    fieldName: String
  ): Either[GiveCommandConversionError, Int] =
    rawValue match
      case None        => Right(default)
      case Some(value) =>
        value.toIntOption.toRight(InvalidCommand(s"$fieldName が整数ではありません: $value"))

  private def parseNbt(source: Option[String]): Either[GiveCommandConversionError, Compound] =
    source match
      case None        => Right(Compound(Vector.empty))
      case Some(value) =>
        SnbtParser
          .parseCompound(value)
          .left
          .map(failure => InvalidSnbt(failure.position, failure.detail))

  private def flattenItem(
    itemId: String,
    dataValue: Int
  ): Either[GiveCommandConversionError, FlattenedItem] =
    val itemName = itemId.stripPrefix("minecraft:")
    if itemName == "skull" then
      skullItemByDataValue
        .get(dataValue)
        .map(FlattenedItem(_, None))
        .toRight(UnsupportedDataValue(itemId, dataValue))
    else if itemName == "golden_apple" && dataValue == 1 then
      Right(FlattenedItem("minecraft:enchanted_golden_apple", None))
    else if itemName.startsWith("record_") && musicDiscNames.contains(itemName.drop(7)) then
      if dataValue == 0 then
        Right(FlattenedItem(s"minecraft:music_disc_${itemName.drop(7)}", None))
      else Left(UnsupportedDataValue(itemId, dataValue))
    else if dataValue == 0 then Right(FlattenedItem(itemId, None))
    else if damageableItemNames.contains(itemName) then
      Right(FlattenedItem(itemId, Some(dataValue)))
    else Left(UnsupportedDataValue(itemId, dataValue))

  private def normalizeItemId(itemId: String): String =
    if itemId.contains(':') then itemId else s"minecraft:$itemId"

  private def addDamage(
    nbt: Compound,
    damage: Option[Int]
  ): Either[GiveCommandConversionError, Compound] =
    damage match
      case None        => Right(nbt)
      case Some(value) =>
        nbt.entries.find(_.key == "Damage") match
          case None => Right(Compound(nbt.entries :+ SnbtEntry("Damage", Atom(value.toString))))
          case Some(SnbtEntry(_, Atom(existing))) if existing.toIntOption.contains(value) =>
            Right(nbt)
          case Some(_) => Left(InvalidField("Damage", "データ値と既存のDamageが一致しません"))

  private def convertDisplayText(nbt: Compound): Either[GiveCommandConversionError, Compound] =
    updateOptionalCompound(nbt, "display"): display =>
      for
        withName <- updateOptionalValue(display, "Name", convertTextValue("display.Name"))
        withLore <- updateOptionalValue(withName, "Lore", convertLore)
      yield withLore

  private def convertTextValue(fieldName: String)(
    value: SnbtValue
  ): Either[GiveCommandConversionError, SnbtValue] =
    value match
      case QuotedString(text, _) => Right(QuotedString(renderLegacyText(text), '\''))
      case _                     => Left(InvalidField(fieldName, "文字列である必要があります"))

  private def convertLore(value: SnbtValue): Either[GiveCommandConversionError, SnbtValue] =
    value match
      case ListValue(lines) =>
        traverse(lines.zipWithIndex): (line, index) =>
          convertTextValue(s"display.Lore[$index]")(line)
        .map(ListValue.apply)
      case _ => Left(InvalidField("display.Lore", "文字列のlistである必要があります"))

  private def renderLegacyText(text: String): String =
    val initialStyle = LegacyTextStyle(None, Set.empty)
    val (segments, remainingText, remainingStyle) =
      parseLegacyText(text, Vector.empty, new StringBuilder, initialStyle, 0)
    val allSegments = appendSegment(segments, remainingText, remainingStyle)
    val jsonComponents =
      if allSegments.isEmpty then Vector(textSegmentToJson(TextSegment("", initialStyle)))
      else allSegments.map(textSegmentToJson)
    if jsonComponents.size == 1 then jsonComponents.head.noSpaces
    else Json.arr((Json.fromString("") +: jsonComponents)*).noSpaces

  private def parseLegacyText(
    text: String,
    segments: Vector[TextSegment],
    currentText: StringBuilder,
    style: LegacyTextStyle,
    position: Int
  ): (Vector[TextSegment], StringBuilder, LegacyTextStyle) =
    if position >= text.length then (segments, currentText, style)
    else if text.charAt(position) == '§' && position + 1 < text.length then
      val code = text.charAt(position + 1).toLower
      val appended = appendSegment(segments, currentText, style)
      colorByCode.get(code) match
        case Some(color) =>
          parseLegacyText(
            text,
            appended,
            new StringBuilder,
            LegacyTextStyle(Some(color), Set.empty),
            position + 2
          )
        case None if code == 'r' =>
          parseLegacyText(
            text,
            appended,
            new StringBuilder,
            LegacyTextStyle(None, Set.empty),
            position + 2
          )
        case None =>
          LegacyFormat.values.find(_.code == code) match
            case Some(format) =>
              parseLegacyText(
                text,
                appended,
                new StringBuilder,
                style.copy(formats = style.formats + format),
                position + 2
              )
            case None =>
              currentText.append('§').append(text.charAt(position + 1))
              parseLegacyText(text, segments, currentText, style, position + 2)
    else
      currentText.append(text.charAt(position))
      parseLegacyText(text, segments, currentText, style, position + 1)

  private def appendSegment(
    segments: Vector[TextSegment],
    text: StringBuilder,
    style: LegacyTextStyle
  ): Vector[TextSegment] =
    if text.isEmpty then segments else segments :+ TextSegment(text.result(), style)

  private def textSegmentToJson(segment: TextSegment): Json =
    val formatFields = LegacyFormat
      .values
      .toVector
      .flatMap: format =>
        Option.when(segment.style.formats.contains(format))(format.jsonField -> Json.True)
    val italicReset =
      Option.when(
        segment.style.color.nonEmpty && !segment.style.formats.contains(LegacyFormat.Italic)
      )("italic" -> Json.False)
    Json.obj(
      (Vector("text" -> Json.fromString(segment.text)) ++
        segment.style.color.map("color" -> Json.fromString(_)) ++
        formatFields ++ italicReset)*
    )

  private def convertEnchantments(nbt: Compound): Either[GiveCommandConversionError, Compound] =
    for
      renamed <- renameField(nbt, "ench", "Enchantments")
      converted <- updateOptionalValue(renamed, "Enchantments", convertEnchantmentList)
      withStored <- updateOptionalValue(converted, "StoredEnchantments", convertEnchantmentList)
    yield withStored

  private def convertEnchantmentList(
    value: SnbtValue
  ): Either[GiveCommandConversionError, SnbtValue] =
    value match
      case ListValue(enchantments) =>
        traverse(enchantments.zipWithIndex): (enchantment, index) =>
          enchantment match
            case compound: Compound => convertEnchantment(compound, index)
            case _ => Left(InvalidField(s"Enchantments[$index]", "compoundである必要があります"))
        .map(ListValue.apply)
      case _ => Left(InvalidField("Enchantments", "compoundのlistである必要があります"))

  private def convertEnchantment(
    enchantment: Compound,
    index: Int
  ): Either[GiveCommandConversionError, Compound] =
    for
      withId <- updateRequiredValue(enchantment, "id", s"Enchantments[$index].id"):
        case Atom(rawId) =>
          parseNumericAtom(rawId)
            .toRight(InvalidField(s"Enchantments[$index].id", s"数値IDではありません: $rawId"))
            .flatMap(id => enchantmentByNumericId.get(id).toRight(UnsupportedEnchantmentId(id)))
            .map(name => QuotedString(s"minecraft:$name", '"'))
        case quoted: QuotedString => Right(quoted)
        case _ => Left(InvalidField(s"Enchantments[$index].id", "数値または文字列が必要です"))
      withLevel <- updateRequiredValue(withId, "lvl", s"Enchantments[$index].lvl"):
        case Atom(rawLevel) if rawLevel.matches("[-+]?\\d+")     => Right(Atom(s"${rawLevel}s"))
        case Atom(rawLevel) if rawLevel.matches("[-+]?\\d+[sS]") =>
          Right(Atom(s"${rawLevel.dropRight(1)}s"))
        case _ => Left(InvalidField(s"Enchantments[$index].lvl", "数値が必要です"))
    yield withLevel

  private def convertAttributeModifiers(
    nbt: Compound
  ): Either[GiveCommandConversionError, Compound] =
    updateOptionalValue(
      nbt,
      "AttributeModifiers",
      value =>
        value match
          case ListValue(modifiers) =>
            traverse(modifiers.zipWithIndex): (modifier, index) =>
              modifier match
                case compound: Compound => convertAttributeModifier(compound, index)
                case _                  =>
                  Left(InvalidField(s"AttributeModifiers[$index]", "compoundである必要があります"))
            .map(ListValue.apply)
          case _ => Left(InvalidField("AttributeModifiers", "compoundのlistである必要があります"))
    )

  private def convertAttributeModifier(
    modifier: Compound,
    index: Int
  ): Either[GiveCommandConversionError, Compound] =
    for
      withAttributeName <- updateOptionalValue(
        modifier,
        "AttributeName",
        convertAttributeName(s"AttributeModifiers[$index].AttributeName")
      )
      withName <- updateOptionalValue(
        withAttributeName,
        "Name",
        convertAttributeName(s"AttributeModifiers[$index].Name")
      )
      withUuid <- convertAttributeUuid(withName, index)
    yield withUuid

  private def convertAttributeName(fieldName: String)(
    value: SnbtValue
  ): Either[GiveCommandConversionError, SnbtValue] =
    value match
      case QuotedString(name, quote) =>
        Right(QuotedString(attributeNameConversions.getOrElse(name, name), quote))
      case Atom(name) => Right(Atom(attributeNameConversions.getOrElse(name, name)))
      case _          => Left(InvalidField(fieldName, "文字列である必要があります"))

  private def convertAttributeUuid(
    modifier: Compound,
    index: Int
  ): Either[GiveCommandConversionError, Compound] =
    val mostEntry = modifier.entries.find(_.key == "UUIDMost")
    val leastEntry = modifier.entries.find(_.key == "UUIDLeast")
    (mostEntry, leastEntry) match
      case (None, None)                      => Right(modifier)
      case (Some(_), None) | (None, Some(_)) =>
        Left(InvalidField(s"AttributeModifiers[$index].UUID", "UUIDMostとUUIDLeastの両方が必要です"))
      case (Some(most), Some(least)) =>
        for
          mostValue <- parseLongValue(most.value, s"AttributeModifiers[$index].UUIDMost")
          leastValue <- parseLongValue(least.value, s"AttributeModifiers[$index].UUIDLeast")
          _ <- Either.cond(
            !modifier.entries.exists(_.key == "UUID"),
            (),
            InvalidField(s"AttributeModifiers[$index].UUID", "新旧両方のUUID指定があります")
          )
        yield replaceUuidFields(modifier, mostValue, leastValue)

  private def replaceUuidFields(modifier: Compound, most: Long, least: Long): Compound =
    val uuidPosition =
      modifier.entries.indexWhere(entry => entry.key == "UUIDMost" || entry.key == "UUIDLeast")
    val withoutLegacyUuid =
      modifier.entries.filterNot(entry => entry.key == "UUIDMost" || entry.key == "UUIDLeast")
    val uuid = SnbtEntry(
      "UUID",
      IntArray(Vector((most >>> 32).toInt, most.toInt, (least >>> 32).toInt, least.toInt))
    )
    Compound(withoutLegacyUuid.patch(uuidPosition, Vector(uuid), 0))

  private def parseLongValue(
    value: SnbtValue,
    fieldName: String
  ): Either[GiveCommandConversionError, Long] =
    value match
      case Atom(raw) =>
        val withoutSuffix = raw.stripSuffix("L").stripSuffix("l")
        withoutSuffix.toLongOption.toRight(InvalidField(fieldName, s"long値ではありません: $raw"))
      case _ => Left(InvalidField(fieldName, "long値である必要があります"))

  private def convertSkullOwner(nbt: Compound): Either[GiveCommandConversionError, Compound] =
    updateOptionalCompound(nbt, "SkullOwner"): owner =>
      updateOptionalValue(owner, "Id", convertSkullOwnerId)

  private def convertSkullOwnerId(
    value: SnbtValue
  ): Either[GiveCommandConversionError, SnbtValue] =
    value match
      case QuotedString(uuid, _) =>
        val compact = uuid.replace("-", "")
        if compact.matches("[0-9a-fA-F]{32}") then
          val values =
            compact.grouped(8).map(hex => java.lang.Long.parseLong(hex, 16).toInt).toVector
          Right(IntArray(values))
        else Left(InvalidField("SkullOwner.Id", s"UUID形式ではありません: $uuid"))
      case existing: IntArray => Right(existing)
      case _ => Left(InvalidField("SkullOwner.Id", "UUID文字列またはint配列である必要があります"))

  private def renameField(
    compound: Compound,
    oldName: String,
    newName: String
  ): Either[GiveCommandConversionError, Compound] =
    if compound.entries.exists(_.key == oldName) && compound.entries.exists(_.key == newName)
    then Left(InvalidField(oldName, s"$oldName と $newName の両方があります"))
    else
      Right(
        Compound(
          compound
            .entries
            .map(entry => if entry.key == oldName then entry.copy(key = newName) else entry)
        )
      )

  private def updateOptionalCompound(compound: Compound, key: String)(
    transform: Compound => Either[GiveCommandConversionError, Compound]
  ): Either[GiveCommandConversionError, Compound] =
    updateOptionalValue(
      compound,
      key,
      value =>
        value match
          case value: Compound => transform(value)
          case _               => Left(InvalidField(key, "compoundである必要があります"))
    )

  private def updateOptionalValue(
    compound: Compound,
    key: String,
    transform: SnbtValue => Either[GiveCommandConversionError, SnbtValue]
  ): Either[GiveCommandConversionError, Compound] =
    compound.entries.indexWhere(_.key == key) match
      case -1    => Right(compound)
      case index =>
        transform(compound.entries(index).value).map(value =>
          Compound(compound.entries.updated(index, SnbtEntry(key, value)))
        )

  private def updateRequiredValue(compound: Compound, key: String, fieldName: String)(
    transform: SnbtValue => Either[GiveCommandConversionError, SnbtValue]
  ): Either[GiveCommandConversionError, Compound] =
    if compound.entries.exists(_.key == key) then updateOptionalValue(compound, key, transform)
    else Left(InvalidField(fieldName, "必須フィールドがありません"))

  private def parseNumericAtom(rawValue: String): Option[Int] =
    rawValue.toLowerCase(Locale.ROOT).stripSuffix("s").toIntOption

  private def traverse[A, B](values: Vector[A])(
    transform: A => Either[GiveCommandConversionError, B]
  ): Either[GiveCommandConversionError, Vector[B]] =
    values.foldLeft[Either[GiveCommandConversionError, Vector[B]]](Right(Vector.empty)):
      case (result, value) =>
        for
          accumulated <- result
          converted <- transform(value)
        yield accumulated :+ converted
