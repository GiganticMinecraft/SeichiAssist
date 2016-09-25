package com.github.unchama.seichiassist.util;



import java.util.ArrayList;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.inventivetalent.particle.ParticleEffect;

import com.github.unchama.seichiassist.data.Coordinate;

public class EffectUtil {

	public static void playEffectCircle(Player player, Effect effect,
			int radius) {
		Location ploc = player.getLocation();

		for(int degree = 0;degree < 360;degree += 30){
			Location efloc = ploc.clone().add(radius * Math.cos(Math.toRadians(degree)),0,radius * Math.sin(Math.toRadians(degree)));
			player.getWorld().playEffect(efloc, effect, 1);
		}

	}

	public static void playEffectCube(Player player,Location efloc,ParticleEffect pe, Coordinate start,
			Coordinate end , Color c) {
		List<Player> pl = new ArrayList<Player>();
		pl.add(player);
		 double d = 0.5;
		for(double x = start.x ; x <= end.x + 1 ; x += d){
			pe.sendColor(pl, efloc.clone().add(x,start.y,start.z),c);
			pe.sendColor(pl, efloc.clone().add(x,start.y,end.z+1),c);
			pe.sendColor(pl, efloc.clone().add(x,end.y+1,start.z),c);
			pe.sendColor(pl, efloc.clone().add(x,end.y+1,end.z+1),c);
		}
		for(double y = start.y ; y <= end.y + 1 ; y += d){
			pe.sendColor(pl, efloc.clone().add(start.x,y,start.z),c);
			pe.sendColor(pl, efloc.clone().add(start.x,y,end.z+1),c);
			pe.sendColor(pl, efloc.clone().add(end.x+1,y,start.z),c);
			pe.sendColor(pl, efloc.clone().add(end.x+1,y,end.z+1),c);
		}
		for(double z = start.z ; z <= end.z + 1 ; z += d){
			pe.sendColor(pl, efloc.clone().add(start.x,start.y,z),c);
			pe.sendColor(pl, efloc.clone().add(start.x,end.y+1,z),c);
			pe.sendColor(pl, efloc.clone().add(end.x+1,start.y,z),c);
			pe.sendColor(pl, efloc.clone().add(end.x+1,end.y+1,z),c);
		}
	}

}
