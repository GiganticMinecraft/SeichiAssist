package com.github.unchama.seichiassist;

import org.bukkit.potion.PotionEffectType;

public class EffectData {
	PotionEffectType potioneffecttype;
	int duration;//持続時間
	double amplifier;//強さ
	String string;//メッセージの内容

	EffectData(){
		potioneffecttype = null;
		duration = 0;
		amplifier = 0;
		string = null;
	}

	//６０秒固定採掘速度固定
	EffectData(double _amplifier,String _string){
		potioneffecttype = PotionEffectType.FAST_DIGGING;
		duration = 1200;
		amplifier = _amplifier;
		string = _string;

	}
	EffectData(int _duration,double _amplifier,String _string){
		potioneffecttype = PotionEffectType.FAST_DIGGING;
		duration = _duration;
		amplifier = _amplifier;
		string = _string;

	}
}
