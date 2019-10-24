package com.github.unchama.seichiassist.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ItemListSerialization {
    private ItemListSerialization() {

    }

    public static @Nullable String serializeToBase64(List<ItemStack> items) {
        if (items.isEmpty()) {
            return "";
        }

        try {
            // List検査
            // ByteArray出力ストリーム
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // Object出力ストリーム
            try (BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
                // 要素数格納
                dataOutput.writeInt(items.size());
                // アイテム実態格納
                for (ItemStack item : items) {
                    dataOutput.writeObject(item);
                }
            }
            // 変換後のシリアルデータを取得
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (ClassCastException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<ItemStack> deserializeFromBase64(String serial) {
        List<ItemStack> items = new ArrayList<>();
        try {
            // String検査
            if (serial != null && serial.length() != 0) {
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
        } catch (ClassCastException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
            items.clear();
        }
        return items;
    }
}
