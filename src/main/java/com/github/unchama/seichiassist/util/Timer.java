package com.github.unchama.seichiassist.util;

import com.github.unchama.seichiassist.SeichiAssist;

import java.util.concurrent.TimeUnit;

public class Timer {
    private long t0, nanoSeconds;
    private TimeUnit timeUnit;
    
    public Timer() {
        this(TimeUnit.MILLISECONDS);
    }

    @Deprecated
    public Timer(String timeUnit) {
        this(convertToUnit(timeUnit));
    }

    public Timer(TimeUnit unit) {
        timeUnit = unit;
    }
     
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
     
    public final void setTimeUnit(String timeUnit) {
        this.timeUnit = convertToUnit(timeUnit);
    }

    private static TimeUnit convertToUnit(String unit) {
        switch (unit) {
            case "ms":
                return TimeUnit.MILLISECONDS;
            case "s":
                return TimeUnit.SECONDS;
            case "ns":
                return TimeUnit.NANOSECONDS;
            default:
                throw new RuntimeException("Timerはサポートされていない単位を受け取りました: " + unit);
        }
    }
     
    public void start() {
        t0 = System.nanoTime();
    }
    
    public void stop() {
        nanoSeconds = System.nanoTime() - t0;
    }
     
    public long getNanoSeconds() {
        return nanoSeconds;
    }
    
    public void sendLapTimeMessage(String message) {
    	nanoSeconds = System.nanoTime() - t0;
        SeichiAssist.instance.getServer().getConsoleSender().sendMessage(message + "(time: "+ this.getNanoSeconds() / 1000 +" ms)");
    	t0 = System.nanoTime();
    }
}