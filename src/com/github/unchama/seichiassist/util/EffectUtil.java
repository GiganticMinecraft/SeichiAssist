package com.github.unchama.seichiassist.util;



import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EffectUtil {

	public static void playEffectCircle(Player player, Effect effect,
			int radius) {
		Location ploc = player.getLocation();

		for(int degree = 0;degree < 360;degree += 30){
			Location efloc = ploc.clone().add(radius * Math.cos(Math.toRadians(degree)),0,radius * Math.sin(Math.toRadians(degree)));
			player.getWorld().playEffect(efloc, effect, 1);
		}

	}

	public static void playEffectSquare(Player player, Effect effect,
			double length) {
		Location ploc = player.getLocation();

		for(double i = -length ; i < length ; i += 2){
			player.getWorld().playEffect(ploc.clone().add(length,0,i), effect, 1);
			player.getWorld().playEffect(ploc.clone().add(-length,0,i), effect, 1);
			player.getWorld().playEffect(ploc.clone().add(i,0,length), effect, 1);
			player.getWorld().playEffect(ploc.clone().add(i,0,-length), effect, 1);
		}

	}

}
