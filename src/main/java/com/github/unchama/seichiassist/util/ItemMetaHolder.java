package com.github.unchama.seichiassist.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemMetaHolder {
    private ItemMetaHolder() {

    }

    private static final ItemFactory FACTORY = Bukkit.getItemFactory();
    public static final ValueHolder<SkullMeta> SKULL = new ValueHolder<>((SkullMeta) FACTORY.getItemMeta(Material.SKULL_ITEM), SkullMeta::clone);
    public static final ValueHolder<PotionMeta> TIPPED_AWRROW = new ValueHolder<>((PotionMeta) FACTORY.getItemMeta(Material.TIPPED_ARROW), PotionMeta::clone);
    public static final ValueHolder<PotionMeta> SPLASH_POTION = new ValueHolder<>((PotionMeta) FACTORY.getItemMeta(Material.SPLASH_POTION), PotionMeta::clone);
    public static final ValueHolder<ItemMeta> DIAMOND_BLOCK = new ValueHolder<>(FACTORY.getItemMeta(Material.DIAMOND_BLOCK), ItemMeta::clone);
    public static final ValueHolder<ItemMeta> GOLDEN_APPLE = new ValueHolder<>(FACTORY.getItemMeta(Material.GOLDEN_APPLE), ItemMeta::clone);
}
