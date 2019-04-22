package com.github.unchama.util;

import com.github.unchama.util.collection.MapFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Monchi
 */
public class MapFactoryTest {
    @Test
    public void testEmptyMapFactory() {
        Map<String, String> expected = new HashMap<>();

        Map<String, String> actual = MapFactory.of();

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testNonemptyMapFactory() {
        Map<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");

        Map<String, String> actual = MapFactory.of(
                Pair.of("key1", "value1"),
                Pair.of("key2", "value2")
        );

        Assertions.assertEquals(expected, actual);
    }
}
