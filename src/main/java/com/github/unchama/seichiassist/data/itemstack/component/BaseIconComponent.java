package com.github.unchama.seichiassist.data.itemstack.component;

import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.unchama.seichiassist.text.Text.toStringList;

/**
 * Iconの要素をまとめたクラス.
 * <p>
 * Created by karayuu on 2019/04/09
 */
public class BaseIconComponent {
    @Nonnull
    private Material material;
    @Nonnull
    private Function<PlayerData, Text> title;
    @Nonnull
    private Function<PlayerData, List<Text>> lore;
    private Boolean isEnchanted = false;
    private int number = 1;
    private short durability = 0;

    public BaseIconComponent(@Nonnull Material material) {
        this(material, (short) 0);
    }

    public BaseIconComponent(@Nonnull Material material, short durability) {
        this.material = material;
        this.title = playerData -> Text.of(Bukkit.getItemFactory().getItemMeta(material).getDisplayName());
        this.lore = playerData -> Collections.emptyList();
        this.durability = durability;
    }

    @Nonnull
    public Material getMaterial() {
        return material;
    }

    public void setTitle(@Nonnull Function<PlayerData, Text> title) {
        this.title = title;
    }

    public void setTitle(@Nonnull Text title) {
        setTitle(playerData -> title);
    }

    @Nonnull
    public Function<PlayerData, List<Text>> getLore() {
        return lore;
    }

    /**
     * @param lore {@link List} として渡された要素に {@code null} が含まれていた場合,無視されます.
     */
    public void setLore(@Nonnull Function<PlayerData, List<Text>> lore) {
        this.lore = lore;
    }

    /**
     * @param lore {@link List} として渡された要素に {@code null} が含まれていた場合,無視されます.
     */
    public void setLore(@Nonnull List<Text> lore) {
        setLore(playerData -> lore);
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
     * 与えられたPlayerDataを用いて基本的なMetaを生成します.
     * 必要なMetaは各クラスにて実装してください.
     *
     * @param playerData lore, title生成に必要なPlayerData ({@code null} は許容されません.)
     * @return ItemMeta
     */
    public ItemMeta getItemMeta(@Nonnull PlayerData playerData) {
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(material);
        meta.setDisplayName(title.apply(playerData).stringValue());
        List<Text> collectLore = lore.apply(playerData).stream().filter(Objects::nonNull).collect(Collectors.toList());
        meta.setLore(toStringList(collectLore));

        if (isEnchanted) {
            meta.addEnchant(Enchantment.DIG_SPEED, 100, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        return meta;
    }
}
