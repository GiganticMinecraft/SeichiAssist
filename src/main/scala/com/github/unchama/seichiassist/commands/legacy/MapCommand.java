package com.github.unchama.seichiassist.commands.legacy;

import com.github.unchama.seichiassist.SeichiAssist;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;

public final class MapCommand implements TabExecutor {
    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] args) {
        if (commandSender instanceof Player) {
            final Player who = (Player) commandSender;
            final Location where = who.getLocation();
            who.sendMessage("http://map-s" + SeichiAssist.seichiAssistConfig().getServerNum()
                    + ".minecraftserver.jp/?worldname=" + who.getWorld().getName()
                    + "&mapname=flat&zoom=2&x=" + where.getBlockX() + "&y=" + where.getBlockY() + "&z=" + where.getBlockZ());
            return true;
        }
        commandSender.sendMessage("このコマンドはゲーム内から実行してください。");
        return false;
    }

    @Override
    public List<String> onTabComplete(final CommandSender commandSender, final Command command, final String s, final String[] args) {
        return null;
    }
}
