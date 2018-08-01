package com.github.unchama.seichiassist.listener;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.GridTemplate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.RegionMenuData;
import com.github.unchama.seichiassist.util.Util;
import com.github.unchama.seichiassist.util.Util.DirectionType;
import com.github.unchama.seichiassist.util.Util.Direction;
import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.commands.AsyncCommandHelper;
import com.sk89q.worldguard.bukkit.commands.task.RegionAdder;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.util.DomainInputResolver;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * 保護関連メニューのListenerクラス
 * @author karayuu
 * 2017/09/02
 */
public class RegionInventoryListener implements Listener {
    HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
    static WorldGuardPlugin Wg = Util.getWorldGuard();
    static WorldEditPlugin We = Util.getWorldEdit();
    static Config config = SeichiAssist.config;

    /**
     * 木の棒メニューの保護ボタンのみのListener
     * @param event InventoryClickListener
     */
    @EventHandler
    public void onPlayerClickStickMenu(InventoryClickEvent event) {
        //外枠のクリック処理なら終了
        if(event.getClickedInventory() == null){
            return;
        }

        ItemStack itemstackcurrent = event.getCurrentItem();
        InventoryView view = event.getView();
        HumanEntity he = view.getPlayer();
        //インベントリを開けたのがプレイヤーではない時終了
        if(!he.getType().equals(EntityType.PLAYER)){
            return;
        }
        Inventory topinventory = view.getTopInventory();
        //インベントリが存在しない時終了
        if(topinventory == null){
            return;
        }
        //インベントリサイズが36でない時終了
        if(topinventory.getSize() != 4*9){
            return;
        }
        Player player = (Player)he;

        //インベントリ名が以下の時処理
        if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "木の棒メニュー")) {
            event.setCancelled(true);

            //プレイヤーインベントリのクリックの場合終了
            if (event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
                return;
            }
			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

			//土地保護メニュー
            if (itemstackcurrent.getType().equals(Material.DIAMOND_AXE)) {
                player.openInventory(RegionMenuData.getRegionMenuData(player));
                player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.5);
            }
        }
    }

    /**
     * 保護メニューのInventoryClickListener
     * @param event InventoryClickListener
     */
    @EventHandler
    public void onPlayerRegionMenu(InventoryClickEvent event) {
        //外枠のクリック処理なら終了
        if(event.getClickedInventory() == null){
            return;
        }

        ItemStack itemstackcurrent = event.getCurrentItem();
        InventoryView view = event.getView();
        HumanEntity he = view.getPlayer();
        //インベントリを開けたのがプレイヤーではない時終了
        if(!he.getType().equals(EntityType.PLAYER)){
            return;
        }
        Inventory topinventory = view.getTopInventory();
        //インベントリが存在しない時終了
        if(topinventory == null){
            return;
        }
        //インベントリタイプがホッパーでない時終了
        if(!topinventory.getType().equals(InventoryType.HOPPER)){
            return;
        }
        Player player = (Player)he;

        //インベントリ名が以下の時処理
        if(topinventory.getTitle().equals(ChatColor.BLACK + "保護メニュー")) {
            event.setCancelled(true);

            //プレイヤーインベントリのクリックの場合終了
            if (event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
                return;
            }
			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

            UUID uuid = player.getUniqueId();
            PlayerData playerdata = playermap.get(uuid);

            if(itemstackcurrent.getType().equals(Material.WOOD_AXE)){
                // wand召喚
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
                player.chat("//wand");
                player.sendMessage(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "保護のかけ方\n"
                        + ChatColor.RESET + "" +  ChatColor.GREEN + "①召喚された斧を手に持ちます\n"
                        + ChatColor.RESET + "" +  ChatColor.GREEN + "②保護したい領域の一方の角を" + ChatColor.YELLOW + "左" + ChatColor.GREEN + "クリック\n"
                        + ChatColor.RESET + "" +  ChatColor.GREEN + "③もう一方の対角線上の角を" + ChatColor.RED + "右" + ChatColor.GREEN + "クリック\n"
                        + ChatColor.RESET + "" +  ChatColor.GREEN + "④メニューの" + ChatColor.RESET + "" +  ChatColor.YELLOW + "金の斧" + ChatColor.RESET + "" +  ChatColor.GREEN + "をクリック\n"
                        + ChatColor.DARK_GREEN + "解説ページ→" + ChatColor.UNDERLINE + "http://seichi.click/d/WorldGuard"
                );
            }

            else if(itemstackcurrent.getType().equals(Material.GOLD_AXE)){
                // 保護の設定
                player.closeInventory();
                if (config.isGridProtectForce(player)) {
                    player.sendMessage(ChatColor.RED + "このワールドでは保護の申請はグリッド式のみ許可されています。");
                    player.sendMessage(ChatColor.RED + "グリッド式保護作成メニューより申請してください。");
                    return;
                }
                Selection selection = Util.getWorldEdit().getSelection(player);
                if(!player.hasPermission("worldguard.region.claim")){
                    player.sendMessage(ChatColor.RED + "このワールドでは保護を申請できません");
                    return;
                }else if (selection == null) {
                    player.sendMessage(ChatColor.RED + "先に木の斧で範囲を指定してからこのボタンを押してください");
                    player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
                    return;
                }else if(selection.getLength() < 10||selection.getWidth() < 10){
                    player.sendMessage(ChatColor.RED + "指定された範囲が狭すぎます。1辺当たり最低10ブロック以上にしてください");
                    player.sendMessage(ChatColor.DARK_GRAY + "[TIPS]どうしても小さい保護が必要な人は直接コマンド入力で作ろう！");
                    player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, (float)0.5);
                    return;
                }

                player.chat("//expand vert");
                player.chat("/rg claim " + player.getName() + "_" + playerdata.rgnum);
                playerdata.rgnum += 1;
                player.chat("//sel");
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
            }

            else if(itemstackcurrent.getType().equals(Material.STONE_AXE)){
                // 保護リストの表示
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
                player.closeInventory();
                player.sendMessage(ChatColor.GRAY + "--------------------\n"
                        + ChatColor.GRAY + "複数ページの場合… " + ChatColor.RESET + "" +  ChatColor.RED + "" + ChatColor.BOLD + "/rg list -p " + player.getName() + " ページNo\n"
                        + ChatColor.RESET + "" +  ChatColor.GRAY + "先頭に[+]のついた保護はOwner権限\n[-]のついた保護はMember権限を保有しています\n"
                        + ChatColor.DARK_GREEN + "解説ページ→" + ChatColor.UNDERLINE + "https://seichi.click/wiki/WorldGuard");
                player.chat("/rg list -p " + player.getName());
            }

            else if(itemstackcurrent.getType().equals(Material.DIAMOND_AXE)){
                // ReguionGUI表示
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
                player.closeInventory();
                player.chat("/land");
            }

            else if(itemstackcurrent.getType().equals(Material.IRON_AXE)) {
                gridResetFunction(player);
                //グリッド式保護設定画面表示
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
                player.openInventory(RegionMenuData.getGridWorldGuardMenu(player));
            }
        }
    }

    /**
     * グリッド式保護メニューInventoryClickListener
     * @param event InventoryClickEvent
     */
    @EventHandler
    public void onPlayerClickGridMenu(InventoryClickEvent event) {
        //外枠のクリック処理なら終了
        if(event.getClickedInventory() == null){
            return;
        }
        //クリックしたところにアイテムがない場合終了
        if (event.getCurrentItem() == null) {
            return;
        }

        ItemStack itemstackcurrent = event.getCurrentItem();
        InventoryView view = event.getView();
        HumanEntity he = view.getPlayer();
        //インベントリを開けたのがプレイヤーではない時終了
        if(!he.getType().equals(EntityType.PLAYER)){
            return;
        }
        Inventory topinventory = view.getTopInventory();
        //インベントリが存在しない時終了
        if(topinventory == null){
            return;
        }
        //インベントリタイプがディスペンサーでない時終了
        if(!topinventory.getType().equals(InventoryType.DISPENSER)){
            return;
        }

        //インベントリ名が以下の時処理
        if(topinventory.getTitle().equals(ChatColor.LIGHT_PURPLE + "グリッド式保護設定メニュー")) {
            event.setCancelled(true);

            //プレイヤーインベントリのクリックの場合終了
            if (event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
                return;
            }

            /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
            Player player = (Player) view.getPlayer();
            UUID uuid = player.getUniqueId();
            PlayerData playerData = playermap.get(uuid);

            //チャンク延長
            if (itemstackcurrent.getType().equals(Material.STAINED_GLASS_PANE) && itemstackcurrent.getDurability() == 14) {
                gridChangeFunction(player, DirectionType.AHEAD, event);
            } else if (itemstackcurrent.getType().equals(Material.STAINED_GLASS_PANE) && itemstackcurrent.getDurability() == 10) {
                gridChangeFunction(player, DirectionType.LEFT, event);
            } else if (itemstackcurrent.getType().equals(Material.STAINED_GLASS_PANE) && itemstackcurrent.getDurability() == 5) {
                gridChangeFunction(player, DirectionType.RIGHT, event);
            } else if (itemstackcurrent.getType().equals(Material.STAINED_GLASS_PANE) && itemstackcurrent.getDurability() == 13) {
                gridChangeFunction(player, DirectionType.BEHIND, event);
            } else if (itemstackcurrent.getType().equals(Material.WOOL) && itemstackcurrent.getDurability() == 11) {
                player.chat("//expand vert");
                createRegion(player);
                playerData.rgnum += 1;
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
                player.closeInventory();
            } else if (itemstackcurrent.getType().equals(Material.STAINED_GLASS_PANE) && itemstackcurrent.getDurability() == 4) {
                gridResetFunction(player);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_DESTROY, (float) 0.5, 1);
                player.openInventory(RegionMenuData.getGridWorldGuardMenu(player));
            } else if (itemstackcurrent.getType().equals(Material.STAINED_GLASS_PANE) && itemstackcurrent.getDurability() == 0) {
                playerData.toggleUnitPerGrid();
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
                player.openInventory(RegionMenuData.getGridWorldGuardMenu(player));
            } else if (itemstackcurrent.getType().equals(Material.CHEST)) {
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
                player.openInventory(RegionMenuData.getGridTemplateInventory(player));
            }
        }
    }

    private static void gridResetFunction(Player player) {
        PlayerData playerData = SeichiAssist.playermap.get(player.getUniqueId());
        playerData.setUnitAmount(DirectionType.AHEAD, 0);
        playerData.setUnitAmount(DirectionType.BEHIND, 0);
        playerData.setUnitAmount(DirectionType.RIGHT, 0);
        playerData.setUnitAmount(DirectionType.LEFT, 0);
        //始点座標Map(最短)
        Map<String, Double> start = getNearlyUnitStart(player);
        //終点座標Map(最短)
        Map<String, Double> end = getNearlyUnitEnd(player);
        //範囲選択
        wgSelect(new Location(player.getWorld(), start.get("x"), 0, start.get("z")),
                new Location(player.getWorld(), end.get("x"), 256, end.get("z")), player);
        canCreateRegion(player);
    }

    private static void gridChangeFunction(Player player, DirectionType directionType, InventoryClickEvent event) {
        PlayerData playerData = SeichiAssist.playermap.get(player.getUniqueId());
        if (event.isLeftClick()) {
            if (playerData.canGridExtend(directionType)) {
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
                playerData.addUnitAmount(directionType, playerData.getUnitPerClick());
                setWGSelection(player);
                canCreateRegion(player);
                player.openInventory(RegionMenuData.getGridWorldGuardMenu(player));
            }
        } else if (event.isRightClick()) {
            if (playerData.canGridReduce(directionType)) {
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
                playerData.addUnitAmount(directionType, playerData.getUnitPerClick() * (-1));
                setWGSelection(player);
                canCreateRegion(player);
                player.openInventory(RegionMenuData.getGridWorldGuardMenu(player));
            }
        }
    }

    private static void setWGSelection(Player player) {
        PlayerData playerData = SeichiAssist.playermap.get(player.getUniqueId());
        Map<DirectionType, Integer> unitMap = playerData.getUnitMap();
        Direction direction = Util.getPlayerDirection(player);
        World world = player.getWorld();

        int aheadUnitAmount = unitMap.get(DirectionType.AHEAD);
        int leftsideUnitAmount = unitMap.get(DirectionType.LEFT);
        int rightsideUnitAmount = unitMap.get(DirectionType.RIGHT);
        int behindUnitAmount = unitMap.get(DirectionType.BEHIND);

        //0ユニット指定の始点/終点のx,z座標
        double start_x = getNearlyUnitStart(player).get("x");
        double start_z = getNearlyUnitStart(player).get("z");
        double end_x = getNearlyUnitEnd(player).get("x");
        double end_z = getNearlyUnitEnd(player).get("z");

        Location start_loc = null;
        Location end_loc = null;

        switch (direction) {
            case NORTH:
                start_loc = new Location(world, start_x - 15 * leftsideUnitAmount, 0, start_z - 15 * aheadUnitAmount);
                end_loc = new Location(world, end_x + 15 * rightsideUnitAmount, 256, end_z + 15 * behindUnitAmount);
                break;

            case EAST:
                start_loc = new Location(world, start_x - 15 * behindUnitAmount, 0, start_z + 15 * leftsideUnitAmount);
                end_loc = new Location(world, end_x + 15 * aheadUnitAmount , 256, end_z + 15 * rightsideUnitAmount);
                break;

            case SOUTH:
                start_loc = new Location(world, start_x - 15 * rightsideUnitAmount, 0, start_z - 15 * behindUnitAmount);
                end_loc = new Location(world, end_x + 15 * leftsideUnitAmount, 256, end_z + 15 * aheadUnitAmount);
                break;

            case WEST:
                start_loc = new Location(world, start_x - 15 * aheadUnitAmount, 0, start_z - 15 * rightsideUnitAmount);
                end_loc = new Location(world, end_x + 15 * behindUnitAmount, 256, end_z + 15 * leftsideUnitAmount);
                break;

            default:
                //わざと何もしない。
        }
        wgSelect(start_loc, end_loc, player);
    }

    private static void wgSelect(Location loc1, Location loc2, Player player) {
        player.chat("//;");
        player.chat("//pos1 " + (int)loc1.getX() + "," + (int)loc1.getY() + "," + (int)loc1.getZ());
        player.chat("//pos2 " + (int)loc2.getX() + "," + (int)loc2.getY() + "," + (int)loc2.getZ());
    }

    private static void canCreateRegion(Player player) {
        PlayerData playerData = SeichiAssist.playermap.get(player.getUniqueId());
        Selection selection = We.getSelection(player);
        RegionManager manager = Wg.getRegionManager(player.getWorld());
        WorldConfiguration wcfg = Wg.getGlobalStateManager().get(player.getWorld());

        if (selection == null) {
            playerData.setCanCreateRegion(false);
        }

        ProtectedRegion region = new ProtectedCuboidRegion(player.getName() + "_" + playerData.rgnum,
                selection.getNativeMinimumPoint().toBlockVector(), selection.getNativeMaximumPoint().toBlockVector());
        ApplicableRegionSet regions = manager.getApplicableRegions(region);

        if (regions.size() != 0) {
            playerData.setCanCreateRegion(false);
            return;
        }

        int maxRegionCount = wcfg.getMaxRegionCount(player);
        if (maxRegionCount >= 0 && manager.getRegionCountOfPlayer(Wg.wrapPlayer(player)) >= maxRegionCount) {
            playerData.setCanCreateRegion(false);
            return;
        }

        playerData.setCanCreateRegion(true);
    }

    private void createRegion(Player player) {
        PlayerData playerData = SeichiAssist.playermap.get(player.getUniqueId());
        Selection selection = We.getSelection(player);

        ProtectedRegion region = new ProtectedCuboidRegion(player.getName() + "_" + playerData.rgnum,
                selection.getNativeMinimumPoint().toBlockVector(), selection.getNativeMaximumPoint().toBlockVector());
        RegionManager manager = Wg.getRegionManager(player.getWorld());

        RegionAdder task = new RegionAdder(Wg, manager, region);
        task.setLocatorPolicy(DomainInputResolver.UserLocatorPolicy.UUID_ONLY);
        task.setOwnersInput(new String[]{player.getName()});
        ListenableFuture<?> future = Wg.getExecutorService().submit(task);

        AsyncCommandHelper.wrap(future, Wg, player).formatUsing(player.getName() + "_" + playerData.rgnum)
                .registerWithSupervisor("保護申請中").thenRespondWith("保護申請完了。保護名: '%s'", "保護作成失敗");
    }

    @EventHandler
    public void onPlayerClickRegionTemplateMenu(InventoryClickEvent event) {
        //外枠のクリック処理なら終了
        if (event.getClickedInventory() == null) {
            return;
        }
        //クリックしたところにアイテムがない場合終了
        if (event.getCurrentItem() == null) {
            return;
        }

        ItemStack itemstackcurrent = event.getCurrentItem();
        InventoryView view = event.getView();
        HumanEntity he = view.getPlayer();
        //インベントリを開けたのがプレイヤーではない時終了
        if (!he.getType().equals(EntityType.PLAYER)) {
            return;
        }
        Inventory topinventory = view.getTopInventory();
        //インベントリが存在しない時終了
        if (topinventory == null) {
            return;
        }

        //インベントリ名が以下の時処理
        if (topinventory.getTitle().equals(ChatColor.LIGHT_PURPLE + "グリッド式保護・設定保存")) {
            event.setCancelled(true);

            //プレイヤーインベントリのクリックの場合終了
            if (event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
                return;
            }

            /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
            Player player = (Player) view.getPlayer();
            UUID uuid = player.getUniqueId();
            PlayerData playerData = playermap.get(uuid);
            Map<Integer, GridTemplate> templateMap = playerData.getTemplateMap();

            //戻るボタン
            if (itemstackcurrent.getType().equals(Material.BARRIER)) {
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
                player.openInventory(RegionMenuData.getGridWorldGuardMenu(player));
                return;
            } else {
                int slot = event.getSlot();

                if (templateMap.get(slot) == null) {
                    return;
                }

                if (templateMap.get(slot).isEmpty()) {
                    //何も登録されてないとき
                    if (event.isLeftClick()) {
                        //左クリックの時は新規登録処理
                        playerGridTemplateSave(player, slot);
                        player.openInventory(RegionMenuData.getGridTemplateInventory(player));
                    }
                } else {
                    //登録されていた時
                    if (event.isLeftClick()) {
                        player.sendMessage(ChatColor.GREEN + "グリッド式保護設定データ読み込み完了");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        GridTemplate template = templateMap.get(slot);
                        playerData.setUnitAmount(DirectionType.AHEAD, template.getAheadAmount());
                        playerData.setUnitAmount(DirectionType.BEHIND, template.getBehindAmount());
                        playerData.setUnitAmount(DirectionType.RIGHT, template.getRightAmount());
                        playerData.setUnitAmount(DirectionType.LEFT, template.getLeftAmount());
                        setWGSelection(player);
                        canCreateRegion(player);
                        player.openInventory(RegionMenuData.getGridWorldGuardMenu(player));
                    }

                    if (event.isRightClick()) {
                        //新規登録処理
                        playerGridTemplateSave(player, slot);
                        player.openInventory(RegionMenuData.getGridTemplateInventory(player));
                    }
                }
            }
        }
    }

    private static void playerGridTemplateSave(Player player, int i) {
        PlayerData playerData = SeichiAssist.playermap.get(player.getUniqueId());
        Map<DirectionType,Integer> unitMap = playerData.getUnitMap();
        Map<Integer, GridTemplate> templateMap = playerData.getTemplateMap();

        player.sendMessage(ChatColor.GREEN + "グリッド式保護の現在の設定を保存しました。");
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);
        GridTemplate template = new GridTemplate(unitMap.get(DirectionType.AHEAD), unitMap.get(DirectionType.BEHIND),
                unitMap.get(DirectionType.RIGHT), unitMap.get(DirectionType.LEFT));
        templateMap.put(i, template);
        playerData.setTemplateMap(templateMap);
    }

    /**
     * ユニット単位における最短の始点のx,z座標を取得します。
     * @param player 該当プレイヤー
     * @return x,z座標のMap
     */
    public static Map<String, Double> getNearlyUnitStart(Player player) {
        Map<String, Double> result = new HashMap<>();

        double player_x = player.getLocation().getBlockX();
        double player_z = player.getLocation().getBlockZ();

        if (player_x % 15 == 0) {
            result.put("x", player_x);
        } else {
            result.put("x", Math.floor(player_x / 15) * 15);
        }

        if (player_z % 15 == 0) {
            result.put("z", player_z);
        } else {
            result.put("z", Math.floor(player_z / 15) * 15);
        }
        return result;
    }

    /**
     * ユニット単位における最短の終点(始点から対角になる)のx,z座標を取得します。
     * @param player 該当プレイヤー
     * @return x,z座標のMap
     */
    public static Map<String, Double> getNearlyUnitEnd(Player player) {
        Map<String, Double> startCoordinate = getNearlyUnitStart(player);

        Map<String, Double> resultMap = new HashMap<>();

        resultMap.put("x", startCoordinate.get("x") + 14.0);
        resultMap.put("z", startCoordinate.get("z") + 14.0);

        return resultMap;
    }
}
