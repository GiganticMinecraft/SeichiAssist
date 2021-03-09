package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.bukkit

import cats.Monad
import cats.effect.Sync
import com.github.unchama.minecraft.actions.MinecraftServerThreadShift
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.PocketSize
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.actions.InteractInventory
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.{Inventory, ItemStack}

class InteractBukkitInventory[
  F[_] : Sync : MinecraftServerThreadShift
] extends InteractInventory[F, Player, Inventory] {

  import cats.implicits._

  import scala.jdk.CollectionConverters._

  override def open(inventory: Inventory)(player: Player): F[Unit] =
  // インベントリの開閉はパケットが送られるためメインスレッドからのみ許可される(Spigot 1.12.2)
    MinecraftServerThreadShift[F].shift >>
      Sync[F].delay {
        player.openInventory(inventory)
      }.as(())

  override def extendSize(newSize: PocketSize)
                         (inventory: Inventory): F[Inventory] = {
    val shouldCreateNew: F[Boolean] = Sync[F].delay(inventory.getSize < newSize.totalStackCount)

    Monad[F].ifM(shouldCreateNew)(
      for {
        _ <- MinecraftServerThreadShift[F].shift
        _ <- Sync[F].delay {
          inventory.getViewers.forEach(_.closeInventory())
        }
        newInventory <- new CreateBukkitInventory[F].create(newSize)
        _ <- Sync[F].delay {
          // 内容物を移動する。
          inventory.asScala.zipWithIndex.foreach { case (stack, i) =>
            inventory.setItem(i, new ItemStack(Material.AIR, 0))
            newInventory.setItem(i, stack)
          }
        }
      } yield newInventory,
      Monad[F].pure(inventory)
    )
  }

}
