package com.github.unchama.seichiassist;

public class MebiusTalk {
	private final String mebiusSerif;
	private final String playerSerif;

	public MebiusTalk(String mebiusSerif, String playerSerif) {

		this.mebiusSerif = mebiusSerif;
		this.playerSerif = playerSerif;
	}

	public String getPlayerSerif() {
		return playerSerif;
	}

	public String getMebiusSerif() {
		return mebiusSerif;
	}

	/**
	 * 従来のList&lt;String&gt;と互換性を保つためのメソッド。
	 * @param i インデックス
	 * @return メッセージ
	 */
	@Deprecated
	public String get(int i) {
		if (i == 0) {
			return getMebiusSerif();
		} else if (i == 1) {
			return getPlayerSerif();
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MebiusTalk) {
			return ((MebiusTalk) obj).mebiusSerif.equals(this.mebiusSerif) && ((MebiusTalk) obj).playerSerif.equals(this.playerSerif);
		} else {
			return false;
		}
	}
}
