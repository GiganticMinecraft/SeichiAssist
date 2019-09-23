package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.builder.{ContextualExecutorBuilder, Parsers}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.commands.SeichiHasteCommand.ScopeSpecification.{ALL, PLAYER}
import com.github.unchama.seichiassist.util.TypeConverter
import org.bukkit.Bukkit
import org.bukkit.ChatColor._

object SeichiHasteCommand {
  sealed trait ScopeSpecification
  case object ScopeSpecification {
    case object PLAYER extends ScopeSpecification
    case object ALL extends ScopeSpecification
  }

  object ScopeSpecification {
    def fromString(string: String): Option[ScopeSpecification] = string match {
      case "player" => Some(PLAYER)
      case "all" => Some(ALL)
      case _ => None
    }
  }

  private val descriptionPrintExecutor = EchoExecutor(List(
    s"${RED}/seichihaste [説明文id] [効果の持続ティック数] [効果の強さ] [スコープ指定子]",
      "指定されたプレイヤーに採掘速度上昇効果を付与します。",
      "同じサーバーにログイン中であるプレーヤーにしか適用されません。",
      "",
      "[スコープ指定子]は、 player [プレーヤー名] または all のどちらかです。",
      "",
      "[説明文id]は上昇値に付加する説明文を指定します。",
      "指定できるidは0から5の整数で、それぞれ次の説明文を指します。",
      " - 0 不明な上昇値",
      " - 1 接続人数から",
      " - 2 採掘量から",
      " - 3 ドラゲナイタイムから",
      " - 4 投票から",
      " - 5 コマンド入力から(イベントや不具合等)"
  ).asMessageEffect())

  val executor = ContextualExecutorBuilder.beginConfiguration()
      .argumentsParsers(
          List(
              Parsers.closedRangeInt(0, 5, "説明文idは0から5の整数を指定してください。".asMessageEffect()),
              Parsers.nonNegativeInteger("効果の持続ティック数は非負の整数を指定してください。".asMessageEffect()),
              Parsers.double("効果の強さは実数を指定してください。".asMessageEffect()),
              parser { argument =>
                ScopeSpecification.fromString(argument)
                    ?.let { succeedWith(it) } ?: failWith("スコープ指定子はallかplayerのどちらかを指定してください。")
              }
          ),
          onMissingArguments = descriptionPrintExecutor
      )
      .execution { context =>
        val descriptionId = context.args.parsed[0].asInstanceOf[Int]
        val effectLengthInTick = context.args.parsed[1].asInstanceOf[Int]
        val effectAmplifier = context.args.parsed[2].asInstanceOf[Double]
        val scope = context.args.parsed[3].asInstanceOf[ScopeSpecification]

        val effectData = FastDiggingEffect(effectLengthInTick, effectAmplifier, descriptionId)
        val effectLengthString = TypeConverter.toTimeString(effectLengthInTick / 20)

        when (scope) {
          ScopeSpecification.PLAYER => {
            val playerName = context.args.yetToBeParsed
              .firstOrNull() ?: return@execution "対象のプレーヤー名を指定してください。".asMessageEffect()

            val playerData = Bukkit.getPlayer(playerName)
              ?.let { SeichiAssist.playermap(it.uniqueId) } ?: return@execution s"プレーヤー $playerName はオンラインではありません。".asMessageEffect()

            playerData.effectdatalist.add(effectData)

            s"${LIGHT_PURPLE}$playerName に上昇値 $effectAmplifier を $effectLengthString 追加しました".asMessageEffect()
          }
          ScopeSpecification.ALL => {
            SeichiAssist.playermap.values.forEach {
              it.effectdatalist.add(effectData)
            }

            s"${LIGHT_PURPLE}すべてのプレーヤーに上昇値 $effectAmplifier を $effectLengthString 追加しました".asMessageEffect()
          }
        }
      }
      .build()
      .asNonBlockingTabExecutor()
}