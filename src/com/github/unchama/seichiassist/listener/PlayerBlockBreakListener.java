package com.github.unchama.seichiassist.listener;

import net.coreprotect.CoreProtectAPI;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.github.unchama.seichiassist.Util;

public class PlayerBlockBreakListener implements Listener {
	//ブロックが壊された時に実行
	@EventHandler
	public void onPlayerBlockBreakEvent(BlockBreakEvent event){
		Player player = event.getPlayer();
		Block block = event.getBlock();
		CoreProtectAPI CoreProtect = Util.getCoreProtect();

		event.setExpToDrop(10);
		Location loc = block.getLocation();
		for(double y = loc.getY()-1.0;y<=loc.getY()+1;y++){
			for(double x = loc.getX()-1.0;x<=loc.getX()+1;x++){
				for(double z = loc.getZ() -1.0;z<=loc.getZ()+1;z++){
					Location breakloc = new Location(loc.getWorld(), x, y, z);
					Block breakblock = breakloc.getBlock();
					if(breakblock.getType().equals(Material.STONE)){
						breakblock.breakNaturally();
						breakblock.getWorld().playEffect(breakloc, Effect.STEP_SOUND,Material.STONE);
						@SuppressWarnings("deprecation")
						boolean success = CoreProtect.logRemoval(player.getName().toLowerCase(), breakloc, breakblock.getType(),breakblock.getData());
						if(success = false){
							player.sendMessage("保存に失敗しました。");
						}else{
							player.sendMessage("保存");
						}
					}
				}
			}
		}


	}
}
