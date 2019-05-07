package com.github.unchama.seichiassist.data.inventory.itemstack.component;

import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.unchama.seichiassist.text.Text.toStringList;
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
    private Function<PlayerData, Text> title;
    @Nonnull
    private Function<PlayerData, List<Text>> lore;
    @NotNull
    private Predicate<PlayerData> enchantPredicate;
    private int number = 1;
    private short durability;

    public BaseIconComponent(@Nonnull Material material) {
        this(material, (short) 0);
    }

    public BaseIconComponent(@Nonnull Material material, short durability) {
        requireNonNull(material);
        this.material = material;
        this.title = playerData -> Text.of(Bukkit.getItemFactory().getItemMeta(material).getDisplayName());
        this.lore = playerData -> Collections.emptyList();
        this.enchantPredicate = playerData -> false;
        this.durability = durability;
    }

    @Nonnull
    public Material getMaterial() {
        return material;
    }

    public void setTitle(@Nonnull Function<PlayerData, Text> title) {
        requireNonNull(title);
        this.title = title;
    }

    public void setTitle(@Nonnull Text title) {
        requireNonNull(title);
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
        requireNonNull(lore);
        this.lore = lore;
    }

    /**
     * @param lore {@link List} として渡された要素に {@code null} が含まれていた場合,無視されます.
     */
    public void setLore(@Nonnull List<Text> lore) {
        requireNonNull(lore);
        setLore(playerData -> lore);
    }

    public void setEnchantPredicate(Predicate<PlayerData> predicate) {
        this.enchantPredicate = predicate;
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
        requireNonNull(playerData);
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(material);
        meta.setDisplayName(title.apply(playerData).stringValue());
        List<Text> collectLore = lore.apply(playerData).stream().filter(Objects::nonNull).collect(Collectors.toList());
        meta.setLore(toStringList(collectLore));

        if (enchantPredicate.test(playerData)) {
            meta.addEnchant(Enchantment.DIG_SPEED, 100, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        return meta;
    }
}
