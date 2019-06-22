package com.github.unchama.seichiassist.effect.breaking;

import com.github.unchama.seichiassist.effect.XYZIterator;
import com.github.unchama.seichiassist.effect.XYZTuple;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Ref;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public abstract class AbstractBreakTask2 extends BukkitRunnable {
    protected List<Block> blocks;
    /**
     * {@code loc}を中心にした3x3x3の立方体の範囲のブロックが一つでも{@code blocks}に格納されているか調べる
     * @param loc 中心点
     * @return 含まれているならtrue、含まれていないならfalse
     */
    protected boolean isBreakBlock(final Location loc) {
        final Block b = loc.getBlock();
        if(blocks.contains(b)) return true;
        // needs final rule
        final Ref.BooleanRef ret = new Ref.BooleanRef();
        final Function1<? super XYZTuple, Unit> ll = xyzTuple -> {
            if (blocks.contains(b.getRelative(xyzTuple.getX(), xyzTuple.getY(), xyzTuple.getZ()))) {
                ret.element = true;
            }
            return Unit.INSTANCE;
        };
        new XYZIterator(new XYZTuple(-1, -1, -1), new XYZTuple(1, 1, 1), ll).doAction();
        return ret.element;
    }
}
