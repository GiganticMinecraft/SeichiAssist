package com.github.unchama.seichiassist.data.itemstack.component;

import com.github.unchama.seichiassist.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Iconの要素をまとめたクラス.
 * <p>
 * Created by karayuu on 2019/04/09
 */
public class BaseIconComponent {
    @Nonnull
    private Material material;
    @Nonnull
    private Function<PlayerData, String> title;
    @Nonnull
    private Function<PlayerData, List<String>> lore;
    private Boolean isEnchanted = false;
    private int number = 1;

    public BaseIconComponent(@Nonnull Material material) {
        requireNonNull(material);
        this.material = material;
        this.title = playerData -> Bukkit.getItemFactory().getItemMeta(material).getDisplayName();
        this.lore = playerData -> Collections.emptyList();
    }

    @Nonnull
    public Material getMaterial() {
        return material;
    }

    public void setTitle(@Nonnull Function<PlayerData, String> title) {
        requireNonNull(title);
        this.title = title;
    }

    public void setTitle(@Nonnull String title) {
        requireNonNull(title);
        setTitle(playerData -> title);
    }

    @Nonnull
    public Function<PlayerData, List<String>> getLore() {
        return lore;
    }

    public void setLore(@Nonnull Function<PlayerData, List<String>> lore) {
        requireNonNull(lore);
        this.lore = lore;
    }

    public void setLore(@Nonnull List<String> lore) {
        requireNonNull(lore);
        setLore(playerData -> lore);
    }

    public void setEnchanted(Boolean enchanted) {
        isEnchanted = enchanted;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public ItemStack getItemStack() {
        return new ItemStack(material, number);
    }

    /**
     * 与えられたPlayerDataを用いて基本的なMetaを生成します.
     * 必要なMetaは各クラスにて実装してください.
     *
     * @param playerData lore, title生成に必要なPlayerData ({@code null} は許容されません.)
     * @return ItemMeta
     */
    public ItemMeta getItemMeta(@Nonnull PlayerData playerData) {
        requireNonNull(playerData);
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(material);
        meta.setDisplayName(title.apply(playerData));
        meta.setLore(lore.apply(playerData));

        if (isEnchanted) {
            meta.addEnchant(Enchantment.DIG_SPEED, 100, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        return meta;
    }
}
