package com.github.unchama.seichiassist;


public enum ActiveSkill{
	DUALBREAK(1,"デュアルブレイク"),
	TRIALBREAK(2,"トリアルブレイク"),
	EXPLOSION(3,"エクスプロージョン"),
	THUNDERSTORM(4,"サンダーストーム"),
	ILLUSION(5,"イリュージョン"),
	METEO(6,"メテオ"),
	GRAVITY(7,"グラビティ"),
	;

	private int num;
	private String name;

	ActiveSkill(int num,String name){
		this.num = num;
		this.name = name;
	}

	public int getNum() {
        return this.num;
    }
	private String getName() {
		return this.name;
	}
	public static String getStringByNum(int num) {
	    // 列挙定数を取得
		ActiveSkill[] activeskill = ActiveSkill.values();
	    // 列挙定数の位置（num）を指定してラベルを取得
	    return activeskill[num-1].getName();
	}


}
