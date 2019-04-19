package com.github.unchama.seichiassist.commands;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author unicroak
 * TODO: Implement CommandExecutor instead of TabExecutor
 */
public class EffectCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはゲーム内から実行してください。");

            return true;
        }

        final Player player = (Player) sender;
        final PlayerData playerData = SeichiAssist.playermap.get(player.getUniqueId());

        if (args.length == 0) {
            final int effectFlag = (playerData.effectflag + 1) % 6;

            final String changedFlagMessage;
            switch (effectFlag) {
                case 0:
                    changedFlagMessage = "採掘速度上昇効果:ON(無制限)";
                    break;
                case 1:
                    changedFlagMessage = "採掘速度上昇効果:ON(127制限)";
                    break;
                case 2:
                    changedFlagMessage = "採掘速度上昇効果:ON(200制限)";
                    break;
                case 3:
                    changedFlagMessage = "採掘速度上昇効果:ON(400制限)";
                    break;
                case 4:
                    changedFlagMessage = "採掘速度上昇効果:ON(600制限)";
                    break;
                default:
                    changedFlagMessage = "採掘速度上昇効果:OFF";
            }

            sender.sendMessage(ChatColor.GREEN + changedFlagMessage);
            sender.sendMessage(ChatColor.GREEN + "再度コマンドを実行することでトグルします。");

            playerData.effectflag = effectFlag;

            return true;
        }

        final String commandType = args[0];
        switch (commandType.toUpperCase()) {
            case "SMART":
                final boolean messageFlag = !playerData.messageflag;
                if (messageFlag) {
                    sender.sendMessage(ChatColor.GREEN + "内訳表示:ON(OFFに戻したい時は再度コマンドを実行します。)");
                } else {
                    sender.sendMessage(ChatColor.GREEN + "内訳表示:OFF");
                }

                playerData.messageflag = messageFlag;

                return true;
            case "DEMO":
                final SimulatedGachaResult simulateResult = simulateGacha(1000);

                sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "ガチャ券" + simulateResult.trialCount + "回試行結果\n"
                        + ChatColor.RESET + "ギガンティック：" + simulateResult.giganticCount + "回(" + simulateResult.calcGiganticRate() + "%)\n"
                        + "大当たり：" + simulateResult.bigCount + "回(" + simulateResult.calcBigRate() + "%)\n"
                        + "当たり：" + simulateResult.regularCount + "回(" + simulateResult.calcRegularRate() + "%)\n"
                        + "ハズレ：" + simulateResult.potatoCount + "回(" + simulateResult.calcPotatoRate() + "%)\n"
                );

                return true;
        }

        return false;
    }

    private static SimulatedGachaResult simulateGacha(int trialCount) {
        int gigantic = 0;
        int big = 0;
        int regular = 0;
        int potato = 0;

        for (int i = 0; i < trialCount; i++) {
            double rawResult = runGachaDemo();

            if (rawResult < 0.001) {
                gigantic++;
            } else if (rawResult < 0.01) {
                big++;
            } else if (rawResult < 0.1) {
                regular++;
            } else {
                potato++;
            }
        }

        return new SimulatedGachaResult(trialCount, gigantic, big, regular, potato);
    }

    private static double runGachaDemo() {
        double sum = 1.0;
        final double rand = Math.random();

        for (GachaData gachadata : SeichiAssist.gachadatalist) {
            sum -= gachadata.probability;

            if (sum <= rand) return gachadata.probability;
        }

        return 1.0;
    }

    @Override
    public List<String> onTabComplete(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
        return null; // :(
    }

    // TODO: shouldn't exist here
    private static class SimulatedGachaResult {

        private final int trialCount;
        private final int giganticCount;
        private final int bigCount;
        private final int regularCount;
        private final int potatoCount;

        private SimulatedGachaResult(int trialCount, int giganticCount, int bigCount, int regularCount, int potatoCount) {
            this.trialCount = trialCount;
            this.giganticCount = giganticCount;
            this.bigCount = bigCount;
            this.regularCount = regularCount;
            this.potatoCount = potatoCount;
        }

        double calcGiganticRate() {
            return giganticCount / 100.0 * trialCount;
        }

        double calcBigRate() {
            return bigCount / 100.0 * trialCount;
        }

        double calcRegularRate() {
            return regularCount / 100.0 * trialCount;
        }

        double calcPotatoRate() {
            return potatoCount / 100.0 * trialCount;
        }

    }

}
