package com.github.unchama.seichiassist;

import org.bukkit.potion.PotionEffectType;

public class EffectData {
	PotionEffectType potioneffecttype;
	int duration;//持続時間
	double amplifier;//強さ
	boolean originalflag;//このプラグイン独自の処理による追加エフェクト
	
	EffectData(){
		potioneffecttype = null;
		duration = 0;
		amplifier = 0;
	}

	//６０秒固定
	EffectData(PotionEffectType _potioneffecttype,double _amplifier){
		potioneffecttype = _potioneffecttype;
		originalflag = true;
		duration = 60;
		amplifier = _amplifier;
	}
	//持続時間を設定したい時
	EffectData(PotionEffectType _potioneffecttype,int _duration,double _amplifier){
		potioneffecttype = _potioneffecttype;
		duration = _duration;
		amplifier = _amplifier;
	}
	
	
	
	//最後のエフェクト合算時に使用する
	EffectData(int _duration,double _amplifier){
		duration = _duration;
		amplifier = _amplifier;
	}
}
