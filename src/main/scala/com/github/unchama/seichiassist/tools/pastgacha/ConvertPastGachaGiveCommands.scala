package com.github.unchama.seichiassist.tools.pastgacha

import java.nio.charset.StandardCharsets
import java.nio.file.StandardOpenOption.{CREATE_NEW, WRITE}
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.*

/**
 * 1行1コマンドのファイルを変換し、新しいファイルへ出すCLI。
 */
object ConvertPastGachaGiveCommands:
  private val MaximumInputBytes = 10L * 1024L * 1024L

  def main(arguments: Array[String]): Unit =
    arguments.toList match
      case inputFile :: outputFile :: Nil =>
        convertFile(Path.of(inputFile), Path.of(outputFile))
      case _ =>
        System
          .err
          .println("Usage: ConvertPastGachaGiveCommands <1.12.2-input.txt> <1.18.2-output.txt>")
        sys.exit(2)

  private def convertFile(inputFile: Path, outputFile: Path): Unit =
    val size = Files.size(inputFile)
    if size > MaximumInputBytes then
      System.err.println(s"入力ファイルが10 MiBを超えています: $size bytes")
      sys.exit(2)
    if Files.exists(outputFile) then
      System.err.println(s"出力ファイルは既に存在します: $outputFile")
      sys.exit(2)

    val lines = Files.readAllLines(inputFile, StandardCharsets.UTF_8)
    val converted = Vector.newBuilder[String]
    val iterator = lines.iterator()
    var lineNumber = 1
    while iterator.hasNext do
      val line = iterator.next()
      if line.trim.isEmpty || line.trim.startsWith("#") then converted += line
      else
        PastGachaGiveCommandConverter.convert(line) match
          case Right(command) => converted += command
          case Left(error)    =>
            System.err.println(s"$inputFile:$lineNumber: ${error.message}")
            sys.exit(1)
      lineNumber += 1

    val _ = Files.write(
      outputFile,
      converted.result().asJava,
      StandardCharsets.UTF_8,
      CREATE_NEW,
      WRITE
    )
