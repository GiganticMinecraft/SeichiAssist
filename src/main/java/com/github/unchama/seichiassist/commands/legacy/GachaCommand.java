package com.github.unchama.seichiassist.commands.legacy;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.GachaPrize;
import com.github.unchama.seichiassist.data.MineStackGachaData;
import com.github.unchama.seichiassist.database.DatabaseGateway;
import com.github.unchama.seichiassist.util.StaticGachaPrizeFactory;
import com.github.unchama.seichiassist.util.TypeConverter;
import com.github.unchama.seichiassist.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

import static com.github.unchama.util.ActionStatus.Fail;

public class GachaCommand implements CommandExecutor {
	// /gacha set 0.01 (現在手にもってるアイテムが確率0.01でガチャに出現するように設定）
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		DatabaseGateway databaseGateway = SeichiAssist.Companion.getDatabaseGateway();

		if(args.length == 0){
			return false;
		}else if(args[0].equalsIgnoreCase("help")){

			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD +"[コマンドリファレンス]");
			sender.sendMessage(ChatColor.RED + "/gacha mente");
			sender.sendMessage("メンテモードのON,OFF切り替え。ONだとガチャが引けなくなる");
			sender.sendMessage(ChatColor.RED + "/gacha give <all/プレイヤー名> <個数>");
			sender.sendMessage("ガチャ券配布コマンドです。allを指定で全員に配布(マルチ鯖対応済)");
			sender.sendMessage(ChatColor.RED + "/gacha vote <プレイヤー名>");
			sender.sendMessage("投票特典配布用コマンドです(マルチ鯖対応済)");
			sender.sendMessage(ChatColor.RED + "/gacha donate <プレイヤー名> <ポイント数>");
			sender.sendMessage("寄付者用プレミアムエフェクトポイント配布コマンドです(マルチ鯖対応済)");
			sender.sendMessage(ChatColor.RED + "/gacha get <ID> (<名前>)");
			sender.sendMessage("指定したガチャリストのIDを入手 (所有者付きにもできます) IDを0に指定するとガチャリンゴを入手できます");
			sender.sendMessage(ChatColor.RED + "/gacha add <確率>");
			sender.sendMessage("現在のメインハンドをガチャリストに追加。確率は1.0までで指定");
			sender.sendMessage(ChatColor.RED + "/gacha addms2 <確率> <名前> <レベル>");
			sender.sendMessage("現在のメインハンドをMineStack用ガチャリストに追加。確率は1.0までで指定");
			sender.sendMessage(ChatColor.RED + "/gacha addms <名前> <レベル> <ID>");
			sender.sendMessage("指定したガチャリストのIDを指定した名前とレベル(実際のレベルではないことに注意)でMineStack用ガチャリストに追加");
			sender.sendMessage(ChatColor.DARK_GRAY + "※ゲーム内でのみ実行できます");
			sender.sendMessage(ChatColor.RED + "/gacha list");
			sender.sendMessage("現在のガチャリストを表示");
			sender.sendMessage(ChatColor.RED + "/gacha listms");
			sender.sendMessage("現在のMineStack用ガチャリストを表示");
			sender.sendMessage(ChatColor.RED + "/gacha remove <番号>");
			sender.sendMessage("リスト該当番号のガチャ景品を削除");
			sender.sendMessage(ChatColor.RED + "/gacha removems");
			sender.sendMessage("リスト一番下のMineStackガチャ景品を削除(追加失敗した場合の修正用)");
			sender.sendMessage(ChatColor.RED + "/gacha setamount <番号> <個数>");
			sender.sendMessage("リスト該当番号のガチャ景品の個数変更。64まで");
			sender.sendMessage(ChatColor.RED + "/gacha setprob <番号> <確率>");
			sender.sendMessage("リスト該当番号のガチャ景品の確率変更");
			sender.sendMessage(ChatColor.RED + "/gacha move <番号> <移動先番号>");
			sender.sendMessage("リスト該当番号のガチャ景品の並び替えを行う");
			sender.sendMessage(ChatColor.RED + "/gacha clear");
			sender.sendMessage("ガチャリストを全消去する。取扱注意");
			sender.sendMessage(ChatColor.RED + "/gacha save");
			sender.sendMessage("コマンドによるガチャリストへの変更をmysqlに送信");
			sender.sendMessage(ChatColor.RED + "/gacha savems");
			sender.sendMessage("コマンドによるMineStack用ガチャリストへの変更をmysqlに送信");
			sender.sendMessage(ChatColor.DARK_RED + "※変更したら必ずsaveコマンドを実行(セーブされません)");
			sender.sendMessage(ChatColor.RED + "/gacha reload");
			sender.sendMessage("ガチャリストをmysqlから読み込む");
			sender.sendMessage(ChatColor.DARK_GRAY + "※onEnable時と同じ処理");
			sender.sendMessage(ChatColor.RED + "/gacha demo <回数>");
			sender.sendMessage("現在のガチャリストで指定回数試行し結果を表示。100万回まで");

			return true;

		}else if(args[0].equalsIgnoreCase("give")){
			//gacha give と入力したとき
			//[2]:プレイヤー名/all
			//[3]:個数
			if(args.length != 3){
				//引数が3でない時の処理
				sender.sendMessage(ChatColor.RED + "/gacha give <all/プレイヤー名> <個数>");
				sender.sendMessage("ガチャ券配布コマンドです。allを指定すると全員に配布します");
				return true;
			}else{
				//引数が3の時の処理

				//プレイヤー名を取得
				String name = Util.getName(args[1]);
				//個数取得
				int num = TypeConverter.toInt(args[2]);

				if(!name.equalsIgnoreCase("all")){
					//プレイヤー名がallでない時の処理

					//プレイヤーオンライン、オフラインにかかわらずsqlに送信(マルチ鯖におけるコンフリクト防止の為)
					sender.sendMessage(ChatColor.YELLOW + name + "のガチャ券配布処理開始…");

					//mysqlにも書き込んどく
					if(databaseGateway.playerDataManipulator.addPlayerBug(name,num) == Fail){
						sender.sendMessage(ChatColor.RED + "失敗");
					}else{
						sender.sendMessage(ChatColor.GREEN + "ガチャ券" + num +"枚加算成功");
					}
					return true;

				}else{
					//プレイヤー名がallの時の処理(全員に配布)

					//プレイヤーオンライン、オフラインにかかわらずsqlに送信(マルチ鯖におけるコンフリクト防止の為)
					sender.sendMessage(ChatColor.YELLOW + "全プレイヤーへのガチャ券配布処理開始…");

					//MySql処理
					if(databaseGateway.playerDataManipulator.addAllPlayerBug(num) == Fail){
						sender.sendMessage(ChatColor.RED + "失敗");
					}else{
						sender.sendMessage(ChatColor.GREEN + "ガチャ券" + num +"枚加算成功");
					}
					//addSorryForBug(sender,Util.toInt(args[2]));
					return true;
				}
			}


		}else if(args[0].equalsIgnoreCase("vote")){
			if(args.length != 2){
				//引数が2つでない時の処理
				sender.sendMessage(ChatColor.RED + "/gacha vote <プレイヤー名>");
				sender.sendMessage("投票特典配布用コマンドです");
				return true;
			}else{
				//引数が2つの時の処理

				String lowerCasePlayerName = Util.getName(args[1]);

				//プレイヤーオンライン、オフラインにかかわらずsqlに送信(マルチ鯖におけるコンフリクト防止の為)
				sender.sendMessage(ChatColor.YELLOW + lowerCasePlayerName + "の投票特典配布処理開始…");

				//mysqlにも書き込んどく
				if(databaseGateway.playerDataManipulator.incrementVotePoint(lowerCasePlayerName) == Fail){
					sender.sendMessage(ChatColor.RED + "失敗");
				}else{
					sender.sendMessage(ChatColor.GREEN + "成功");
				}
				if(!databaseGateway.playerDataManipulator.addChainVote(lowerCasePlayerName)){
					sender.sendMessage(ChatColor.RED + "連続投票数の記録に失敗");
				}else{
					sender.sendMessage(ChatColor.GREEN + "連続投票数の記録に成功");
				}
				return true;
			}

		}else if(args[0].equalsIgnoreCase("donate")){
			if(args.length != 3){
				//引数が3でない時の処理
				sender.sendMessage(ChatColor.RED + "/gacha donate <プレイヤー名> <ポイント数>");
				sender.sendMessage("寄付者用プレミアムエフェクトポイント配布コマンドです");
				return true;
			}else{
				//引数が3の時の処理

				//プレイヤー名を取得(小文字にする)
				String name = Util.getName(args[1]);
				//配布ポイント数取得
				int num = TypeConverter.toInt(args[2]);

				//プレイヤーオンライン時はplayerdataに直接反映、オフライン時はsqlに送信(結果をsenderへ)
				sender.sendMessage(ChatColor.YELLOW + name + "のプレミアムエフェクトポイント配布処理開始…");

				//mysqlにも書き込んどく
				if(databaseGateway.playerDataManipulator.addPremiumEffectPoint(name,num) == Fail || databaseGateway.donateDataManipulator.addDonate(name, num) == Fail){
					sender.sendMessage(ChatColor.RED + "失敗");
				}else{
					sender.sendMessage(ChatColor.GREEN + "成功");
				}
				return true;
			}

		}else if(args[0].equalsIgnoreCase("mente")){
				//menteフラグ反転処理
				SeichiAssist.Companion.setGachamente(!SeichiAssist.Companion.getGachamente());
				if (SeichiAssist.Companion.getGachamente()){
					sender.sendMessage(ChatColor.GREEN + "ガチャシステムを一時停止しました");
				}else{
					sender.sendMessage(ChatColor.GREEN + "ガチャシステムを再開しました");
				}
				return true;
		}else if(args[0].equalsIgnoreCase("reload")){
			//gacha reload と入力したとき
			if(!databaseGateway.gachaDataManipulator.loadGachaData()){
				sender.sendMessage("mysqlからガチャデータのロードできませんでした");
			}else{
				sender.sendMessage("mysqlからガチャデータをロードしました");
			}
			return true;
		}else if(args[0].equalsIgnoreCase("save")){
			//gacha save と入力したとき
			if(!databaseGateway.gachaDataManipulator.saveGachaData()){
				sender.sendMessage("mysqlにガチャデータを保存できませんでした");
			}else{
				sender.sendMessage("mysqlにガチャデータを保存しました");
			}
			return true;

		}else if(args[0].equalsIgnoreCase("savems")){
			//gacha save と入力したとき
			if(!databaseGateway.mineStackGachaDataManipulator.saveMineStackGachaData()){
				sender.sendMessage("mysqlにMineStack用ガチャデータを保存できませんでした");
			}else{
				sender.sendMessage("mysqlにMineStack用ガチャデータを保存しました");
			}
			return true;

		}

		else if(args[0].equalsIgnoreCase("get")){
			if(args.length != 2 && args.length != 3 ){
				sender.sendMessage("/gacha get 1  または /gacha get 1 unchama のように、入手したいガチャアイテムのID(と名前)を入力してください");
				return true;
			}
			/*
			 * コンソールからのコマンドは処理しない - ここから
			 */
			if (!(sender instanceof Player)) {
				sender.sendMessage("このコマンドはゲーム内から実行してください");
				return true;
			}
			Player player = (Player) sender;
			/*
			 * ここまで
			 */

			if(args.length==2){
				int id = TypeConverter.toInt(args[1]);
				Gachagive(player,id,null);
			} else if(args.length==3){
				int id = TypeConverter.toInt(args[1]);
				Gachagive(player,id,args[2]);
			}
			return true;

		} else if (args[0].equalsIgnoreCase("giveitem")) {
			if (sender instanceof Player) {
				sender.sendMessage("このコマンドはプレイヤーからは送信できません。");
				return true;
			}
			if (args.length != 3) {
				sender.sendMessage("/gacha giveitem <ガチャアイテムID> <与えるプレイヤーUUID> のように入力してください。");
				return true;
			}

			Player player = Bukkit.getPlayer(UUID.fromString(args[2]));

			if (player == null) {
				sender.sendMessage("プレイヤーがオフラインの際コマンド使用はできません。");
				return true;
			}

			int id = TypeConverter.toInt(args[1]);
			Gachagive(player, id, player.getName());
			return true;
		}

		else if(args[0].equalsIgnoreCase("add")){
			if(args.length != 2){
				sender.sendMessage("/gacha add 0.05  のように、追加したいアイテムの出現確率を入力してください");
				return true;
			}
			/*
			 * コンソールからのコマンドは処理しない - ここから
			 */
			if (!(sender instanceof Player)) {
				sender.sendMessage("このコマンドはゲーム内から実行してください");
				return true;
			}
			Player player = (Player) sender;
			/*
			 * ここまで
			 */

			double probability = TypeConverter.toDouble(args[1]);
			Gachaadd(player,probability);
			return true;
		}else if(args[0].equalsIgnoreCase("addms2")){
			if(args.length != 4){
				sender.sendMessage("/gacha addms2 0.05 gacha1 5  のように、追加したいアイテムの出現確率、名前、レベルを入力してください");
				return true;
			}
			/*
			 * コンソールからのコマンドは処理しない - ここから
			 */
			if (!(sender instanceof Player)) {
				sender.sendMessage("このコマンドはゲーム内から実行してください");
				return true;
			}
			Player player = (Player) sender;
			/*
			 * ここまで
			 */

			double probability = TypeConverter.toDouble(args[1]);
			int level = TypeConverter.toInt(args[3]);
			Gachaaddms2(player,probability, args[2], level);
			return true;
		} else if(args[0].equalsIgnoreCase("addms")) {
			if(args.length != 4){
				sender.sendMessage("/gacha addms <名前> <レベル> <ID> のように、追加したいアイテムの名前とID、レベルを入力してください");
				return true;
			}

			int level = TypeConverter.toInt(args[2]);
			int num = TypeConverter.toInt(args[3]);
			Gachaaddms(sender, args[1], level, num);

			return true;
		}else if(args[0].equalsIgnoreCase("remove")){
			if(args.length != 2){
				sender.sendMessage("/gacha remove 2 のように、削除したいリスト番号を入力してください");
				return true;
			}
			int num = TypeConverter.toInt(args[1]);
			Gacharemove(sender,num);
			return true;
		}else if(args[0].equalsIgnoreCase("removems")){
			if(args.length != 1){
				sender.sendMessage("/gacha removemsのように入力してください");
				return true;
			}
			Gacharemovems(sender);
			return true;
		}else if(args[0].equalsIgnoreCase("setamount")){
			if(args.length != 3){
				sender.sendMessage("/gacha setamount 2 1 のように、変更したいリスト番号と変更後のアイテム個数を入力してください");
				return true;
			}
			int num = TypeConverter.toInt(args[1]);
			int amount = TypeConverter.toInt(args[2]);
			GachaEditAmount(sender,num,amount);
			return true;
		}else if(args[0].equalsIgnoreCase("setprob")){
			if(args.length != 3){
				sender.sendMessage("/gacha setprob 2 1 のように、変更したいリスト番号と変更後の確率を入力してください");
				return true;
			}
			int num = TypeConverter.toInt(args[1]);
			double probability = TypeConverter.toDouble(args[2]);
			GachaEditProbability(sender,num,probability);
			return true;
		}else if(args[0].equalsIgnoreCase("move")){
			if(args.length != 3){
				sender.sendMessage("/gacha move 2 10 のように、変更したいリスト番号と変更後のリスト番号を入力してください");
				return true;
			}
			int num = TypeConverter.toInt(args[1]);
			int tonum = TypeConverter.toInt(args[2]);
			GachaMove(sender,num,tonum);
			return true;
		}else if(args[0].equalsIgnoreCase("list")){
			if(args.length != 1){
				sender.sendMessage("/gacha list で現在登録されているガチャアイテムを全て表示します");
			}
			if(SeichiAssist.Companion.getGachadatalist().isEmpty()){
				sender.sendMessage("ガチャが設定されていません");
				return true;
			}
			Gachalist(sender);
			return true;
		} else if(args[0].equalsIgnoreCase("listms")){
			if(args.length != 1){
				sender.sendMessage("/gacha listms で現在登録されているガチャアイテムを全て表示します");
			}
			if(SeichiAssist.Companion.getMsgachadatalist().isEmpty()){
				sender.sendMessage("MineStack用ガチャリストが設定されていません");
				return true;
			}
			Gachalistms(sender);
			return true;
		} else if(args[0].equalsIgnoreCase("clear")){
			if(args.length != 1){
				sender.sendMessage("/gacha clear で現在登録されているガチャアイテムを削除します");
			}
			Gachaclear(sender);
			return true;
		}else if (args[0].equalsIgnoreCase("demo")){
			if(args.length != 2){
				sender.sendMessage("/gacha demo 10000  のように、試行したい回数を入力して下さい");
				return true;
			}
			int n = TypeConverter.toInt(args[1]);
			if(n > 1000000){
				sender.sendMessage("100万回以上は指定出来ません");
				return true;
			}
			//ガチャ券をn回試行してみる処理
			int i = 0;
			double p;
			int gigantic = 0;
			int big = 0;
			int regular = 0;
			int potato = 0;
			while(n > i){
				p = runGachaDemo();
				if(p < 0.001){
					gigantic ++;
				}else if(p < 0.01){
					big ++;
				}else if(p < 0.1){
					regular ++;
				}else{
					potato ++;
				}
				i++;
			}
			sender.sendMessage(
					ChatColor.AQUA + "" + ChatColor.BOLD + "ガチャ券" + i + "回試行結果\n"
					+ ChatColor.RESET + "ギガンティック："+ gigantic +"回("+ ((double)gigantic/(double)i*100.0) +"%)\n"
					+ "大当たり："+ big +"回("+ ((double)big/(double)i*100.0) +"%)\n"
					+ "当たり："+ regular +"回("+ ((double)regular/(double)i*100.0) +"%)\n"
					+ "ハズレ："+ potato +"回("+ ((double)potato/(double)i*100.0) +"%)\n"
					);
			return true;
		}

		return false;
	}

	public static void Gachagive(Player player, int _id, String name) {

		int id = _id-1;
		if(id>=-1 && id< SeichiAssist.Companion.getGachadatalist().size()){
			//プレゼント用ガチャデータ作成
			GachaPrize present;
			//ガチャ実行
			if(id>=0){
				present = SeichiAssist.Companion.getGachadatalist().get(id).copy();
			} else {
				present = new GachaPrize(StaticGachaPrizeFactory.getGachaRingo(),1.0);
			}
			if(present.getProbability() < 0.1){
				if(name!=null){
					present.appendOwnerLore(name);
				}
			}

			//プレゼントを格納orドロップ
			if(!Util.isPlayerInventoryFull(player)){
				Util.addItem(player, present.getItemStack());
			}else{
				Util.dropItem(player, present.getItemStack());
				//str += ChatColor.AQUA + "ガチャアイテムがドロップしました。";
			}
		}
	}


	private void Gachaadd(Player player,double probability) {
		PlayerInventory inventory = player.getInventory();
		ItemStack targetItemStack = inventory.getItemInMainHand();

		GachaPrize gachadata = new GachaPrize(targetItemStack, probability);

		SeichiAssist.Companion.getGachadatalist().add(gachadata);
		player.sendMessage(gachadata.getItemStack().getType().toString() + "/" + gachadata.getItemStack().getItemMeta().getDisplayName() + ChatColor.RESET + gachadata.getItemStackAmount() + "個を確率" + gachadata.getProbability() + "としてガチャに追加しました");
		player.sendMessage("/gacha saveでmysqlに保存してください");
	}

	private void Gachaaddms(CommandSender sender, String s, int level, int num) {
		int temp = num-1;
		if(temp >= 0 && temp < SeichiAssist.Companion.getGachadatalist().size()){
			GachaPrize g = SeichiAssist.Companion.getGachadatalist().get(temp);
			MineStackGachaData mg = new MineStackGachaData(s, g.getItemStack().clone(), g.getProbability(), level);
			SeichiAssist.Companion.getMsgachadatalist().add(mg);
			sender.sendMessage("データガチャリストID" + num + "のデータを" + "変数名:" + s + ",レベル:" + level + "でMineStack用ガチャデータリストに追加しました");
			sender.sendMessage("/gacha savemsでmysqlに保存してください");
		}
		sender.sendMessage("正しくないIDです");
	}

	private void Gachaaddms2(Player player,double probability, String name, int level) {
		ItemStack targetItemStack = player.getInventory().getItemInMainHand();
		MineStackGachaData gachaData = new MineStackGachaData(name, targetItemStack, probability, level);
		SeichiAssist.Companion.getMsgachadatalist().add(gachaData);
		player.sendMessage(gachaData.getItemStack().getType().toString() + "/" + gachaData.getItemStack().getItemMeta().getDisplayName() + ChatColor.RESET + gachaData.getAmount() + "個を確率" + gachaData.getProbability() + "としてMineStack用ガチャリストに追加しました");
		player.sendMessage("/gacha savemsでmysqlに保存してください");
	}

	private void Gachalist(CommandSender sender){
		int i = 1;
		double totalprobability = 0.0;
		sender.sendMessage(ChatColor.RED + "アイテム番号|アイテム名|アイテム数|出現確率");
		for (GachaPrize gachadata : SeichiAssist.Companion.getGachadatalist()) {
			sender.sendMessage(i + "|" + gachadata.getItemStack().getType().toString() + "/" + gachadata.getItemStack().getItemMeta().getDisplayName() + ChatColor.RESET + "|" + gachadata.getItemStackAmount() + "|" + gachadata.getProbability() + "(" + (gachadata.getProbability() *100) + "%)");
			totalprobability += gachadata.getProbability();
			i++;
		}
		sender.sendMessage(ChatColor.RED + "合計確率:" + totalprobability + "(" + (totalprobability*100) + "%)");
		sender.sendMessage(ChatColor.RED + "合計確率は100%以内に収まるようにしてください");
	}
	private void Gachalistms(CommandSender sender){
		int i = 1;
		//double totalprobability = 0.0;
		sender.sendMessage(ChatColor.RED + "アイテム番号|レベル|変数名|アイテム名|アイテム数|出現確率");
		for (MineStackGachaData gachadata : SeichiAssist.Companion.getMsgachadatalist()) {
			sender.sendMessage(i + "|" + gachadata.getLevel() + "|" + gachadata.getObjName() + "|" + gachadata.getItemStack().getType().toString() + "/" + gachadata.getItemStack().getItemMeta().getDisplayName() + ChatColor.RESET + "|" + gachadata.getAmount() + "|" + gachadata.getProbability() + "(" + (gachadata.getProbability() *100) + "%)");
			//totalprobability += gachadata.probability;
			i++;
		}
		//sender.sendMessage(ChatColor.RED + "合計確率:" + totalprobability + "(" + (totalprobability*100) + "%)");
		//sender.sendMessage(ChatColor.RED + "合計確率は100%以内に収まるようにしてください");
	}
	private void Gacharemove(CommandSender sender,int num) {
		if(num < 1 || SeichiAssist.Companion.getGachadatalist().size() < num){
			sender.sendMessage("アイテム番号が間違っているようです");
			return;
		}
		GachaPrize gachadata = SeichiAssist.Companion.getGachadatalist().get(num-1);
		SeichiAssist.Companion.getGachadatalist().remove(num-1);
		sender.sendMessage(num + "|" + gachadata.getItemStack().getType().toString() + "/" + gachadata.getItemStack().getItemMeta().getDisplayName() + ChatColor.RESET + "|" + gachadata.getItemStackAmount() + "|" + gachadata.getProbability() + "を削除しました");
		sender.sendMessage("/gacha saveでmysqlに保存してください");
	}
	private void Gacharemovems(CommandSender sender) {

		if(SeichiAssist.Companion.getMsgachadatalist().size() == 0){
			sender.sendMessage("MineStack用ガチャデータリストが空です");
			return;
		}
		int size = SeichiAssist.Companion.getMsgachadatalist().size();
		MineStackGachaData mg = SeichiAssist.Companion.getMsgachadatalist().get(size-1);
		SeichiAssist.Companion.getMsgachadatalist().remove(size-1);
		sender.sendMessage(size + "|" + mg.getLevel() + "|" + mg.getObjName() + "|" + mg.getItemStack().getType().toString() + "/" + mg.getItemStack().getItemMeta().getDisplayName() + ChatColor.RESET + "|" + mg.getAmount() + "|" + mg.getProbability() + "を削除しました");
		sender.sendMessage("/gacha savemsでmysqlに保存してください");
	}
	private void GachaEditAmount(CommandSender sender,int num,int amount) {
		if(num < 1 || SeichiAssist.Companion.getGachadatalist().size() < num){
			sender.sendMessage("アイテム番号が間違っているようです");
			return;
		}
		final GachaPrize editTarget = SeichiAssist.Companion.getGachadatalist().get(num - 1);

		editTarget.getItemStack().setAmount(amount);

		sender.sendMessage(num + "|" + editTarget.getItemStack().getType().toString() + "/" + editTarget.getItemStack().getItemMeta().getDisplayName() + ChatColor.RESET + "のアイテム数を" + amount + "個に変更しました");
	}
	private void GachaEditProbability(CommandSender sender,int num,double probability) {
		if(num < 1 || SeichiAssist.Companion.getGachadatalist().size() < num){
			sender.sendMessage("アイテム番号が間違っているようです");
			return;
		}
		GachaPrize gachadata = SeichiAssist.Companion.getGachadatalist().get(num-1);
		gachadata.setProbability(probability);
		SeichiAssist.Companion.getGachadatalist().set(num-1,gachadata);
		sender.sendMessage(num + "|" + gachadata.getItemStack().getType().toString() + "/" + gachadata.getItemStack().getItemMeta().getDisplayName() + ChatColor.RESET + "の確率を" + gachadata.getProbability() + "個に変更しました");
		sender.sendMessage("/gacha saveでmysqlに保存してください");
	}
	private void GachaMove(CommandSender sender,int num,int tonum) {
		if(num < 1 || SeichiAssist.Companion.getGachadatalist().size() < num){
			sender.sendMessage("アイテム番号が間違っているようです");
			return;
		}
		if(tonum < 1 || SeichiAssist.Companion.getGachadatalist().size() < tonum){
			sender.sendMessage("アイテム番号が間違っているようです");
			return;
		}
		GachaPrize gachadata = SeichiAssist.Companion.getGachadatalist().get(num-1);
		SeichiAssist.Companion.getGachadatalist().remove(num-1);
		SeichiAssist.Companion.getGachadatalist().add(tonum-1,gachadata);
		sender.sendMessage(num + "|" + gachadata.getItemStack().getType().toString() + "/" + gachadata.getItemStack().getItemMeta().getDisplayName() + ChatColor.RESET + "をリスト番号" + tonum + "番に移動しました");
		sender.sendMessage("/gacha saveでmysqlに保存してください");
	}
	private void Gachaclear(CommandSender sender) {
		SeichiAssist.Companion.getGachadatalist().clear();
		sender.sendMessage("すべて削除しました");
		sender.sendMessage("/gacha saveを実行するとmysqlのデータも全削除されます");
		sender.sendMessage("削除を取り消すには/gacha reloadコマンドを実行します");
	}
	private double runGachaDemo() {
		double sum = 1.0;
		double rand;

		rand = Math.random();

		for (GachaPrize gachadata : SeichiAssist.Companion.getGachadatalist()) {
			sum -= gachadata.getProbability();
			if (sum <= rand) {
				return gachadata.getProbability();
			}
		}
		return 1.0;
	}

}
