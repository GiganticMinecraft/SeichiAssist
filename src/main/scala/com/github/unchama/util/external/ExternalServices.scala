package com.github.unchama.util.external

import cats.effect.Sync
import chunk_search.{Chunk, ChunkCoord, SearchResult}
import com.github.unchama.util.MillisecondTimer
import com.github.unchama.util.bukkit.WorldUtil
import org.bukkit.World
import org.slf4j.Logger

/**
 * チャンク検索の外部コマンドが非 0 の終了コードで終了したことを表す例外。
 *
 * 失敗したコマンドの出力を「対象チャンク 0 件」として扱うと、
 * ワールドのアイテム変換が行われないまま適用済みとして記録されてしまうため、
 * この例外で変換処理全体を中断する。
 */
class ChunkSearchFailedException(
  val command: Seq[String],
  val exitCode: Int,
  val stderr: String
) extends RuntimeException(
      s"チャンク検索コマンドが終了コード $exitCode で失敗しました: ${command.mkString(" ")}\nstderr:\n$stderr"
    )

object ExternalServices {

  /**
   * 例外メッセージに含める stderr の最大行数。panic のバックトレース等で長くなりうるため末尾のみ残す。
   */
  private final val maxStderrLines = 100

  /**
   * `command` を外部プロセスとして実行し、標準出力を protobuf の [[SearchResult]] として解釈する。
   *
   * 終了コードが非 0 の場合、標準出力の内容によらず [[ChunkSearchFailedException]] を送出する。
   * protobuf では空のバイト列も正常な(空の)メッセージとして解釈されるため、
   * 終了コードを確認しないと失敗が「対象チャンク 0 件」と区別できない。
   */
  def runChunkSearch[F[_]: Sync](command: Seq[String]): F[SearchResult] = Sync[F].delay {
    import scala.sys.process.{BasicIO, Process, ProcessIO}

    val stdout = new java.util.concurrent.atomic.AtomicReference(Array.emptyByteArray)
    // stdout と stderr はそれぞれ別スレッドで読み切るので、パイプが詰まってデッドロックすることはない。
    // stderr はメモリを使いすぎないよう末尾の maxStderrLines 行だけを保持する
    val stderrTail = new java.util.ArrayDeque[String]()

    val processIO = new ProcessIO(
      _.close(),
      out =>
        try stdout.set(out.readAllBytes())
        finally out.close(),
      err =>
        BasicIO.processFully { line =>
          stderrTail.synchronized {
            if (stderrTail.size >= maxStderrLines) stderrTail.removeFirst()
            stderrTail.addLast(line)
          }
        }(err)
    )

    val process = Process(command).run(processIO)
    // exitValue() はプロセスの終了と stdout/stderr を読むスレッドの終了を待つ。
    // 待機中に割り込まれた場合等に外部プロセスが残らないよう、例外時は破棄する
    val exitCode =
      try process.exitValue()
      catch {
        case e: Throwable =>
          process.destroy()
          throw e
      }

    if (exitCode != 0) {
      val stderr = stderrTail.synchronized(String.join("\n", stderrTail))
      throw new ChunkSearchFailedException(command, exitCode, stderr)
    }

    SearchResult.parseFrom(stdout.get())
  }

  def getChunkCoordinates[F[_]: Sync](
    chunkSearchCommand: String
  )(world: World)(implicit logger: Logger): F[Seq[(Int, Int)]] = {
    import cats.implicits._

    // 従来の Runtime#exec(String) と同様に、設定値のコマンド文字列は空白で分割する。
    // ワールドのパスは空白を含みうるので分割せず 1 引数として渡す
    val buildCommand: F[Seq[String]] = Sync[F].delay {
      val commandBase = chunkSearchCommand.trim
      require(commandBase.nonEmpty, "chunk-search-command-base が設定されていません")
      commandBase.split("\\s+").toSeq :+ WorldUtil.getAbsoluteWorldFolder(world)
    }

    // 普通、この検索にはかなりの時間がかかるので要した時間をログに表示する
    MillisecondTimer
      .timeF((buildCommand >>= runChunkSearch[F]).map { searchResult =>
        searchResult.result.flatMap {
          // https://bukkit.org/threads/combining-world-and-world_nether.60319/
          // Bukkitの挙動でワールドとフォルダが一対一に対応するため、dimIdは無視して良い
          case Chunk(Some(ChunkCoord(x, z, _)), _, _) =>
            Some((x, z))
          case _ =>
            None
        }
      })(s"${world.getName}内のチャンクを検索しました。")
      .onError {
        case e: ChunkSearchFailedException =>
          Sync[F].delay {
            logger.error(
              s"${world.getName}内のチャンク検索(chunk-search)が終了コード${e.exitCode}で失敗したため、ワールドのアイテム変換を中断します。"
            )
          }
        case e =>
          Sync[F].delay {
            logger.error(s"${world.getName}内のチャンク検索(chunk-search)に失敗したため、ワールドのアイテム変換を中断します。", e)
          }
      }
      .flatTap { seq =>
        Sync[F].delay {
          logger.info(s"変換対象チャンク数${seq.size}")
        }
      }
  }

}
