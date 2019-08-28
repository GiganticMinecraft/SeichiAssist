package com.github.unchama.minestack

import com.github.unchama.util.collection.MutableInvertibleMap
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.min

data class MineStackId(val id: String)

class MineStack {
  private val playerMineStackMapping: MutableMap<Player, PlayerMineStack> = HashMap()
  private val registry: MineStackRegistry = MineStackRegistry()

  private val Player.mineStack
    get() = playerMineStackMapping[this]!!

  fun initializeFor(player: Player, countMap: Map<MineStackId, Long>) {
    this.playerMineStackMapping[player] = PlayerMineStack(countMap.toMutableMap())
    this.registry.synchronizePlayerRegistry(player)
  }

  fun removeRecord(player: Player) {
    this.playerMineStackMapping.remove(player)
    this.registry.desynchronizePlayerRegistry(player)
  }

  fun Player.amountStackedInMineStack(itemStack: ItemStack): Long {
    val correspondingId = with(registry[player]) { mineStackIdFor(itemStack) } ?: return 0

    return mineStack.getStackedAmountOf(correspondingId)
  }

  fun Player.attemptToDepositToMineStack(itemStack: ItemStack): Boolean {
    val correspondingId = with(registry[player]) { mineStackIdFor(itemStack) }

    return if (correspondingId != null) {
      mineStack.addStackedAmountOf(correspondingId, itemStack.amount.toLong())
      true
    } else {
      false
    }
  }

  fun Player.withdrawOneStackFromMineStack(id: MineStackId): ItemStack? {
    val targetItemStack = with(registry[player]) { itemStackFor(id) } ?: return null
    val withdrawAmount =
        min(mineStack.getStackedAmountOf(id), targetItemStack.maxStackSize.toLong())

    if (withdrawAmount == 0L) return null

    mineStack.subtractStackedAmountOf(id, withdrawAmount)

    return targetItemStack.clone().apply { amount = withdrawAmount.toInt() }
  }

  private class MineStackObject(val item: ItemStack) {
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as MineStackObject

      return item.isSimilar(other.item)
    }

    override fun hashCode(): Int = item.type.hashCode()
  }

  private class MineStackRegistry {
    private val generators: MutableMap<MineStackId, (Player) -> ItemStack> = HashMap()
    private val registry: MutableMap<Player, PlayerRegistry> = HashMap()

    operator fun get(player: Player) = registry[player]!!

    fun registerGenerator(id: MineStackId, generator: (Player) -> ItemStack) {
      generators[id] = generator

      registry.forEach { (player, playerRegistry) ->
        playerRegistry.registerObject(id, MineStackObject(generator(player)))
      }
    }

    fun synchronizePlayerRegistry(player: Player) {
      val registryMap = generators
          .map { (id, generator) -> id to MineStackObject(generator(player)) }
          .toMap()

      registry[player] = PlayerRegistry(registryMap)
    }

    fun desynchronizePlayerRegistry(player: Player) {
      registry.remove(player)
    }

    class PlayerRegistry(preset: Map<MineStackId, MineStackObject>) {
      private val mapping: MutableInvertibleMap<MineStackId, MineStackObject> = MutableInvertibleMap(HashMap(preset))

      fun registerObject(id: MineStackId, mineStackObject: MineStackObject) {
        mapping[id] = mineStackObject
      }

      fun Player.mineStackIdFor(itemStack: ItemStack): MineStackId? =
          mapping.getInverse()[MineStackObject(itemStack)]

      fun Player.itemStackFor(mineStackId: MineStackId): ItemStack? =
          mapping[mineStackId]?.item
    }
  }

  private class PlayerMineStack(private val countMap: MutableMap<MineStackId, Long>) {
    fun getStackedAmountOf(id: MineStackId): Long = countMap[id] ?: 0

    fun addStackedAmountOf(id: MineStackId, byAmount: Long) {
      countMap[id] = getStackedAmountOf(id) + byAmount
    }

    fun subtractStackedAmountOf(id: MineStackId, byAmount: Long) = addStackedAmountOf(id, -byAmount)
  }
}
