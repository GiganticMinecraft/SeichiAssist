package com.github.unchama.seichiassist;

import com.github.unchama.seichiassist.bungee.BungeeReceiver;
import com.github.unchama.seichiassist.commands.*;
import com.github.unchama.seichiassist.commands.legacy.*;
import com.github.unchama.seichiassist.data.GachaPrize;
import com.github.unchama.seichiassist.data.MineStackGachaData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.RankData;
import com.github.unchama.seichiassist.data.menu.MenuHandler;
import com.github.unchama.seichiassist.database.DatabaseGateway;
import com.github.unchama.seichiassist.listener.*;
import com.github.unchama.seichiassist.listener.new_year_event.NewYearsEvent;
import com.github.unchama.seichiassist.minestack.MineStackObj;
import com.github.unchama.seichiassist.task.EveryHalfHourTask;
import com.github.unchama.seichiassist.task.EveryMinuteTask;
import com.github.unchama.seichiassist.task.PlayerDataBackupTask;
import com.github.unchama.seichiassist.task.PlayerDataSaveTask;
import com.github.unchama.seichiassist.util.Util;
import com.github.unchama.util.collection.ImmutableListFactory;
import com.github.unchama.util.collection.MapFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import static com.github.unchama.util.ActionStatus.Fail;


public class SeichiAssist extends JavaPlugin{

	public static SeichiAssist instance;
	//デバッグフラグ(デバッグモード使用時はここで変更するのではなくconfig.ymlの設定値を変更すること！)
	public static boolean DEBUG = false;
	//ガチャシステムのメンテナンスフラグ
	public static boolean gachamente = false;

	// TODO これらは DatabaseConstants に移されるべき
	public static final String PLAYERDATA_TABLENAME = "playerdata";

	public static final String SEICHIWORLDNAME = "world_sw";
	public static final String DEBUGWORLDNAME = "world";

	// TODO staticであるべきではない
	public static DatabaseGateway databaseGateway;
	public static Config config;

	//起動するタスクリスト
	private List<BukkitTask> tasklist = new ArrayList<>();

	//Gachadataに依存するデータリスト
	public static final List<GachaPrize> gachadatalist = new ArrayList<>();

	//(minestackに格納する)Gachadataに依存するデータリスト
	public static List<MineStackGachaData> msgachadatalist = new ArrayList<>();

	//Playerdataに依存するデータリスト
	public static final HashMap<UUID, PlayerData> playermap = new HashMap<>();

	//総採掘量ランキング表示用データリスト
	public static final List<RankData> ranklist = new ArrayList<>();

	//プレイ時間ランキング表示用データリスト
	public static final List<RankData> ranklist_playtick = new ArrayList<>();

	//投票ポイント表示用データリスト
	public static final List<RankData> ranklist_p_vote = new ArrayList<>();

	//マナ妖精表示用のデータリスト
	public static final List<RankData> ranklist_p_apple = new ArrayList<>();

	//プレミアムエフェクトポイント表示用データリスト
	public static final List<RankData> ranklist_premiumeffectpoint = new ArrayList<>();

	//総採掘量表示用
	public static long allplayerbreakblockint;

	public static long allplayergiveapplelong;

	//プラグインで出すエンティティの保存
	public static final List<Entity> entitylist = new ArrayList<>();

	//プレイヤーがスキルで破壊するブロックリスト
	public static final List<Block> allblocklist = new ArrayList<>();


	public static final Set<Material> materiallist = EnumSet.of(
			Material.STONE,Material.NETHERRACK,Material.NETHER_BRICK,Material.DIRT
			,Material.GRAVEL,Material.LOG,Material.LOG_2,Material.GRASS
			,Material.COAL_ORE,Material.IRON_ORE,Material.GOLD_ORE,Material.DIAMOND_ORE
			,Material.LAPIS_ORE,Material.EMERALD_ORE,Material.REDSTONE_ORE,Material.GLOWING_REDSTONE_ORE,Material.SAND
			,Material.SANDSTONE,Material.QUARTZ_ORE,Material.END_BRICKS,Material.ENDER_STONE
			,Material.ICE,Material.PACKED_ICE,Material.OBSIDIAN,Material.MAGMA,Material.SOUL_SAND,Material.LEAVES,Material.LEAVES_2
			,Material.CLAY,Material.STAINED_CLAY,Material.COBBLESTONE,Material.MOSSY_COBBLESTONE,Material.HARD_CLAY
			,Material.MONSTER_EGGS,Material.WEB,Material.WOOD,Material.FENCE,Material.DARK_OAK_FENCE,Material.RAILS //追加
			,Material.MYCEL,Material.SNOW_BLOCK,Material.HUGE_MUSHROOM_1,Material.HUGE_MUSHROOM_2,Material.BONE_BLOCK //追加
			,Material.PURPUR_BLOCK,Material.PURPUR_PILLAR,Material.SEA_LANTERN,Material.PRISMARINE //追加
			,Material.SMOOTH_BRICK,Material.GLOWSTONE //追加
			,Material.STAINED_GLASS,Material.STAINED_GLASS_PANE,Material.THIN_GLASS,Material.GLASS
			,Material.WOOD_STAIRS,Material.BIRCH_WOOD_STAIRS,Material.SPRUCE_WOOD_STAIRS,Material.ACACIA_STAIRS,Material.DARK_OAK_STAIRS
			,Material.BIRCH_FENCE,Material.SPRUCE_FENCE,Material.ACACIA_FENCE
			,Material.FENCE_GATE,Material.BIRCH_FENCE_GATE,Material.SPRUCE_FENCE_GATE,Material.ACACIA_FENCE_GATE,Material.DARK_OAK_FENCE_GATE
			,Material.COBBLESTONE_STAIRS,Material.SANDSTONE_STAIRS,Material.BRICK_STAIRS,Material.QUARTZ_STAIRS
			,Material.BOOKSHELF,Material.IRON_FENCE,Material.ICE,Material.WOOL,Material.GOLD_BLOCK
			,Material.END_ROD,Material.PUMPKIN,Material.MELON_BLOCK,Material.STONE_SLAB2,Material.SPONGE
			,Material.SOIL,Material.GRASS_PATH,Material.MOB_SPAWNER,Material.WORKBENCH,Material.FURNACE
			,Material.QUARTZ_BLOCK
			,Material.CHEST,Material.TRAPPED_CHEST
			);
	public static final Set<Material> luckmateriallist = EnumSet.of(
			Material.COAL_ORE,Material.DIAMOND_ORE,Material.LAPIS_ORE,Material.EMERALD_ORE,
			Material.REDSTONE_ORE,Material.GLOWING_REDSTONE_ORE,Material.QUARTZ_ORE
			);
	public static final Set<Material> breakmateriallist = EnumSet.of(
			Material.DIAMOND_PICKAXE,Material.DIAMOND_AXE,Material.DIAMOND_SPADE,
			Material.WOOD_PICKAXE,						  Material.WOOD_SPADE,
			Material.IRON_PICKAXE,Material.IRON_AXE,Material.IRON_SPADE,
			Material.GOLD_PICKAXE,Material.GOLD_AXE,Material.GOLD_SPADE
			);
	public static final Set<Material> cancelledmateriallist = EnumSet.of(
			Material.CHEST,Material.ENDER_CHEST,Material.TRAPPED_CHEST,Material.ANVIL,Material.ARMOR_STAND
			,Material.BEACON,Material.BIRCH_DOOR,Material.BIRCH_FENCE_GATE,Material.BIRCH_WOOD_STAIRS
			,Material.BOAT,Material.FURNACE,Material.WORKBENCH,Material.HOPPER,Material.MINECART
			);

	public static final Set<Material> transparentmateriallist = EnumSet.of(
			Material.BEDROCK,Material.AIR
			);
	public static final Set<Material> gravitymateriallist = EnumSet.of(
			Material.LOG, Material.LOG_2,Material.LEAVES,Material.LEAVES_2
			);
	//スキル破壊ブロック分のcoreprotectログ保存処理を除外するワールドリスト(coreprotectログデータ肥大化の軽減が目的)
	//スキル自体はメインワールドと各整地ワールドのみ(world_SWで始まるワールドのみ)で発動する(ここの設定は無視する)
	public static final List<String> ignoreWorldlist = ImmutableListFactory.of(
			"world_SW","world_SW_2","world_SW_3","world_SW_nether","world_SW_the_end"
			);

	//保護を掛けて整地するワールドのリスト
	public static final List<String> rgSeichiWorldlist = ImmutableListFactory.of(
			"world_SW_2"
			);

	//整地ワールドのリスト(保護の有無は問わない)
	public static final List<String> seichiWorldList = ImmutableListFactory.of(
			"world_SW", "world_SW_2", "world_SW_3", "world_SW_nether", "world_SW_the_end"
	);

	@Override
	public void onEnable(){
		instance = this;

		//チャンネルを追加
		String pluginChannel = "BungeeCord";
		Bukkit.getMessenger().registerOutgoingPluginChannel(this,
				pluginChannel);

		//コンフィグ系の設定は全てConfig.javaに移動
		config = new Config(this);
		config.loadConfig();

		final ConsoleCommandSender ccs = Bukkit.getConsoleSender();
		if(SeichiAssist.config.getDebugMode()==1){
			//debugmode=1の時は最初からデバッグモードで鯖を起動
			ccs.sendMessage(ChatColor.RED + "seichiassistをデバッグモードで起動します");
			ccs.sendMessage(ChatColor.RED + "コンソールから/seichi debugmode");
			ccs.sendMessage(ChatColor.RED + "を実行するといつでもONOFFを切り替えられます");
			DEBUG = true;
		}else{
			//debugmode=0の時は/seichi debugmodeによる変更コマンドも使えない
			ccs.sendMessage(ChatColor.GREEN + "seichiassistを通常モードで起動します");
			ccs.sendMessage(ChatColor.GREEN + "デバッグモードを使用する場合は");
			ccs.sendMessage(ChatColor.GREEN + "config.ymlの設定値を書き換えて再起動してください");
		}

		// TODO nullチェック
		databaseGateway = DatabaseGateway.createInitializedInstance(config.getURL(), config.getDB(), config.getID(), config.getPW());

		//mysqlからガチャデータ読み込み
		if(!databaseGateway.gachaDataManipulator.loadGachaData()){
			getLogger().info("ガチャデータのロードに失敗しました");
			Bukkit.shutdown();
		}

		//mysqlからMineStack用ガチャデータ読み込み
		if (databaseGateway.mineStackGachaDataManipulator.loadMineStackGachaData()) { //MineStack用ガチャデータを読み込んだ
			getLogger().info("MineStack用ガチャデータのロードに成功しました");

			MineStackObjectList.INSTANCE.getMinestacklistgacha().addAll(creategachaminestacklist());

			MineStackObjectList.INSTANCE.setMinestacklist(new ArrayList<>());
			MineStackObjectList.INSTANCE.getMinestacklist().addAll(MineStackObjectList.INSTANCE.getMinestacklistmine());
			MineStackObjectList.INSTANCE.getMinestacklist().addAll(MineStackObjectList.INSTANCE.getMinestacklistdrop());
			MineStackObjectList.INSTANCE.getMinestacklist().addAll(MineStackObjectList.INSTANCE.getMinestacklistfarm());
			MineStackObjectList.INSTANCE.getMinestacklist().addAll(MineStackObjectList.INSTANCE.getMinestacklistbuild());
			MineStackObjectList.INSTANCE.getMinestacklist().addAll(MineStackObjectList.INSTANCE.getMinestacklistrs());
			MineStackObjectList.INSTANCE.getMinestacklist().addAll(MineStackObjectList.INSTANCE.getMinestacklistgacha());

		} else {
			getLogger().info("MineStack用ガチャデータのロードに失敗しました");
			Bukkit.shutdown();
		}

		{
			// コマンドの登録
			MapFactory.of(
					Pair.of("gacha", new GachaCommand()),
					Pair.of("ef", EffectCommand.INSTANCE.getExecutor()),
					Pair.of("seichihaste", SeichiHasteCommand.INSTANCE.getExecutor()),
					Pair.of("seichiassist", SeichiAssistCommand.INSTANCE.getExecutor()),
					Pair.of("openpocket", OpenPocketCommand.INSTANCE.getExecutor()),
					Pair.of("lastquit", LastQuitCommand.INSTANCE.getExecutor()),
					Pair.of("stick", StickCommand.INSTANCE.getExecutor()),
					Pair.of("rmp", RmpCommand.INSTANCE.getExecutor()),
					Pair.of("shareinv", ShareInvCommand.INSTANCE.getExecutor()),
					Pair.of("mebius", MebiusCommand.INSTANCE.getExecutor()),
					Pair.of("achievement", AchievementCommand.INSTANCE.getExecutor()),
					Pair.of("halfguard", HalfBlockProtectCommand.INSTANCE.getExecutor()),
					Pair.of("event", EventCommand.INSTANCE.getExecutor()),
					Pair.of("contribute", ContributeCommand.INSTANCE.getExecutor()),
					Pair.of("subhome", SubHomeCommand.INSTANCE.getExecutor()),
					Pair.of("gtfever", GiganticFeverCommand.INSTANCE.getExecutor()),
					Pair.of("minehead", MineHeadCommand.INSTANCE.getExecutor()),
					Pair.of("x-transfer", RegionOwnerTransferCommand.INSTANCE.getExecutor())
			).forEach((commandName, executor) -> getCommand(commandName).setExecutor(executor));
		}

		//リスナーの登録
		getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerClickListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerChatEventListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerBlockBreakListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerInventoryListener(), this);
		getServer().getPluginManager().registerEvents(new EntityListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerPickupItemListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerDeathEventListener(), this);
		getServer().getPluginManager().registerEvents(new GachaItemListener(), this);
		getServer().getPluginManager().registerEvents(new MebiusListener(), this);
		getServer().getPluginManager().registerEvents(new RegionInventoryListener(), this);
		getServer().getPluginManager().registerEvents(new WorldRegenListener(), this);
		//正月イベント用
		new NewYearsEvent(this);
		//Menu用Listener
		getServer().getPluginManager().registerEvents(MenuHandler.getInstance(), this);
		// BungeeCordとのI/O
		Bukkit.getMessenger().registerIncomingPluginChannel(this, "SeichiAssistBungee", new BungeeReceiver(this));
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "SeichiAssistBungee");


		//オンラインの全てのプレイヤーを処理
		for(Player p : getServer().getOnlinePlayers()){
			//プレイヤーデータを生成
			databaseGateway.playerDataManipulator.loadPlayerData(new PlayerData(p));
		}

		//ランキングリストを最新情報に更新する
		if(!databaseGateway.playerDataManipulator.updateAllRankingList()){
			getLogger().info("ランキングデータの作成に失敗しました");
			Bukkit.shutdown();
		}

		//タスクスタート
		startTaskRunnable();

		getLogger().info("SeichiAssist is Enabled!");
	}

	@Override
	public void onDisable(){
		//全てのタスクをキャンセル
		stopAllTaskRunnable();

		//全てのエンティティを削除
		for(Entity e :entitylist){
			e.remove();
		}

		//全てのスキルで破壊されるブロックを強制破壊
		for(Block b : allblocklist){
			b.setType(Material.AIR);
		}

		//sqlコネクションチェック
		databaseGateway.ensureConnection();
		for(Player p : getServer().getOnlinePlayers()){
			//UUIDを取得
			UUID uuid = p.getUniqueId();
			//プレイヤーデータ取得
			PlayerData playerdata = playermap.get(uuid);
			//念のためエラー分岐
			if(playerdata == null){
				p.sendMessage(ChatColor.RED + "playerdataの保存に失敗しました。管理者に報告してください");
				getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[Ondisable処理]でエラー発生");
				getLogger().warning(Util.getName(p)+ "のplayerdataの保存失敗。開発者に報告してください");
				continue;
			}
			//quit時とondisable時、プレイヤーデータを最新の状態に更新
			playerdata.updateonQuit(p);

			new PlayerDataSaveTask(playerdata,true,true).run();
		}

		if(databaseGateway.disconnect() == Fail){
			getLogger().info("データベース切断に失敗しました");
		}

		getLogger().info("SeichiAssist is Disabled!");
	}

	public void startTaskRunnable(){
		//一定時間おきに処理を実行するタスク
		if(DEBUG){
			tasklist.add(new EveryHalfHourTask().runTaskTimer(this,440,400));
		}else{
			tasklist.add(new EveryHalfHourTask().runTaskTimer(this,36400,36000));
		}

		if(DEBUG){
			tasklist.add(new EveryMinuteTask().runTaskTimer(this,0,200));
		}else{
			tasklist.add(new EveryMinuteTask().runTaskTimer(this,0,1200));
		}

		//非同期処理にしたいけど別ステートメントでsql文処理させるようにしてからじゃないとだめぽ
		if(DEBUG){
			tasklist.add(new PlayerDataBackupTask().runTaskTimer(this,480,400));
		}else{
			tasklist.add(new PlayerDataBackupTask().runTaskTimer(this,12800,12000));
		}
	}

	public void stopAllTaskRunnable(){
		for(BukkitTask task:tasklist){
			task.cancel();
		}
	}

	private static List<MineStackObj> creategachaminestacklist(){
		List<MineStackObj> minestacklist = new ArrayList<>();
		for(int i=0; i<SeichiAssist.msgachadatalist.size(); i++){
			MineStackGachaData g = SeichiAssist.msgachadatalist.get(i);
			if(g.getItemStack().getType() != Material.EXP_BOTTLE){ //経験値瓶だけはすでにリストにあるので除外
				minestacklist.add(new MineStackObj(g.getObjName(), g.getLevel(), g.getItemStack(),true,i,5));
			}
		}
		return minestacklist;
	}


}


