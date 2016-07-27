package com.github.unchama.seichiassist.data;

import org.bukkit.potion.PotionEffectType;

public class EffectData {
	public PotionEffectType potioneffecttype;
	public int duration;//持続時間
	public double amplifier;//強さ
	public String string;//メッセージの内容

	EffectData(){
		potioneffecttype = null;
		duration = 0;
		amplifier = 0;
		string = null;
	}

	//６０秒固定採掘速度固定
	public EffectData(double _amplifier,String _string){
		potioneffecttype = PotionEffectType.FAST_DIGGING;
		duration = 1200;
		amplifier = _amplifier;
		string = _string;

	}
	public EffectData(int _duration,double _amplifier,String _string){
		potioneffecttype = PotionEffectType.FAST_DIGGING;
		duration = _duration;
		amplifier = _amplifier;
		string = _string;

	}
}
