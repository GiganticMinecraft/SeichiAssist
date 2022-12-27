package com.github.unchama.seichiassist.util

import cats.effect.SyncIO
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import org.bukkit.entity.Firework
import org.bukkit.{Color, FireworkEffect, Location}

import java.util.Random

object LaunchFireWorksEffect {
  // カラーをランダムで決める
  private def getRandomColors(length: Int): Array[Color] = {
    // 配列を作る
    val rand = new Random()
    // 配列の要素を順に処理していく
    // 24ビットカラーの範囲でランダムな色を決める

    // 配列を返す
    (0 until length).map { _ => Color.fromBGR(rand.nextInt(1 << 24)) }.toArray
  }

  /**
   * 指定された場所を起点として花火を打ち上げる関数。
   * 花火エフェクトは関数内のOnMinecraftServerThread上で実行される。
   * @param loc 花火を打ち上げる起点座標
   */
  def launchFireWorks[F[_]: OnMinecraftServerThread](loc: Location): F[Unit] = {
    val types = List(
      FireworkEffect.Type.BALL,
      FireworkEffect.Type.BALL_LARGE,
      FireworkEffect.Type.BURST,
      FireworkEffect.Type.CREEPER,
      FireworkEffect.Type.STAR
    )
    val effect = FireworkEffect.builder()
    val rand = new Random()

    // 形状をランダムに決める
    effect.`with`(types(rand.nextInt(types.size)))

    // 基本の色を単色～5色以内でランダムに決める
    effect.withColor(LaunchFireWorksEffect.getRandomColors(1 + rand.nextInt(5)): _*)

    // 余韻の色を単色～3色以内でランダムに決める
    effect.withFade(LaunchFireWorksEffect.getRandomColors(1 + rand.nextInt(3)): _*)

    // 爆発後に点滅するかをランダムに決める
    effect.flicker(rand.nextBoolean())

    // 爆発後に尾を引くかをランダムに決める
    effect.trail(rand.nextBoolean())

    OnMinecraftServerThread[F].runAction(SyncIO {
      // 花火を作る
      val firework = loc.getWorld.spawn(loc, classOf[Firework])

      // 花火の設定情報オブジェクトを取り出す
      val meta = firework.getFireworkMeta

      // 打ち上げ高さを1以上4以内でランダムに決める
      meta.setPower(1 + rand.nextInt(4))

      // 花火の設定情報を花火に設定
      meta.addEffect(effect.build())

      firework.setFireworkMeta(meta)
    })
  }

}
