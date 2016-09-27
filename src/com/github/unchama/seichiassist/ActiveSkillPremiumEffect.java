package com.github.unchama.seichiassist;

import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.unchama.seichiassist.arroweffect.ArrowBlizzardTaskRunnable;
import com.github.unchama.seichiassist.arroweffect.ArrowExplosionTaskRunnable;
import com.github.unchama.seichiassist.arroweffect.ArrowMeteoTaskRunnable;
import com.github.unchama.seichiassist.breakeffect.BlizzardTaskRunnable;
import com.github.unchama.seichiassist.breakeffect.ExplosionTaskRunnable;
import com.github.unchama.seichiassist.breakeffect.MeteoTaskRunnable;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;

public enum ActiveSkillPremiumEffect {

	MAGIC(1,"ef_magic","マジック","鶏が出る手品",10,Material.RED_ROSE),
	BLADE(2,"ef_blade","ブレイド","切り刻む",20,Material.IRON_SWORD),
	VLADMIA(3,"ef_vladmia","ブラッドミア","吸血する",30,Material.REDSTONE),
	TIAMAT(3,"ef_tiamat","ティアマト","彗星を落とす",40,Material.NETHER_STAR),
	;

	SeichiAssist plugin = SeichiAssist.plugin;

	private int num;
	private String sql_name;
	private String name;
	private String explain;
	private int usepoint;
	private Material material;

	ActiveSkillPremiumEffect(int num,String sql_name,String name,String explain,int usepoint,Material material){
		this.num = num;
		this.sql_name = sql_name;
		this.name = name;
		this.explain = explain;
		this.usepoint = usepoint;
		this.material = material;
	}

	public int getNum(){
        return this.num;
    }
	public String getsqlName(){
		return this.sql_name;
	}
	public String getName(){
		return this.name;
	}
	public String getExplain(){
		return this.explain;
	}
	public int getUsePoint(){
		return this.usepoint;
	}
	public Material getMaterial(){
		return this.material;
	}
	//プレイヤーが所持しているかどうか
	public Boolean isObtained(Map<Integer,Boolean> flagmap){
		return flagmap.get(getNum());
	}
	//獲得させる処理
	public void setObtained(Map<Integer,Boolean> flagmap) {
		flagmap.put(getNum(), true);
		return;
	}
	//エフェクトの実行処理分岐 範囲破壊と複数範囲破壊
	public void runBreakEffect(Player player,PlayerData playerdata,ItemStack tool,List<Block> breaklist,Coordinate start,Coordinate end,Location standard){
		switch(this.toString()){
		case "EXPLOSION":
			new ExplosionTaskRunnable(player,playerdata,tool,breaklist,start,end,standard).runTaskLater(plugin, 0);
			break;
		case "BLIZZARD":
			if(playerdata.activeskilldata.skillnum < 3){
				new BlizzardTaskRunnable(player,playerdata,tool,breaklist,start,end,standard).runTaskLater(plugin, 1);
			}else{
				new BlizzardTaskRunnable(player,playerdata,tool,breaklist,start,end,standard).runTaskLater(plugin, 10);
			}

			break;
		case "METEO":
			if(playerdata.activeskilldata.skillnum < 3){
				new MeteoTaskRunnable(player,playerdata,tool,breaklist,start,end,standard).runTaskLater(plugin, 1);
			}else{
				new MeteoTaskRunnable(player,playerdata,tool,breaklist,start,end,standard).runTaskLater(plugin, 10);
			}
			break;
		default :
			break;
		}
		return;
	}

	//エフェクトの実行処理分岐
	public void runArrowEffect(Player player){
		switch(this.toString()){
		case "EXPLOSION":
			new ArrowExplosionTaskRunnable(player).runTaskTimer(plugin,0,1);
			break;
		case "BLIZZARD":
			new ArrowBlizzardTaskRunnable(player).runTaskTimer(plugin,0,1);
			break;
		case "METEO":
			new ArrowMeteoTaskRunnable(player).runTaskTimer(plugin,0,1);
			break;
		default :
			break;
		}
		return;
	}


	public static String getNamebyNum(int effectnum) {
		ActiveSkillEffect[] skilleffect = ActiveSkillEffect.values();
		for(int i = 0 ; i < skilleffect.length ; i++){
			if(skilleffect[i].getNum() == effectnum){
				return skilleffect[i].getName();
			}
		}
		return "未設定";
	}
}
