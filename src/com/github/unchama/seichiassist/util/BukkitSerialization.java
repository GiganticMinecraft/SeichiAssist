package com.github.unchama.seichiassist.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class BukkitSerialization {
    public static String toBase64(Inventory inventory) {
    	if(inventory == null){
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[四次元ポケットセーブ処理]でエラー発生");
			Bukkit.getLogger().warning("四次元ポケットのデータがnullです。開発者に報告してください");
    		return null;
    	}
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the inventory
            dataOutput.writeInt(inventory.getSize());

            // Save every element in the list
            for (int i = 0; i < inventory.getSize(); i++) {
                dataOutput.writeObject(inventory.getItem(i));
            }

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public static Inventory fromBase64(String data) throws IOException {
    	if(data.length() == 0|| data.equals(null)){
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[四次元ポケットロード処理]でエラー発生");
			Bukkit.getLogger().warning("四次元ポケットのデータがnullです。開発者に報告してください");
    		return null;
    	}
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt(),ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "4次元ポケット");

            // Read the serialized inventory
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, (ItemStack) dataInput.readObject());
            }
            dataInput.close();
            return inventory;
        } catch (ClassNotFoundException | IOException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
}
