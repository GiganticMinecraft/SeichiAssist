package com.github.unchama.seichiassist.text;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Stringのラッパー
 *
 * @author karayuu
 */
public class Text {
    @NotNull
    private String string;

    private Text(@NotNull final String string) {
        this.string = string;
    }

    /**
     * {@link Text} を生成するstaticファクトリーメソッド
     *
     * @param string ラップする {@link String}
     * @return {@link Text}
     */
    @NotNull
    public static Text of(@NotNull final String string) {
        return new Text(string);
    }

    /**
     * {@link Text} を生成するstaticファクトリーメソッド
     *
     * @param string     ラップする {@link String}
     * @param chatColors 文字に色をつけたい時の {@link ChatColor} <br>
     *                   Minecraftの仕様上, {@link ChatColor#UNDERLINE} 等の文字修飾コードは初めに指定してください.
     * @return {@link Text}
     */
    @NotNull
    public static Text of(@NotNull final String string, @NotNull final ChatColor... chatColors) {
        Text text = Text.of(string);
        List<ChatColor> colors = Arrays.asList(chatColors);
        colors.forEach(color -> text.addBefore(color + ""));
        text.addBefore(ChatColor.RESET + "");
        return text;
    }

    @NotNull
    public static Text of() {
        return Text.of("");
    }

    /**
     * {@link Text} を連結させて新しい {@link Text} を作成します.
     *
     * @param another_text 連結させる {@link Text} ({@code null} は許容されません.)
     * @return 新しい {@link Text}
     */
    @NotNull
    public Text also(@NotNull Text another_text) {
        return Text.of(this.string + another_text.stringValue());
    }

    /**
     * {@link StringBuilder} において,先頭に文字列を追加します.
     *
     * @param string 先頭に追加する文字列 ({@code null} は許容されません.)
     */
    public void addBefore(@NotNull final String string) {
        this.string = string + this.string;
    }

    /**
     * 元の {@link String} を取り出し(アンラップ)します.
     * @return アンラップした {@link String}
     */
    @NotNull
    public String stringValue() {
        return this.string;
    }

    @NotNull
    public static List<String> toStringList(@NotNull List<Text> texts) {
        List<String> strings = new ArrayList<>();
        texts.forEach(text -> strings.add(text.stringValue()));
        return strings;
    }

    @Override
    public String toString() {
        return stringValue();
    }
}
