package com.github.unchama.util;

import com.github.unchama.util.collection.SetFactory;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Monchi
 */
public class SetFactoryTest {
    @Test
    public void testEmptySetFactory() {
        Set<String> expected = new HashSet<>();

        Set<String> actual = SetFactory.of();

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testNonemptySetFactory() {
        Set<String> expected = new HashSet<>();
        expected.add("value1");
        expected.add("value2");

        Set<String> actual = SetFactory.of("value1", "value2");

        Assertions.assertEquals(expected, actual);
    }
}
