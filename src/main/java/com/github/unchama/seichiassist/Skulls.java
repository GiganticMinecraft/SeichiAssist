package com.github.unchama.seichiassist;

import java.util.UUID;

/**
 * 頭の一覧をenum化したもの。
 */
public enum Skulls {
	/**
	 * unchama
	 */
	UNCHAMA("b66cc3f6-a045-42ad-b4b8-320f20caf140"),
	/**
	 * MHF_Villager
	 */
	VILLAGER("bd482739-767c-45dc-a1f8-c33c40530952"),
	/**
	 * MHF_ArrowRight
	 */
	ARROW_RIGHT("50c8510b-5ea0-4d60-be9a-7d542d6cd156"),
	/**
	 * MHF_ArrowLeft
	 */
	ARROW_LEFT("a68f0b64-8d14-4000-a95f-4b9ba14f8df9"),
	/**
	 * MHF_ArrowUp
	 */
	ARROW_UP("fef039ef-e6cd-4987-9c84-26a3e6134277"),
	/**
	 * MHF_ArrowDown
	 */
	ARROW_DOWN("68f59b9b-5b0b-4b05-a9f2-e1d1405aa348"),
	/**
	 * MHF_Present2
	 */
	PRESENT2("f1eb7cad-e2c0-4e9e-8aad-1eae21d5fd95"),
	;

	private final UUID uuid;

	Skulls(String uuid) {
		this.uuid = UUID.fromString(uuid);
	}

	public UUID getUuid() {
		return uuid;
	}
}
