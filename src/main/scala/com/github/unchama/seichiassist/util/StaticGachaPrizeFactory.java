package com.github.unchama.seichiassist.util;

import com.github.unchama.seichiassist.util.itemcodec.DeathGodSickleCodec$;
import com.github.unchama.seichiassist.util.itemcodec.GachaRingoCodec$;
import com.github.unchama.seichiassist.util.itemcodec.ShiinaRingoConvertedFromGachaPrizeCodec;
import com.github.unchama.seichiassist.util.itemcodec.ShiinaRingoConvertedFromGachaPrizeCodec$;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import scala.runtime.BoxedUnit;

import java.util.List;

@Deprecated
public final class StaticGachaPrizeFactory {
    /**
     * @return ガチャりんごを表すItemStackを返す。
     */
    @Deprecated
    public static @NotNull ItemStack getGachaRingo() {
        return GachaRingoCodec$.MODULE$.create(BoxedUnit.UNIT);
    }

    //がちゃりんごの名前を取得
    @Deprecated
    public static String getGachaRingoName() {
        return getGachaRingo().getItemMeta().getDisplayName();
    }

    //がちゃりんごの説明を取得
    @Deprecated
    public static List<String> getGachaRingoLore() {
        return getGachaRingo().getItemMeta().getLore();
    }

    /**
     * @return 椎名林檎を表すItemStackを返す。
     */
    @Deprecated
    public static @NotNull ItemStack getMaxRingo(String name) {
        return ShiinaRingoConvertedFromGachaPrizeCodec$.MODULE$.create(new ShiinaRingoConvertedFromGachaPrizeCodec.Property(name));
    }

    /**
     * @return 死神の鎌を表すItemStackを返す。
     */
    @Deprecated
    public static @NotNull ItemStack getMineHeadItem() {
        return DeathGodSickleCodec$.MODULE$.create(BoxedUnit.UNIT);
    }
}
