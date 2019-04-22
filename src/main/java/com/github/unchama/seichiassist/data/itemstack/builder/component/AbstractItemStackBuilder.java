package com.github.unchama.seichiassist.data.itemstack.builder.component;

import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.itemstack.component.BaseIconComponent;
import org.bukkit.Material;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

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
        requireNonNull(material);
        this.component = new BaseIconComponent(material);
    }

    @Override
    @Nonnull
    public T title(@Nonnull Function<PlayerData, String> title) {
        requireNonNull(title);
        this.component.setTitle(title);
        return (T) this;
    }

    @Nonnull
    public T title(@Nonnull String title) {
        requireNonNull(title);
        this.component.setTitle(playerData -> title);
        return (T) this;
    }

    @Override
    @Nonnull
    public T lore(@Nonnull Function<PlayerData, List<String>> lore) {
        requireNonNull(lore);
        this.component.setLore(lore);
        return (T) this;
    }

    @Nonnull
    public T lore(@Nonnull List<String> lore) {
        requireNonNull(lore);
        this.component.setLore(playerData -> lore);
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
