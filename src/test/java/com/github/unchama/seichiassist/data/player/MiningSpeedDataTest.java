package com.github.unchama.seichiassist.data.player;

import org.junit.Test;

import com.github.unchama.seichiassist.data.player.MiningSpeedData.MiningSpeed;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by karayuu on 2019/05/06
 */
public class MiningSpeedDataTest {
    @Test
    public void enumTest() {
        assertEquals(MiningSpeed.OFF.getNext(), MiningSpeed.ON);
    }
}
