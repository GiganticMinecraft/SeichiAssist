package com.github.unchama.seichiassist;



public enum ActiveSkill{
	ARROW(1,"","","","エビフライ・ドライブ","ホーリー・ショット","ツァーリ・ボンバ","アーク・ブラスト","ファンタズム・レイ","スーパー・ノヴァ"),
	MULTI(2,"","","","トム・ボウイ","サンダー・ストーム","スターライト・ブレイカー","アース・ディバイド","ヘブン・ゲイボルグ","ディシジョン"),
	BREAK(3,"デュアル・ブレイク","トリアル・ブレイク","エクスプロージョン","ミラージュ・フレア","ドッ・カーン","ゲガンティック・ボム","ブリリアント・デトネーション","レムリア・インパクト","エターナル・ヴァイス"),
	CONDENSE(4,"","","","ホワイト・ブレス","アブソリュート・ゼロ","ダイアモンド・ダスト","ラヴァ・コンデンセーション","モエラキ・ボールダーズ","エルト・フェットル"),
	;

	private int typenum;
	private String lv1name;
	private String lv2name;
	private String lv3name;
	private String lv4name;
	private String lv5name;
	private String lv6name;
	private String lv7name;
	private String lv8name;
	private String lv9name;


	ActiveSkill(int typenum,String lv1name,String lv2name,String lv3name,String lv4name,String lv5name,String lv6name,String lv7name,String lv8name,String lv9name){
		this.typenum = typenum;
		this.lv1name = lv1name;
		this.lv2name = lv2name;
		this.lv3name = lv3name;
		this.lv4name = lv4name;
		this.lv5name = lv5name;
		this.lv6name = lv6name;
		this.lv7name = lv7name;
		this.lv8name = lv8name;
		this.lv9name = lv9name;

	}

	public int gettypenum() {
        return this.typenum;
    }
	public String getLv1Name(){
		return this.lv1name;
	}
	public String getLv2Name(){
		return this.lv2name;
	}
	public String getLv3Name(){
		return this.lv3name;
	}
	public String getLv4Name(){
		return this.lv4name;
	}
	public String getLv5Name(){
		return this.lv5name;
	}
	public String getLv6Name(){
		return this.lv6name;
	}
	public String getLv7Name(){
		return this.lv7name;
	}
	public String getLv8Name(){
		return this.lv8name;
	}
	public String getLv9Name(){
		return this.lv9name;
	}
	public static String getActiveSkillName(int typenum,int skilllevel) {
		// 列挙定数を取得
		ActiveSkill[] activeskill = ActiveSkill.values();
		String str;
		if(typenum == 0){
			str = "未設定";
			return str;
		}
		switch(skilllevel){
		case 0:
			str = "未設定";
			break;
		case 1:
			str = activeskill[typenum-1].getLv1Name();
			break;
		case 2:
			str = activeskill[typenum-1].getLv2Name();
			break;
		case 3:
			str = activeskill[typenum-1].getLv3Name();
			break;
		case 4:
			str = activeskill[typenum-1].getLv4Name();
			break;
		case 5:
			str = activeskill[typenum-1].getLv5Name();
			break;
		case 6:
			str = activeskill[typenum-1].getLv6Name();
			break;
		case 7:
			str = activeskill[typenum-1].getLv7Name();
			break;
		case 8:
			str = activeskill[typenum-1].getLv8Name();
			break;
		case 9:
			str = activeskill[typenum-1].getLv9Name();
			break;
		default:
			str = "エラー";
			break;
		}
		return str;

	}

	public static int getActiveSkillUseExp(int typenum,int skilllevel) {
		int exp = 0;
		if(typenum == ActiveSkill.ARROW.gettypenum()){
			switch(skilllevel){
			case 4:
				exp = 18;
				break;
			case 5:
				exp = 35;
				break;
			case 6:
				exp = 80;
				break;
			case 7:
				exp = 110;
				break;
			case 8:
				exp = 220;
				break;
			case 9:
				exp = 380;
				break;
			default:
				break;
			}
		}else if(typenum == ActiveSkill.MULTI.gettypenum()){
			switch(skilllevel){
			case 4:
				exp = 28;
				break;
			case 5:
				exp = 65;
				break;
			case 6:
				exp = 90;
				break;
			case 7:
				exp = 185;
				break;
			case 8:
				exp = 330;
				break;
			case 9:
				exp = 480;
				break;
			default:
				break;
			}
		}else if(typenum == ActiveSkill.BREAK.gettypenum()){
			switch(skilllevel){
			case 1:
				exp = 1;
				break;
			case 2:
				exp = 3;
				break;
			case 3:
				exp = 15;
				break;
			case 4:
				exp = 30;
				break;
			case 5:
				exp = 70;
				break;
			case 6:
				exp = 100;
				break;
			case 7:
				exp = 200;
				break;
			case 8:
				exp = 350;
				break;
			case 9:
				exp = 500;
				break;
			default:
				break;
			}
		}else if(typenum == ActiveSkill.CONDENSE.gettypenum()){
			switch(skilllevel){
			case 4:
				exp = 25;
				break;
			case 5:
				exp = 50;
				break;
			case 6:
				exp = 100;
				break;
			case 7:
				exp = 60;
				break;
			case 8:
				exp = 120;
				break;
			case 9:
				exp = 240;
				break;
			default:
				break;
			}
		}
		return exp;
	}
















}
