package com.github.unchama.seichiassist.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

@SuppressWarnings("UtilityClassCanBeEnum")
public final class ItemMetaFactory {
    private static final ItemFactory FACTORY = Bukkit.getItemFactory();
    public static final ValueHolder<SkullMeta> SKULL = new ValueHolder<>((SkullMeta) FACTORY.getItemMeta(Material.SKULL_ITEM), SkullMeta::clone);
    public static final ValueHolder<ItemMeta> GOLDEN_APPLE = new ValueHolder<>(FACTORY.getItemMeta(Material.GOLDEN_APPLE), ItemMeta::clone);
    private ItemMetaFactory() {

    }
}
