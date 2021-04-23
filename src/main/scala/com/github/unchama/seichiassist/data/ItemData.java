package com.github.unchama.seichiassist.data;

import com.github.unchama.seichiassist.util.itemcodec.ElsaCodec$;
import com.github.unchama.seichiassist.util.itemcodec.GachaRingoCodec$;
import com.github.unchama.seichiassist.util.itemcodec.VotePickaxeCodec$;
import com.github.unchama.seichiassist.util.itemcodec.VotingGiftCodec$;
import org.bukkit.inventory.ItemStack;
import scala.runtime.BoxedUnit;

/**
 * Created by karayuu on 2018/04/20
 */
@Deprecated
public class ItemData {
    public static ItemStack getSuperPickaxe(int amount) {
        final ItemStack pickaxe = VotePickaxeCodec$.MODULE$.create(BoxedUnit.UNIT);
        pickaxe.setAmount(amount);
        return pickaxe;
    }

    @Deprecated
    public static ItemStack getGachaApple(int amount) {
        final ItemStack gachaimo = GachaRingoCodec$.MODULE$.create(BoxedUnit.UNIT);
        gachaimo.setAmount(amount);
        return gachaimo;
    }

    public static ItemStack getElsa(int amount) {
        final ItemStack elsa = ElsaCodec$.MODULE$.create(BoxedUnit.UNIT);
        elsa.setAmount(amount);
        return elsa;
    }

    @Deprecated
    public static ItemStack getVotingGift(int amount) {
        final ItemStack gift = VotingGiftCodec$.MODULE$.create(BoxedUnit.UNIT);
        gift.setAmount(amount);
        return gift;
    }
}
