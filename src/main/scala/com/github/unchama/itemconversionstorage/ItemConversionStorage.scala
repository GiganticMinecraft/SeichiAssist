package com.github.unchama.itemconversionstorage

import cats.data
import cats.implicits._
import cats.effect.IO
import cats.kernel.Monoid
import com.github.unchama.menuinventory.{LayoutPreparationContext, MenuFrame, MenuSession}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.util.bukkit.ItemStackUtil
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * アイテム変換機構のトレイト。
 *
 * このトレイトを実装するオブジェクトは、Bukkitの[[org.bukkit.inventory.Inventory]]を入力とし、変換を行う。
 * これらのオブジェクトを、アイテム変換機構と呼ぶことにする。
 *
 * # Control flows
 * <pre>
 * [[doOperation]]
 *      |
 *      | -- [[[doMap]] (n times)] -- +
 *      |                           |
 *      | <-------------------------+
 *      |
 *      |
 *  onGrantItem
 *      |
 *      |
 *      v
 *  [[postEffect]]
 * </pre>
 */
trait ItemConversionStorage {
  /**
   * 実装者はこの関連型をConversionResultSetのままにしてはいけない。
   */
  type ResultSet <: ConversionResultSet
  type Environment

  val frame: MenuFrame
  // TODO: InventoryCloseEventにhookしないとうまく行かないんじゃないか？

  /**
   * インベントリを閉じたときに発火される作用。
   * @return `inventory`から変換されたアイテムのリストを計算する[[IO]]
   */
  def doOperation(player: Player, inventory: Map[Int, ItemStack])(implicit environment: Environment): IO[ResultSet] = {
    ItemStackUtil.amalgamate(inventory.values.toList).traverse(doMap(player, _)).map(_.combineAll(summonMonoid))
  }

  protected implicit def summonMonoid: Monoid[ResultSet]

  /**
   *
   * @param itemStack 変換する前のアイテム
   */
  def doMap(player: Player, itemStack: ItemStack)(implicit environment: Environment): IO[ResultSet]

  /**
   * @return 全ての変換が終わり、アイテム付与が終わったあとに発火されるプレイヤーに対するエフェクト。
   */
  //noinspection ScalaUnusedSymbol
  def postEffect(conversionResultSet: ResultSet): TargetedEffect[Player] = TargetedEffect.emptyEffect

  /**
   * メニューを[Player]に開かせる[TargetedEffect].
   */
  def open(implicit environment: Environment,
           ctx: LayoutPreparationContext,
           onMainThread: OnMinecraftServerThread[IO]): TargetedEffect[Player] = data.Kleisli { player =>
    for {
      session <- MenuSession.createNewSessionWith[IO](frame)
      _ <- session.openInventory.run(player)
      _ <- IO.shift(ctx)
      conversionResultSet <- doOperation(player, session.getInventory.getContents.zipWithIndex.map(_.swap).toMap)
      _ <- postEffect(conversionResultSet).run(player)
    } yield ()
  }
}
