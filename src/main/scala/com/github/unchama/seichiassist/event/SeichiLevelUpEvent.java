package com.github.unchama.seichiassist.event;

/**
 * Created by karayuu on 2018/04/18
 */

import com.github.unchama.seichiassist.data.player.PlayerData;
import org.bukkit.entity.Player;

/**
 * 整地レベルが上がった際に呼び出されるEventです。
 */
public class SeichiLevelUpEvent extends CustomEvent {
    /**
     * プレイヤー
     */
    private Player player;
    /**
     * プレイヤーデータ
     */
    private PlayerData playerData;
    /**
     * レベルアップ後のレベル
     */
    private int levelAfterLevelUp;


    /**
     * コンストラクタ
     *
     * @param player            該当プレイヤー
     * @param playerData        該当プレイヤーのプレイヤーデータ
     * @param levelAfterLevelUp レベルアップ後(現在の)レベル
     */
    public SeichiLevelUpEvent(Player player, PlayerData playerData, int levelAfterLevelUp) {
        this.player = player;
        this.playerData = playerData;
        this.levelAfterLevelUp = levelAfterLevelUp;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    /**
     * レベルアップ後の(現在の)レベルを取得します
     *
     * @return レベルアップ後の(現在の)レベル
     */
    public int getLevelAfterLevelUp() {
        return levelAfterLevelUp;
    }
}
