package com.github.unchama.util.external

import cats.effect.IO
import chunk_search.{Chunk, ChunkCoord, SearchResult}
import org.scalatest.wordspec.AnyWordSpec

import java.nio.file.Files

class ExternalServicesSpec extends AnyWordSpec {

  "ExternalServices.runChunkSearch" should {
    "終了コード0なら標準出力をSearchResultとして解釈する" in {
      val expected =
        SearchResult(Seq(Chunk(Some(ChunkCoord(1, -2))), Chunk(Some(ChunkCoord(30, 40)), 1)))
      val outputFile = Files.createTempFile("chunk-search-spec", ".bin")
      try {
        Files.write(outputFile, expected.toByteArray)

        val actual =
          ExternalServices.runChunkSearch[IO](Seq("cat", outputFile.toString)).unsafeRunSync()

        assert(actual == expected)
      } finally {
        Files.deleteIfExists(outputFile)
      }
    }

    "終了コード0で出力が空なら空のSearchResultになる" in {
      val actual = ExternalServices.runChunkSearch[IO](Seq("true")).unsafeRunSync()

      assert(actual.result.isEmpty)
    }

    "終了コードが非0なら標準出力が空でもChunkSearchFailedExceptionを送出する" in {
      val command = Seq("sh", "-c", "echo 'TagNotFound { name: \"Level\" }' >&2; exit 101")

      val error = intercept[ChunkSearchFailedException] {
        ExternalServices.runChunkSearch[IO](command).unsafeRunSync()
      }

      assert(error.exitCode == 101)
      assert(error.command == command)
      assert(error.stderr.contains("TagNotFound"))
      assert(error.getMessage.contains("TagNotFound"))
    }

    "stderrは末尾の行だけを保持する" in {
      val command = Seq(
        "sh",
        "-c",
        "i=1; while [ $i -le 1000 ]; do echo line$i >&2; i=$((i+1)); done; exit 1"
      )

      val error = intercept[ChunkSearchFailedException] {
        ExternalServices.runChunkSearch[IO](command).unsafeRunSync()
      }

      val lines = error.stderr.linesIterator.toList
      assert(lines.size == 100)
      assert(lines.head == "line901")
      assert(lines.last == "line1000")
    }
  }

}
