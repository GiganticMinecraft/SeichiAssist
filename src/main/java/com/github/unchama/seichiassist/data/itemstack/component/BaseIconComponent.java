package com.github.unchama.seichiassist.data.itemstack.component;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Iconの要素をまとめたクラス.
 * <p>
 * Created by karayuu on 2019/04/09
 */
public class BaseIconComponent {
    @NotNull
    private Material material;
    @NotNull
    private String title;
    @NotNull
    private List<String> lore;
    private Boolean isEnchanted = false;
    private int number = 1;
    private short durability;

    public BaseIconComponent(@NotNull Material material) {
        this(material, (short) 0);
    }

    public BaseIconComponent(@NotNull Material material, short durability) {
        this.material = material;
        this.title = Bukkit.getItemFactory().getItemMeta(material).getDisplayName();
        this.lore = Collections.emptyList();
        this.durability = durability;
    }

    @NotNull
    public Material getMaterial() {
        return material;
    }

    public void setTitle(@NotNull String title) {
        this.title = title;
    }

    @NotNull
    public List<String> getLore() {
        return lore;
    }

    /**
     * @param lore {@link List} として渡された要素に {@code null} が含まれていた場合,無視されます.
     */
    public void setLore(@NotNull List<String> lore) {
        this.lore = lore;
    }

    public void setEnchanted(Boolean enchanted) {
        isEnchanted = enchanted;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public ItemStack getItemStack() {
        return new ItemStack(material, number, durability);
    }

    /**
     * 基本的な {@link ItemMeta} を生成します.
     * 必要な {@link ItemMeta} は各クラスにて実装してください.
     *
     * @return ItemMeta
     */
    public ItemMeta getItemMeta() {
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(material);
        meta.setDisplayName(title);
        List<String> collectLore = lore.stream()
                                       .filter(Objects::nonNull)
                                       .collect(Collectors.toList());
        meta.setLore(collectLore);

        if (isEnchanted) {
            meta.addEnchant(Enchantment.DIG_SPEED, 100, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        return meta;
    }
}
