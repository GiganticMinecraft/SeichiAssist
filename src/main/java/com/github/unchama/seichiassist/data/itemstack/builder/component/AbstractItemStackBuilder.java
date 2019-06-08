package com.github.unchama.seichiassist.data.itemstack.builder.component;

import com.github.unchama.seichiassist.data.itemstack.component.BaseIconComponent;
import org.bukkit.Material;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * ItemStackBuilderのベースとなる抽象クラスです.
 * 各Builderで共通する処理をまとめています.
 *
 * @author karayuu
 */
@SuppressWarnings("unchecked")
public abstract class AbstractItemStackBuilder<T extends AbstractItemStackBuilder<T>> implements ItemStackBuilder {
    @Nonnull
    protected final BaseIconComponent component;

    protected AbstractItemStackBuilder(@Nonnull Material material) {
        this.component = new BaseIconComponent(material);
    }

    protected AbstractItemStackBuilder(@Nonnull Material material, short durability) {
        this.component = new BaseIconComponent(material, durability);
    }

    @Override
    @Nonnull
    public T title(@Nonnull String title) {
        this.component.setTitle(title);
        return (T) this;
    }

    @Override
    @Nonnull
    public T lore(@Nonnull List<String> lore) {
        this.component.setLore(lore);
        return (T) this;
    }

    @Override
    @Nonnull
    public T enchanted() {
        this.component.setEnchanted(true);
        return (T) this;
    }

    @Override
    @Nonnull
    public T number(int number) {
        this.component.setNumber(number);
        return (T) this;
    }
}
