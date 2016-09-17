package com.github.unchama.seichiassist;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.github.unchama.seichiassist.data.ActiveSkillData;
import com.github.unchama.seichiassist.data.Coordinate;

public enum ActiveSkillEffect {
	EXPLOSION(1,ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エクスプロージョン","単純な爆発のエフェクト",10,Material.TNT),
	BLIZZARD(2,ChatColor.AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ブリザード","凍らせるエフェクト",20,Material.PACKED_ICE),
	METEO(3,ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "メテオ","隕石を落とすエフェクト",30,Material.FIREBALL),
	;

	private int typenum;
	private String name;
	private String explain;
	private int usepoint;
	private Material material;

	ActiveSkillEffect(int typenum,String name,String explain,int usepoint,Material material){
		this.typenum = typenum;
		this.name = name;
		this.explain = explain;
		this.usepoint = usepoint;
		this.material = material;
	}

	public int gettypenum(){
        return this.typenum;
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
	public Boolean isObtained(ActiveSkillData activeskilldata){
		boolean flag = false;
		switch(this.toString()){
		case "EXPLOSION":
			flag = activeskilldata.effect_explosion;
			break;
		case "BLIZZARD":
			flag = activeskilldata.effect_blizzard;
			break;
		case "METEO":
			flag = activeskilldata.effect_meteo;
			break;
		default :
			flag = false;
		}
		return flag;
	}
	//獲得させる処理
	public void setObtained(ActiveSkillData activeskilldata) {
		switch(this.toString()){
		case "EXPLOSION":
			activeskilldata.effect_explosion = true;
			break;
		case "BLIZZARD":
			activeskilldata.effect_blizzard = true;
			break;
		case "METEO":
			activeskilldata.effect_meteo = true;
			break;
		default :
			break;
		}
		return;
	}
	//エフェクトの実行処理分岐
	public void runBreakEffect(List<Block> breaklist,Coordinate start,Coordinate end){
		switch(this.toString()){
		case "EXPLOSION":

			break;
		case "BLIZZARD":

			break;
		case "METEO":

			break;
		default :
			break;
		}
		return;
	}
	//エフェクトの実行処理分岐
	public void runMultiEffect(List<List<Block> > multibreaklist,List<Coordinate> startlist,List<Coordinate> endlist){
		switch(this.toString()){
		case "EXPLOSION":

			break;
		case "BLIZZARD":

			break;
		case "METEO":

			break;
		default :
			break;
		}
		return;
	}

	//エフェクトの実行処理分岐
	public void runArrowEffect(List<Block> breaklist,Coordinate start,Coordinate end){
		switch(this.toString()){
		case "EXPLOSION":

			break;
		case "BLIZZARD":

			break;
		case "METEO":

			break;
		default :
			break;
		}
		return;
	}
	//エフェクトの実行処理分岐
	public void runCondensEffect(List<Block> breaklist,Coordinate start,Coordinate end){
		switch(this.toString()){
		case "EXPLOSION":

			break;
		case "BLIZZARD":

			break;
		case "METEO":

			break;
		default :
			break;
		}
		return;
	}
}
