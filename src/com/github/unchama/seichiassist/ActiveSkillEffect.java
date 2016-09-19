package com.github.unchama.seichiassist;

import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.github.unchama.seichiassist.data.Coordinate;

public enum ActiveSkillEffect {
	EXPLOSION(1,"ef_explosion",ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エクスプロージョン","単純な爆発のエフェクト",10,Material.TNT),
	BLIZZARD(2,"ef_blizzard",ChatColor.AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ブリザード","凍らせるエフェクト",20,Material.PACKED_ICE),
	METEO(3,"ef_meteo",ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "メテオ","隕石を落とすエフェクト",30,Material.FIREBALL),
	;

	private int num;
	private String sql_name;
	private String name;
	private String explain;
	private int usepoint;
	private Material material;

	ActiveSkillEffect(int num,String sql_name,String name,String explain,int usepoint,Material material){
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
	//エフェクトの実行処理分岐
	public void runBreakEffect(List<Block> breaklist,Coordinate start,Coordinate end,Location standard){
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
	public void runMultiEffect(List<List<Block> > multibreaklist,List<Coordinate> startlist,List<Coordinate> endlist,Location standard){
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
	public void runArrowEffect(List<Block> breaklist,Coordinate start,Coordinate end,Location standard){
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
	public void runCondensEffect(List<Block> breaklist,Coordinate start,Coordinate end,Location standard){
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
