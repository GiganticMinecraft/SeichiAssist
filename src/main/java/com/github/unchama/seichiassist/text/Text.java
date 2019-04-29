package com.github.unchama.seichiassist.text;

import org.bukkit.ChatColor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Stringのラッパー
 *
 * @author karayuu
 */
public class Text {
    @Nonnull
    private String string;

    private Text(@Nonnull final String string) {
        this.string = requireNonNull(string);
    }

    /**
     * {@link Text} を生成するstaticファクトリーメソッド
     *
     * @param string ラップする {@link String}
     * @return {@link Text}
     */
    public static Text of(@Nonnull final String string) {
        return new Text(string);
    }

    /**
     * {@link Text} を生成するstaticファクトリーメソッド
     *
     * @param string     ラップする {@link String}
     * @param chatColors 文字に色をつけたい時の {@link ChatColor}
     * @return {@link Text}
     */
    public static Text of(@Nonnull final String string, @Nonnull final ChatColor... chatColors) {
        Text text = Text.of(string);
        List<ChatColor> colors = Arrays.asList(chatColors);
        colors.forEach(color -> text.addBefore(color + ""));
        return text;
    }

    public static Text of() {
        return Text.of("");
    }

    /**
     * {@link StringBuilder} において,先頭に文字列を追加します.
     *
     * @param string 先頭に追加する文字列 ({@code null} は許容されない)
     */
    private void addBefore(@Nonnull final String string) {
        this.string = this.string + string;
    }

    /**
     * 元の {@link String} を取り出し(アンラップ)します.
     * @return アンラップした {@link String}
     */
    public String stringValue() {
        return this.string;
    }

    public static List<String> toStringList(@Nonnull List<Text> texts) {
        requireNonNull(texts);
        List<String> strings = new ArrayList<>();
        texts.forEach(text -> strings.add(text.stringValue()));
        return strings;
    }
}
