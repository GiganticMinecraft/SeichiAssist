package com.github.unchama.seichiassist.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.function.UnaryOperator;

public final class ItemMetaHolder {
    private ItemMetaHolder() {

    }

    // 明示的にインスタンスを一つにするための変数
    private static final UnaryOperator<PotionMeta> A = PotionMeta::clone;
    private static final UnaryOperator<ItemMeta> B = ItemMeta::clone;
    private static final ItemFactory FACTORY = Bukkit.getItemFactory();

    public static final ValueHolder<SkullMeta> SKULL = new ValueHolder<>((SkullMeta) FACTORY.getItemMeta(Material.SKULL_ITEM), SkullMeta::clone);
    public static final ValueHolder<PotionMeta> TIPPED_ARROW = new ValueHolder<>((PotionMeta) FACTORY.getItemMeta(Material.TIPPED_ARROW), A);
    public static final ValueHolder<PotionMeta> SPLASH_POTION = new ValueHolder<>((PotionMeta) FACTORY.getItemMeta(Material.SPLASH_POTION), A);
    public static final ValueHolder<ItemMeta> DIAMOND_BLOCK = new ValueHolder<>(FACTORY.getItemMeta(Material.DIAMOND_BLOCK), B);
    public static final ValueHolder<ItemMeta> BEDROCK = new ValueHolder<>(FACTORY.getItemMeta(Material.BEDROCK), B);
    public static final ValueHolder<ItemMeta> GOLDEN_APPLE = new ValueHolder<>(FACTORY.getItemMeta(Material.GOLDEN_APPLE), B);
}
