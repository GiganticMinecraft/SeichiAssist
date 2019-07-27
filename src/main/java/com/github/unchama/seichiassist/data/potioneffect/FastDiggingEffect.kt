package com.github.unchama.seichiassist.data.potioneffect

import com.github.unchama.seichiassist.util.TypeConverter
import org.bukkit.potion.PotionEffectType

/**
 * @param duration 持続ティック数
 * @param amplifier 効果の強さ
 * @param id 上昇値の種類
 *
 * [id] は以下の値を取り得る:
 * - 0 不明な上昇値
 * - 1 接続人数から
 * - 2 採掘量から
 * - 3 ドラゲナイタイムから
 * - 4 投票から
 * - 5 コマンド入力から(イベントや不具合等)
 *
 * TODO これをイミュータブルなデータクラスに
 * TODO [id]はenumに[effectDescription]
 */
class FastDiggingEffect(var duration: Int, var amplifier: Double, private val id: Int) {
  val effectDescription: String
    get() {
      return when (id) {
        0 -> "+$amplifier 不明な上昇値_${TypeConverter.toTimeString(duration / 20)}"
        1 -> "+$amplifier 接続人数から"
        2 -> "+$amplifier 整地量から"
        3 -> "+$amplifier ﾄﾞﾗｹﾞﾅｲﾀｲﾑから_${TypeConverter.toTimeString(duration / 20)}"
        4 -> "+$amplifier 投票ボーナスから_${TypeConverter.toTimeString(duration / 20)}"
        5 -> "+$amplifier コマンド入力から_${TypeConverter.toTimeString(duration / 20)}"
        else -> "+$amplifier 不明な上昇値_${TypeConverter.toTimeString(duration / 20)}"
      }
    }

  //６０秒固定採掘速度固定
  constructor(amplifier: Double, id: Int) : this(1260, amplifier, id)
}
