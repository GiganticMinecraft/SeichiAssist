package com.github.unchama.seichiassist.data.itemstack.component;

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
import java.util.stream.Collectors;

/**
 * Iconの要素をまとめたクラス.
 * <p>
 * Created by karayuu on 2019/04/09
 */
public class BaseIconComponent {
    @Nonnull
    private Material material;
    @Nonnull
    private Text title;
    @Nonnull
    private List<Text> lore;
    private Boolean isEnchanted = false;
    private int number = 1;
    private short durability = 0;

    public BaseIconComponent(@Nonnull Material material) {
        this(material, (short) 0);
    }

    public BaseIconComponent(@Nonnull Material material, short durability) {
        this.material = material;
        this.title = Text.of(Bukkit.getItemFactory().getItemMeta(material).getDisplayName());
        this.lore = Collections.emptyList();
        this.durability = durability;
    }

    @Nonnull
    public Material getMaterial() {
        return material;
    }

    public void setTitle(@Nonnull Text title) {
        this.title = title;
    }

    @Nonnull
    public List<Text> getLore() {
        return lore;
    }

    /**
     * @param lore {@link List} として渡された要素に {@code null} が含まれていた場合,無視されます.
     */
    public void setLore(@Nonnull List<Text> lore) {
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
        meta.setDisplayName(title.stringValue());
        List<String> collectLore = lore.stream()
                                       .filter(Objects::nonNull)
                                       .map(Text::stringValue)
                                       .collect(Collectors.toList());
        meta.setLore(collectLore);

        if (isEnchanted) {
            meta.addEnchant(Enchantment.DIG_SPEED, 100, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        return meta;
    }
}
