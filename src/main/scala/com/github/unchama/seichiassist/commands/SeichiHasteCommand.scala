package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.{ContextualExecutorBuilder, Parsers}
import com.github.unchama.contextualexecutor.executors.EchoExecutor
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.potioneffect.FastDiggingEffect
import com.github.unchama.seichiassist.util.TypeConverter
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.syntax._
import enumeratum._
import org.bukkit.Bukkit
import org.bukkit.ChatColor._
import org.bukkit.command.{CommandSender, TabExecutor}

object SeichiHasteCommand {
  private val descriptionPrintExecutor = new EchoExecutor(List(
    s"$RED/seichihaste [説明文id] [効果の持続ティック数] [効果の強さ] [スコープ指定子]",
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

  sealed trait ScopeSpecification extends EnumEntry

  case object ScopeSpecification extends Enum[ScopeSpecification] {
    val values: IndexedSeq[ScopeSpecification] = findValues

    case object PLAYER extends ScopeSpecification

    case object ALL extends ScopeSpecification
  }

  val executor: TabExecutor = ContextualExecutorBuilder.beginConfiguration()
    .argumentsParsers(
      List(
        Parsers.closedRangeInt(0, 5, "説明文idは0から5の整数を指定してください。".asMessageEffect()),
        Parsers.nonNegativeInteger("効果の持続ティック数は非負の整数を指定してください。".asMessageEffect()),
        Parsers.double("効果の強さは実数を指定してください。".asMessageEffect()),
        Parsers.fromOptionParser(ScopeSpecification.withNameLowercaseOnlyOption,
          "スコープ指定子はallかplayerのどちらかを指定してください。".asMessageEffect())
      ),
      onMissingArguments = descriptionPrintExecutor
    )
    .execution { context =>
      val descriptionId = context.args.parsed(0).asInstanceOf[Int]
      val effectLengthInTick = context.args.parsed(1).asInstanceOf[Int]
      val effectAmplifier = context.args.parsed(2).asInstanceOf[Double]
      val scope = context.args.parsed(3).asInstanceOf[ScopeSpecification]

      val effectData = new FastDiggingEffect(effectLengthInTick, effectAmplifier, descriptionId)
      val effectLengthString = TypeConverter.toTimeString(effectLengthInTick / 20)

      def execution(): TargetedEffect[CommandSender] = {
        scope match {
          case ScopeSpecification.PLAYER =>
            val playerNameOption = context.args.yetToBeParsed.headOption
            playerNameOption match {
              case None => "対象のプレーヤー名を指定してください。".asMessageEffect()
              case Some(name) =>
                val playerData = SeichiAssist.playermap(Bukkit.getPlayer(name).getUniqueId)
                if (playerData == null) {
                  return s"プレーヤー $name はオンラインではありません。".asMessageEffect()
                }

                playerData.effectdatalist.addOne(effectData)
                s"$LIGHT_PURPLE$name に上昇値 $effectAmplifier を $effectLengthString 追加しました".asMessageEffect()
            }
          case ScopeSpecification.ALL =>
            SeichiAssist.playermap.values.foreach(_.effectdatalist.addOne(effectData))
            s"${LIGHT_PURPLE}すべてのプレーヤーに上昇値 $effectAmplifier を $effectLengthString 追加しました".asMessageEffect()
        }
      }

      IO(execution())
    }
    .build()
    .asNonBlockingTabExecutor()
}