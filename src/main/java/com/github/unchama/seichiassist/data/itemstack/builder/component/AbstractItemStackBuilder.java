package com.github.unchama.seichiassist.data.itemstack.builder.component;

import com.github.unchama.seichiassist.data.itemstack.component.BaseIconComponent;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * ItemStackBuilderのベースとなる抽象クラスです.
 * 各Builderで共通する処理をまとめています.
 *
 * @author karayuu
 */
@SuppressWarnings("unchecked")
public abstract class AbstractItemStackBuilder<T extends AbstractItemStackBuilder<T>> implements ItemStackBuilder {
    @NotNull
    protected final BaseIconComponent component;

    protected AbstractItemStackBuilder(@NotNull Material material) {
        this.component = new BaseIconComponent(material);
    }

    protected AbstractItemStackBuilder(@NotNull Material material, short durability) {
        this.component = new BaseIconComponent(material, durability);
    }

    @Override
    @NotNull
    public T title(@NotNull String title) {
        this.component.setTitle(title);
        return (T) this;
    }

    @Override
    @NotNull
    public T lore(@NotNull List<String> lore) {
        this.component.setLore(lore);
        return (T) this;
    }

    @Override
    @NotNull
    public T enchanted() {
        this.component.setEnchanted(true);
        return (T) this;
    }

    @Override
    @NotNull
    public T number(int number) {
        this.component.setNumber(number);
        return (T) this;
    }
}
