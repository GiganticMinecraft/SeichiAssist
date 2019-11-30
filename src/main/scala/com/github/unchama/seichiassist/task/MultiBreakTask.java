package com.github.unchama.seichiassist.task;

import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.player.PlayerData;
import com.github.unchama.seichiassist.data.XYZTuple;
import com.github.unchama.seichiassist.util.BreakUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import scala.collection.immutable.IndexedSeq;
import scala.collection.immutable.Set;
import scala.collection.mutable.HashMap;
import scala.jdk.CollectionConverters;

import java.util.List;
import java.util.UUID;

public class MultiBreakTask extends BukkitRunnable {
    private HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap();
    private Player player;
    private Location droploc;
    private ItemStack tool;
    private List<List<Block>> multibreaklist;
    private List<List<Block>> multilavalist;
    private List<XYZTuple> startlist;
    private List<XYZTuple> endlist;
    private int breaknum;
    private PlayerData playerdata;
    private int count;

    public MultiBreakTask(Player player, Block centerblock, ItemStack tool,
                          List<List<Block>> multibreaklist, List<List<Block>> multilavalist,
                          List<XYZTuple> startlist, List<XYZTuple> endlist) {
        this.player = player;
        this.droploc = centerblock.getLocation().add(0.5, 0.5, 0.5);
        this.tool = tool;
        this.multibreaklist = multibreaklist;
        this.multilavalist = multilavalist;
        this.startlist = startlist;
        this.endlist = endlist;
        this.breaknum = multibreaklist.size();
        this.count = 0;
        //this.key = key;
        //playerdataを取得
        playerdata = playermap.apply(player.getUniqueId());
    }

    @Override
    public void run() {
        if (count < breaknum) {
            if (SeichiAssist.DEBUG()) {
                player.sendMessage("" + count);
            }
            //溶岩の破壊する処理
            for (int lavanum = 0; lavanum < multilavalist.get(count).size(); lavanum++) {
                multilavalist.get(count).get(lavanum).setType(Material.AIR);
            }

            final Set<Block> converted = CollectionConverters.ListHasAsScala(multibreaklist.get(count)).asScala().toSet();

            final XYZTuple startPoint = startlist.get(count);
            final XYZTuple endPoint = endlist.get(count);

            //エフェクトが選択されていない時の通常処理
            if (playerdata.activeskilldata().effectnum == 0) {
                BreakUtil.massBreakBlock(player, converted,droploc, tool, false, Material.AIR);
                SeichiAssist.managedBlocks().$minus$minus$eq(converted);
            }

            //通常エフェクトが指定されているときの処理(100以下の番号に割り振る）
            else if (playerdata.activeskilldata().effectnum <= 100) {
                IndexedSeq<ActiveSkillEffect> skilleffect = ActiveSkillEffect.values();
                skilleffect.apply(playerdata.activeskilldata().effectnum - 1).runBreakEffect(player, playerdata.activeskilldata(), tool, converted, startPoint, endPoint, droploc);
            }

            //スペシャルエフェクトが指定されているときの処理(１０１からの番号に割り振る）
            else if (playerdata.activeskilldata().effectnum > 100) {
                IndexedSeq<ActiveSkillPremiumEffect> premiumeffect = ActiveSkillPremiumEffect.values();
                premiumeffect.apply(playerdata.activeskilldata().effectnum - 1 - 100).runBreakEffect(player, tool, converted, startPoint, endPoint, droploc);
            }
            count++;
        } else {
            cancel();
        }

    }

}
