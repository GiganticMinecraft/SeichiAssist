package com.github.unchama.seichiassist.util

import org.bukkit.Material
import org.bukkit.inventory.{ItemFlag, ItemStack}

object MenuIcon {

  import scala.util.chaining._
  import scala.jdk.CollectionConverters._

  /**
   * GUIメニューアイコン作成用
   *
   * @author
   *   karayuu
   * @param material
   *   メニューアイコンMaterial
   * @param amount
   *   メニューアイコンのアイテム個数
   * @param displayName
   *   メニューアイコンのDisplayName
   * @param lore
   *   メニューアイコンのLore
   * @param isHideFlags
   *   攻撃値・ダメージ値を隠すかどうか(true: 隠す / false: 隠さない)
   * @return
   *   ItemStack型のメニューアイコン
   */
  def getMenuIcon(
    material: Material,
    amount: Int,
    displayName: String,
    lore: List[String],
    isHideFlags: Boolean
  ): ItemStack = {
    new ItemStack(material, amount).tap { itemStack =>
      import itemStack._
      setItemMeta {
        getItemMeta.tap { meta =>
          import meta._
          setDisplayName(displayName)
          setLore(lore.asJava)
          if (isHideFlags) {
            addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
          }
        }
      }
    }
  }

  /**
   * GUIメニューアイコン作成用
   *
   * @author
   *   karayuu
   * @param material
   *   メニューアイコンMaterial, not `null`
   * @param amount
   *   メニューアイコンのアイテム個数
   * @param durabity
   *   メニューアイコンのダメージ値
   * @param displayName
   *   メニューアイコンのDisplayName, not `null`
   * @param lore
   *   メニューアイコンのLore, not `null`
   * @param isHideFlags
   *   攻撃値・ダメージ値を隠すかどうか(true: 隠す / false: 隠さない)
   * @throws IllegalArgumentException
   *   Material,DisplayName, Loreのいずれかが `null` の時
   * @return
   *   ItemStack型のメニューアイコン
   */
  def getMenuIcon(
    material: Material,
    amount: Int,
    durabity: Int,
    displayName: String,
    lore: List[String],
    isHideFlags: Boolean
  ): ItemStack = {
    new ItemStack(material, amount, durabity.toShort).tap { itemStack =>
      import itemStack._
      setItemMeta {
        getItemMeta.tap { meta =>
          import meta._
          setDisplayName(displayName)
          setLore(lore.asJava)

          if (isHideFlags) addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        }
      }
    }
  }
}
