package com.github.unchama.seichiassist.data.potioneffect

import com.github.unchama.seichiassist.util.TypeConverter

/**
 * @param duration  持続ティック数
 * @param amplifier 効果の強さ
 * @param id        上昇値の種類
 *
 *                  [id] は以下の値を取り得る:
 * - 0 不明な上昇値
 * - 1 接続人数から
 * - 2 採掘量から
 * - 3 ドラゲナイタイムから
 * - 4 投票から
 * - 5 コマンド入力から(イベントや不具合等)
 *
 *                  TODO これをイミュータブルなデータクラスに
 *                  TODO [id]はenumに[effectDescription]
 */
class FastDiggingEffect(var duration: Int, var amplifier: Double, private val id: Int) {
  def effectDescription: String = {
    val effectStrength = String.format("%.2f", amplifier)
    val formattedDuration = TypeConverter.toTimeString(duration / 20)

    id match {
      case 0 => s"+$effectStrength 不明な上昇値_$formattedDuration"
      case 1 => s"+$effectStrength 接続人数から"
      case 2 => s"+$effectStrength 整地量から"
      case 3 => s"+$effectStrength ﾄﾞﾗｹﾞﾅｲﾀｲﾑから_$formattedDuration"
      case 4 => s"+$effectStrength 投票ボーナスから_$formattedDuration"
      case 5 => s"+$effectStrength コマンド入力から_$formattedDuration"
      case _ => s"+$effectStrength 不明な上昇値_$formattedDuration"
    }
  }

  //６０秒固定採掘速度固定
  def this(amplifier: Double, id: Int) = this(1260, amplifier, id)
}
