package com.github.unchama.seichiassist.task;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.MaterialSets;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.BreakArea;
import com.github.unchama.seichiassist.data.Mana;
import com.github.unchama.seichiassist.data.player.PlayerData;
import com.github.unchama.seichiassist.effect.XYZTuple;
import com.github.unchama.seichiassist.util.BreakUtil;
import com.github.unchama.seichiassist.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import scala.Some;
import scala.collection.mutable.HashMap;
import scala.collection.mutable.HashSet;

import java.util.UUID;

public class AssaultTask extends BukkitRunnable {
    SeichiAssist plugin = SeichiAssist.instance();
    HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap();
    Player player;
    UUID uuid;
    PlayerData playerdata;
    int level;
    int type;
    int playerlocy;
    Mana mana;
    PlayerInventory inventory;
    ItemStack tool;
    //一回の破壊の範囲
    XYZTuple breaklength;
    //１回の全て破壊したときのブロック数
    int ifallbreaknum;
    //破壊エリアデータ
    BreakArea assaultarea;
    //放置判定用位置データ
    Location lastloc;
    //放置判定用int
    int idletime;

    boolean errorflag = false;

    boolean waterflag = false, lavaflag = false, fluidflag = false, breakflag = false, condensflag = false;

	/*
	List<Material> material2list = new ArrayList<Material>(Arrays.asList(
	Material.STAINED_GLASS_PANE,Material.GRASS_PATH,Material.SOIL
	,Material.WOOD_STAIRS,Material.COBBLESTONE_STAIRS));
	*/

    public AssaultTask(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
        this.playerdata = playermap.apply(uuid);
        lastloc = player.getLocation();
        idletime = 0;


        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[アサルトタスクRunnable処理]でエラー発生");
            plugin.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            errorflag = true;
            return;
        }

        this.level = playerdata.activeskilldata().assaultnum;
        this.type = playerdata.activeskilldata().assaulttype;
        this.assaultarea = playerdata.activeskilldata().assaultarea;
        this.mana = playerdata.activeskilldata().mana;

        //もしサバイバルでなければ処理を終了
        if (player.getGameMode() != GameMode.SURVIVAL) {// || player.isFlying()){
            player.sendMessage(ChatColor.GREEN + "ゲームモードをサバイバルに変更してください。");
            errorflag = true;
            return;
        }

        //整地ワールドではない時スキルを発動しない。
        if (!Util.isSkillEnable(player)) {
            player.sendMessage(ChatColor.GREEN + "スキルは整地ワールドでのみ使用可能です。");
            errorflag = true;
            return;
        }

        //プレイヤーインベントリを取得
        inventory = player.getInventory();
        //メインハンドとオフハンドを取得
        ItemStack mainhanditem = inventory.getItemInMainHand();
        ItemStack offhanditem = inventory.getItemInOffHand();
        //実際に使用するツールを格納する
        tool = null;
        //メインハンドにツールがあるか
        boolean mainhandtoolflag = MaterialSets.breakMaterials().contains(mainhanditem.getType());
        //オフハンドにツールがあるか
        boolean offhandtoolflag = MaterialSets.breakMaterials().contains(offhanditem.getType());

        //場合分け
        if (offhandtoolflag) {
            //サブハンドの時
            tool = offhanditem;
        } else if (mainhandtoolflag) {
            //メインハンドの時
            player.sendMessage(ChatColor.GREEN + "使うツールをオフハンドにセット(fキー)してください");
            errorflag = true;
            return;
        } else {
            //どちらにももっていない時処理を終了
            player.sendMessage(ChatColor.GREEN + "使うツールをオフハンドにセット(fキー)してください");
            errorflag = true;
            return;
        }
        //耐久値がマイナスかつ耐久無限ツールでない時処理を終了
        if (tool.getDurability() > tool.getType().getMaxDurability() && !tool.getItemMeta().spigot().isUnbreakable()) {
            player.sendMessage(ChatColor.GREEN + "不正な耐久値です。");
            errorflag = true;
            return;
        }
        this.breaklength = assaultarea.getBreakLength();
        //壊すフラグを指定
        if (playerdata.activeskilldata().assaulttype == ActiveSkill.WATERCONDENSE.gettypenum()) {
            waterflag = true;
        } else if (playerdata.activeskilldata().assaulttype == ActiveSkill.LAVACONDENSE.gettypenum()) {
            lavaflag = true;
        } else if (playerdata.activeskilldata().assaulttype == ActiveSkill.FLUIDCONDENSE.gettypenum()) {
            fluidflag = true;
        } else if (playerdata.activeskilldata().assaulttype == ActiveSkill.ARMOR.gettypenum()) {
            breakflag = true;
        }
        ifallbreaknum = (breaklength.x() * breaklength.y() * breaklength.z());


    }

    private void setCancel() {
        playerdata.activeskilldata().assaultflag = false;
        playerdata.activeskilldata().mineflagnum = 0;
        this.cancel();
    }

    @Override
    public void run() {

        if (isCanceled()) {
            setCancel();
            return;
        }
        //もしサバイバルでなければ処理を終了
        //もしフライ中なら終了
        if (player.getGameMode() != GameMode.SURVIVAL) {// || player.isFlying()){
            player.sendMessage(ChatColor.GREEN + "ゲームモードをサバイバルに変更してください。");
            setCancel();
            return;
        }

        //整地ワールドではない時スキルを発動しない。
        if (!Util.isSkillEnable(player)) {
            player.sendMessage(ChatColor.GREEN + "スキルは整地ワールドでのみ使用可能です。");
            setCancel();
            return;
        }

        //放置判定、動いてなかったら処理終了
        if (
                ((lastloc.getBlockX() - 10) < player.getLocation().getBlockX())
                        && ((lastloc.getBlockX() + 10) > player.getLocation().getBlockX())
                        && ((lastloc.getBlockY() - 10) < player.getLocation().getBlockY())
                        && ((lastloc.getBlockY() + 10) > player.getLocation().getBlockY())
                        && ((lastloc.getBlockZ() - 10) < player.getLocation().getBlockZ())
                        && ((lastloc.getBlockZ() + 10) > player.getLocation().getBlockZ())
        ) {
            if (SeichiAssist.DEBUG()) {
                player.sendMessage(ChatColor.RED + "放置を検出");
            }
            idletime++;
            if (idletime > 20) {
                player.sendMessage(ChatColor.YELLOW + "アサルトスキルがOFFになりました");
                setCancel();
                return;
            }
        } else {
            //動いてたら次回判定用に場所更新しとく
            lastloc = player.getLocation();
            idletime = 0;
        }

        HashSet<Block> blocks = new HashSet<>();
        HashSet<Block> lavas = new HashSet<>();
        HashSet<Block> waters = new HashSet<>();

        //プレイヤーの足のy座標を取得
        int playerlocy = player.getLocation().getBlockY() - 1;
        Block block = player.getLocation().getBlock();

        ItemStack offhanditem = inventory.getItemInOffHand();
        //最初に登録したツールと今のツールが違う場合
        if (!tool.equals(offhanditem)) {
            if (SeichiAssist.DEBUG()) {
                player.sendMessage(ChatColor.RED + "ツールの変更を検知しました");
            }
            setCancel();
            return;
        }


        //壊されるブロックの宣言
        Block breakblock;
        //壊されるエリアの設定
        //現在のプレイヤーの向いている方向
        String dir = BreakUtil.getCardinalDirection(player);
        //もし前回とプレイヤーの向いている方向が違ったら範囲を取り直す
        if (!dir.equals(assaultarea.getDir())) {
            assaultarea.setDir(dir);
            assaultarea.makeArea();
        }
        XYZTuple start = assaultarea.getStartList().get(0);
        XYZTuple end = assaultarea.getEndList().get(0);

        for (int y = end.y(); y >= start.y(); y--) { //上から
            for (int x = start.x(); x <= end.x(); x++) {
                for (int z = start.z(); z <= end.z(); z++) {
                    breakblock = block.getRelative(x, y, z);
                    boolean lava_materialflag = breakblock.getType() == Material.STATIONARY_LAVA
                            || breakblock.getType() == Material.LAVA;
                    boolean water_materialflag = breakblock.getType() == Material.STATIONARY_WATER
                            || breakblock.getType() == Material.WATER;
                    if (MaterialSets.materials().contains(breakblock.getType())
                            || lava_materialflag || water_materialflag
                    ) {
                        if (playerlocy < breakblock.getLocation().getBlockY() || player.isSneaking() || breakblock.equals(block) || !breakflag) {
                            if (BreakUtil.canBreak(player, Some.apply(breakblock))) {
                                if (lava_materialflag) {
                                    lavas.add(breakblock);
                                } else if (water_materialflag) {
                                    waters.add(breakblock);
                                } else {
                                    blocks.add(breakblock);
                                    SeichiAssist.allblocklist().$plus$eq(breakblock);
                                }
                            }
                        }
                    }
                }
            }
        }
        //重力値計算
        int gravity = BreakUtil.getGravity(player, block, true);

        // 実際に破壊するブロック数の計算分岐
        int breaksum = 0;
        if (waterflag) {
            breaksum = waters.size();
        } else if (lavaflag) {
            breaksum = lavas.size();
        } else if (fluidflag) {
            breaksum = waters.size() + lavas.size();
        } else if (breakflag) {
            breaksum = waters.size() + lavas.size() + blocks.size();
        }

        //減る経験値計算
        //実際に破壊するブロック数 * 全てのブロックを破壊したときの消費経験値÷すべての破壊するブロック数 * 重力

        double useMana = (double) breaksum * (gravity + 1)
                * ActiveSkill.getActiveSkillUseExp(playerdata.activeskilldata().assaulttype, playerdata.activeskilldata().assaultnum)
                / (ifallbreaknum);


        //減る耐久値の計算
        short durability = (short) (tool.getDurability() + BreakUtil.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY), breaksum));


        //重力値の判定
        if (gravity > 15) {
            player.sendMessage(ChatColor.RED + "スキルを使用するには上から掘ってください。");
            SeichiAssist.allblocklist().$minus$minus$eq(blocks);
            setCancel();
            return;
        }

        //実際に経験値を減らせるか判定
        if (!mana.has(useMana)) {
            //デバッグ用
            if (SeichiAssist.DEBUG()) {
                player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なマナが足りません");
            }
            SeichiAssist.allblocklist().$minus$minus$eq(blocks);
            setCancel();
            return;
        }


        //実際に耐久値を減らせるか判定
        if (tool.getType().getMaxDurability() <= durability && !tool.getItemMeta().spigot().isUnbreakable()) {
            //デバッグ用
            if (SeichiAssist.DEBUG()) {
                player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なツールの耐久値が足りません");
            }
            SeichiAssist.allblocklist().$minus$minus$eq(blocks);
            setCancel();
            return;
        }


        //経験値を減らす
        mana.decrease(useMana, player, playerdata.level());

        //耐久値を減らす
        if (!tool.getItemMeta().spigot().isUnbreakable()) {
            tool.setDurability(durability);
        }

        //以降破壊する処理

        //破壊する処理分岐
        if (waterflag) {
            waters.foreach(value -> {
                value.setType(Material.PACKED_ICE);
                BreakUtil.logRemove(player, value);
                return 0;
            });
        } else if (lavaflag) {
            lavas.foreach(value -> {
                value.setType(Material.MAGMA);
                BreakUtil.logRemove(player, value);
                return 0;
            });
        } else if (fluidflag) {
            waters.foreach(item -> {
                item.setType(Material.PACKED_ICE);
                BreakUtil.logRemove(player, item);
                return 0;
            });
            lavas.foreach(value -> {
                value.setType(Material.MAGMA);
                BreakUtil.logRemove(player, value);
                return 0;
            });
        } else if (breakflag) {
            waters.foreach(item -> {
                item.setType(Material.AIR);
                return 0;
            });
            lavas.foreach(value -> {
                value.setType(Material.AIR);
                return 0;
            });
            blocks.foreach(b -> {
                BreakUtil.breakBlock(player, b, player.getLocation(), tool, false);
                SeichiAssist.allblocklist().$minus$eq(b);
                return 0;
            });
        }
        SeichiAssist.allblocklist().$minus$minus$eq(blocks);
    }

    private boolean isCanceled() {
        return playerdata.activeskilldata().mineflagnum == 0 || errorflag || playerdata.activeskilldata().assaulttype == 0;
    }
}
