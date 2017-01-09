package com.github.unchama.seichiassist.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class SerializeItemList {
	public static String toBase64(List<ItemStack> items) {
		String serial = "";
		try {
			// List検査
			if (!items.isEmpty()) {
				// ByteArray出力ストリーム
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				// Object出力ストリーム
				BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

				// 要素数格納
				dataOutput.writeInt(items.size());
				// アイテム実態格納
				for (ItemStack item : items) {
					dataOutput.writeObject(item);
				}

				// ストリームを閉じる
				dataOutput.close();
				// 変換後のシリアルデータを取得
				serial = Base64Coder.encodeLines(outputStream.toByteArray());
			}
		} catch (Exception e) {
			e.printStackTrace();
			serial = "";
		}
		return serial;
	}

	public static List<ItemStack> fromBase64(String serial) {
		List<ItemStack> items = new ArrayList<ItemStack>();
		try {
			// String検査
			if ((serial.length() != 0) && (!serial.equals(null))) {
				// ByteArray入力ストリーム
				ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(serial));
				// Object入力ストリーム
				BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

				// 要素数格納
				int length = dataInput.readInt();
				// アイテム実態格納
				for (int i = 0; i < length; i++) {
					items.add((ItemStack) dataInput.readObject());
				}

				// ストリームを閉じる
				dataInput.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			items.clear();
		}
		return items;
	}
}
