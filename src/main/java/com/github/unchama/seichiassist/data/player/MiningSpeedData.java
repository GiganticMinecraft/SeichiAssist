package com.github.unchama.seichiassist.data.player;

import com.github.unchama.seichiassist.data.inventory.slot.button.Button;
import com.github.unchama.seichiassist.text.Templates;
import com.github.unchama.seichiassist.text.Text;
import com.github.unchama.seichiassist.util.list.CircularList;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 採掘速度のプレイヤーデータ
 * Created by karayuu on 2019/05/06
 */
public class MiningSpeedData {
    /**
     * データを保持する {@link Player}
     */
    @NotNull
    public final Player player;

    /**
     * {@link Player} の採掘速度
     */
    public MiningSpeed miningSpeed = MiningSpeed.ON;

    public MiningSpeedData(@NotNull final Player player) {
        this.player = player;
    }

    /**
     * {@link MiningSpeed} を次に更新し, {@link Player} にお知らせします.
     */
    public void toNextAndNotifyPlayer() {
        this.miningSpeed = this.miningSpeed.getNext();
        player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
        player.sendMessage(ChatColor.GREEN + "採掘速度上昇効果：" + this.miningSpeed.getDescription());
    }

    /**
     * {@link Button} に使用する説明文を取得します.
     *
     * @return 説明文
     */
    @NotNull
    public List<Text> menuDescrpition() {
        List<Text> descriptions = new ArrayList<>();
        if (miningSpeed == MiningSpeed.OFF) {
            descriptions.add(Text.of("現在OFFです", ChatColor.RED));
            descriptions.add(Text.of("クリックで" + this.miningSpeed.getNext().description,
                ChatColor.UNDERLINE, ChatColor.DARK_GREEN));
        } else {
            descriptions.add(Text.of("現在有効です", ChatColor.GREEN)
                                .also(Text.of(this.miningSpeed.description, ChatColor.GREEN)));
            descriptions.add(Text.of("クリックで" + this.miningSpeed.getNext().description,
                ChatColor.UNDERLINE, ChatColor.DARK_RED));
        }
        descriptions.addAll(Templates.miningSpeedDescription);
        return descriptions;
    }

    /**
     * 採掘速度制限を管理するenumです.
     */
    public enum MiningSpeed {
        ON(25565, 0, "ON(無制限)"),
        LV1(127, 1, "ON(127制限)"),
        LV2(200, 2, "ON(200制限)"),
        LV3(400, 3, "ON(400制限)"),
        LV4(600, 4, "ON(600制限)"),
        OFF(0, 5, "OFF");

        /**
         * Minecraft内の採掘速度
         */
        final int mininSpeed;

        /**
         * SQLの区別用識別番号です.
         */
        final int sqlIdentifier;

        /**
         * 説明文用 {@link String}
         */
        @NotNull
        final String description;

        MiningSpeed(final int speed, final int sqlIdentifier, @NotNull final String description) {
            this.mininSpeed = speed;
            this.sqlIdentifier = sqlIdentifier;
            this.description = description;
        }

        /**
         * この {@link MiningSpeed} の次の {@link MiningSpeed} の要素を取得します.
         *
         * @return 次の {@link MiningSpeed}
         */
        public MiningSpeed getNext() {
            final CircularList<MiningSpeed> speedList = new CircularList<>(Arrays.asList(MiningSpeed.values()));
            final int index = speedList.indexOf(this);
            return speedList.get(index + 1);
        }

        /**
         * 説明文を取得します.
         *
         * @return 説明文
         */
        @NotNull
        public String getDescription() {
            return this.description;
        }

        /**
         * SQL保存・読み込み時の識別子を取得します.
         *
         * @return 識別子
         */
        public int getSqlIdentifier() {
            return this.sqlIdentifier;
        }
    }
}
