package com.github.unchama.seichiassist;

import org.bukkit.potion.PotionEffectType;

public class EffectData {
	PotionEffectType potioneffecttype;
	int duration;//持続時間
	double amplifier;//強さ
	
	EffectData(){
		potioneffecttype = null;
		duration = 0;
		amplifier = 0;
	}
	EffectData(PotionEffectType _potioneffecttype,int _duration,double _amplifier){
		potioneffecttype = _potioneffecttype;
		duration = _duration;
		amplifier = _amplifier;
	}
}
