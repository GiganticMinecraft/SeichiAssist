package com.github.unchama.seichiassist.util;

import com.github.unchama.seichiassist.SeichiAssist;

public class MilliSecondTimer {
    private long startTime;

    private MilliSecondTimer() {}

    public static MilliSecondTimer initializedTimerInstance() {
        MilliSecondTimer timer = new MilliSecondTimer();
        timer.resetTimer();
        return timer;
    }

    public void resetTimer() {
        startTime = System.nanoTime();
    }
    
    public void sendLapTimeMessage(String message) {
        final long recordedNanoSecondDuration = System.nanoTime() - startTime;
        SeichiAssist.instance.getServer().getConsoleSender()
                .sendMessage(message + "(time: "+ recordedNanoSecondDuration / 1000 +" ms)");

        startTime = System.nanoTime();
    }
}
