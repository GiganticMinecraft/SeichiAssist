package com.github.unchama.seichiassist.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by karayuu on 2019/05/04
 */
class TypeConverterTest {
    @Test
    void toSecond() {
        final int tick = 800;
        assertEquals(40, TypeConverter.toSecond(tick));
    }

    @Test
    void toTimeString() {
        final int second = 987;
        assertEquals("16分", TypeConverter.toTimeString(second));
    }
}
