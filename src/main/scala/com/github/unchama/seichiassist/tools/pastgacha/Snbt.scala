package com.github.unchama.seichiassist.tools.pastgacha

private[pastgacha] final case class SnbtEntry(key: String, value: SnbtValue)

private[pastgacha] enum SnbtValue:
  case Compound(entries: Vector[SnbtEntry])
  case ListValue(values: Vector[SnbtValue])
  case IntArray(values: Vector[Int])
  case QuotedString(value: String, quote: Char)
  case Atom(value: String)

  def render: String = this match
    case Compound(entries) =>
      entries
        .map(entry => s"${SnbtValue.renderKey(entry.key)}:${entry.value.render}")
        .mkString("{", ",", "}")
    case ListValue(values)          => values.map(_.render).mkString("[", ",", "]")
    case IntArray(values)           => values.mkString("[I;", ",", "]")
    case QuotedString(value, quote) =>
      val escaped = value.flatMap:
        case '\\'                            => "\\\\"
        case character if character == quote => s"\\$character"
        case character                       => character.toString
      s"$quote$escaped$quote"
    case Atom(value) => value

private[pastgacha] object SnbtValue:
  private val unquotedKeyPattern = "[A-Za-z0-9._+\\-]+".r

  private def renderKey(key: String): String =
    if unquotedKeyPattern.matches(key) then key
    else QuotedString(key, '\'').render

private[pastgacha] final case class SnbtParseFailure(position: Int, detail: String)

private[pastgacha] object SnbtParser:
  def parseCompound(input: String): Either[SnbtParseFailure, SnbtValue.Compound] =
    new Parser(input).parseRootCompound()

  private final class Parser(input: String):
    private var position = 0

    def parseRootCompound(): Either[SnbtParseFailure, SnbtValue.Compound] =
      for
        value <- parseValue()
        compound <- value match
          case value: SnbtValue.Compound => Right(value)
          case _                         => fail("ルートNBTはcompoundである必要があります")
        _ <-
          skipWhitespace()
          if atEnd then Right(()) else fail("NBTの後ろに余分な文字があります")
      yield compound

    private def parseValue(): Either[SnbtParseFailure, SnbtValue] =
      skipWhitespace()
      currentCharacter match
        case Some('{')  => parseCompound()
        case Some('[')  => parseListOrIntArray()
        case Some('\'') => parseQuotedString('\'')
        case Some('"')  => parseQuotedString('"')
        case Some(_)    => parseAtom()
        case None       => fail("値が必要です")

    private def parseCompound(): Either[SnbtParseFailure, SnbtValue.Compound] =
      position += 1
      skipWhitespace()
      if consumeIf('}') then Right(SnbtValue.Compound(Vector.empty))
      else parseCompoundEntries(Vector.empty)

    private def parseCompoundEntries(
      entries: Vector[SnbtEntry]
    ): Either[SnbtParseFailure, SnbtValue.Compound] =
      for
        key <- parseKey()
        _ <- expect(':', "キーの後にコロンが必要です")
        value <- parseValue()
        result <-
          skipWhitespace()
          val parsed: Either[SnbtParseFailure, SnbtValue.Compound] =
            if consumeIf('}') then Right(SnbtValue.Compound(entries :+ SnbtEntry(key, value)))
            else if consumeIf(',') then parseCompoundEntries(entries :+ SnbtEntry(key, value))
            else fail("compoundの要素間にはカンマが必要です")
          parsed
      yield result

    private def parseKey(): Either[SnbtParseFailure, String] =
      skipWhitespace()
      currentCharacter match
        case Some('\'') => parseQuotedString('\'').map(_.value)
        case Some('"')  => parseQuotedString('"').map(_.value)
        case Some(_)    =>
          parseUnquotedUntil(character => character == ':' || character.isWhitespace)
        case None => fail("キーが必要です")

    private def parseListOrIntArray(): Either[SnbtParseFailure, SnbtValue] =
      position += 1
      skipWhitespace()
      if position + 1 < input.length && input.charAt(position).toUpper == 'I' && input.charAt(
          position + 1
        ) == ';'
      then
        position += 2
        parseIntArrayValues(Vector.empty)
      else if consumeIf(']') then Right(SnbtValue.ListValue(Vector.empty))
      else parseListValues(Vector.empty)

    private def parseListValues(
      values: Vector[SnbtValue]
    ): Either[SnbtParseFailure, SnbtValue.ListValue] =
      for
        value <- parseValue()
        result <-
          skipWhitespace()
          val parsed: Either[SnbtParseFailure, SnbtValue.ListValue] =
            if consumeIf(']') then Right(SnbtValue.ListValue(values :+ value))
            else if consumeIf(',') then parseListValues(values :+ value)
            else fail("listの要素間にはカンマが必要です")
          parsed
      yield result

    private def parseIntArrayValues(
      values: Vector[Int]
    ): Either[SnbtParseFailure, SnbtValue.IntArray] =
      skipWhitespace()
      if consumeIf(']') then Right(SnbtValue.IntArray(values))
      else
        for
          rawValue <- parseUnquotedUntil(character => character == ',' || character == ']')
          value <- rawValue
            .toIntOption
            .toRight(SnbtParseFailure(position, s"int配列に整数でない値があります: $rawValue"))
          result <-
            skipWhitespace()
            val parsed: Either[SnbtParseFailure, SnbtValue.IntArray] =
              if consumeIf(']') then Right(SnbtValue.IntArray(values :+ value))
              else if consumeIf(',') then parseIntArrayValues(values :+ value)
              else fail("int配列の要素間にはカンマが必要です")
            parsed
        yield result

    private def parseQuotedString(
      quote: Char
    ): Either[SnbtParseFailure, SnbtValue.QuotedString] =
      position += 1
      val value = new StringBuilder
      var escaped = false
      while position < input.length do
        val character = input.charAt(position)
        position += 1
        if escaped then
          value.append(character)
          escaped = false
        else if character == '\\' then escaped = true
        else if character == quote then
          return Right(SnbtValue.QuotedString(value.result(), quote))
        else value.append(character)
      fail("引用符が閉じられていません")

    private def parseAtom(): Either[SnbtParseFailure, SnbtValue.Atom] =
      parseUnquotedUntil(character =>
        character.isWhitespace || character == ',' || character == '}' || character == ']'
      ).map(SnbtValue.Atom.apply)

    private def parseUnquotedUntil(
      delimiter: Char => Boolean
    ): Either[SnbtParseFailure, String] =
      val start = position
      while position < input.length && !delimiter(input.charAt(position)) do position += 1
      if position == start then fail("空の値は指定できません")
      else Right(input.substring(start, position))

    private def expect(character: Char, detail: String): Either[SnbtParseFailure, Unit] =
      skipWhitespace()
      if consumeIf(character) then Right(()) else fail(detail)

    private def consumeIf(character: Char): Boolean =
      if currentCharacter.contains(character) then
        position += 1
        true
      else false

    private def skipWhitespace(): Unit =
      while position < input.length && input.charAt(position).isWhitespace do position += 1

    private def currentCharacter: Option[Char] =
      if atEnd then None else Some(input.charAt(position))

    private def atEnd: Boolean = position >= input.length

    private def fail[A](detail: String): Left[SnbtParseFailure, A] =
      Left(SnbtParseFailure(position, detail))
