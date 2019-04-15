package com.github.unchama.seichiassist.data;

import com.github.unchama.seichiassist.util.TypeConverter;
import org.bukkit.potion.PotionEffectType;

public class EffectData {
	public PotionEffectType potioneffecttype;
	public int duration;//持続時間
	public double amplifier;//強さ
	public int id;//上昇値の種類
	/*
	 * id=0 不明な上昇値
	 * id=1 接続人数から
	 * id=2 採掘量から
	 * id=3 ドラゲナイタイムから
	 * id=4 投票から
	 * id=5 コマンド入力から(イベントや不具合等)
	 */

	EffectData(){
		potioneffecttype = null;
		duration = 0;
		amplifier = 0;
		id = 0;
	}

	//６０秒固定採掘速度固定
	public EffectData(double _amplifier,int _id){
		potioneffecttype = PotionEffectType.FAST_DIGGING;
		duration = 1260;
		amplifier = _amplifier;
		id = _id;

	}
	public EffectData(int _duration,double _amplifier,int _id){
		potioneffecttype = PotionEffectType.FAST_DIGGING;
		duration = _duration;
		amplifier = _amplifier;
		id = _id;
	}

	public String EDtoString(int id,int _duration,double _amplifier){
		if(id == 0){
			return "+" + _amplifier +  " 不明な上昇値_" + TypeConverter.toTimeString(_duration/20);
		}else if(id == 1){
			return "+" + _amplifier +  " 接続人数から";
		}else if(id == 2){
			return "+" + _amplifier +  " 整地量から";
		}else if(id == 3){
			return "+" + _amplifier +  " ﾄﾞﾗｹﾞﾅｲﾀｲﾑから_" + TypeConverter.toTimeString(_duration/20);
		}else if(id == 4){
			return "+" + _amplifier +  " 投票ボーナスから_" + TypeConverter.toTimeString(_duration/20);
		}else if(id == 5){
			return "+" + _amplifier +  " コマンド入力から_" + TypeConverter.toTimeString(_duration/20);
		}
		return "+" + _amplifier +  " 不明な上昇値_" + TypeConverter.toTimeString(_duration/20);
	}
}
