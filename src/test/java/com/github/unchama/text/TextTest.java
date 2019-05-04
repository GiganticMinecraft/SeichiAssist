package com.github.unchama.text;

import com.github.unchama.seichiassist.text.Text;
import org.bukkit.ChatColor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by karayuu on 2019/05/04
 */
public class TextTest {
    @Test
    public void textTest() {
        Text text = Text.of("test", ChatColor.YELLOW);
        assertEquals(ChatColor.RESET + "" + ChatColor.YELLOW + "test", text.stringValue());
    }

    @Test
    public void addbeforeTest() {
        String base = "hoge";
        String before = "test";

        Text text = Text.of(base);
        text.addBefore(before);

        assertEquals(before + base, text.stringValue());
    }
}
