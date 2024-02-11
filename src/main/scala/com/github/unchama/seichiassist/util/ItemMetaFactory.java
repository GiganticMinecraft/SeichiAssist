package com.github.unchama.seichiassist.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

@SuppressWarnings("UtilityClassCanBeEnum")
public final class ItemMetaFactory {
    private static final ItemFactory FACTORY = Bukkit.getItemFactory();
    public static final ValueHolder<SkullMeta> SKULL = new ValueHolder<>((SkullMeta) FACTORY.getItemMeta(Material.PLAYER_HEAD), SkullMeta::clone);
}
