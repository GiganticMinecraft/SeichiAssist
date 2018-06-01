package com.github.unchama.seichiassist.util;

import com.github.unchama.seichiassist.SeichiAssist;

public class Timer {
    public static final String MILLISECOND = "ms";
    public static final String SECOND = "s";
    public static final String NANOSECOND = "ns";
     
    private long t0, time;
    private String timeUnit;
    
    private SeichiAssist plugin = SeichiAssist.plugin;
     
    public Timer() {
        this(NANOSECOND);
    }
     
    public Timer(String timeUnit) {
        setTimeUnit(timeUnit);
    }
     
    public String getTimeUnit() {
        return timeUnit;
    }
     
    public final void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }
     
    public void start() {
        t0 = System.nanoTime();
    }
    
    public void stop() {
        time = System.nanoTime() - t0;
    }
     
    public double getTime() {
        if(timeUnit.equals(NANOSECOND)) return time;
        if(timeUnit.equals(SECOND)) return time / 1000000000.0;
        if(timeUnit.equals(MILLISECOND)) return time / 1000000.0;
        return time;
    }
    
    public void sendLapTimeMessage(String message){
    	time = System.nanoTime() - t0;
    	plugin.getServer().getConsoleSender().sendMessage(message + "(time: "+ this.getTime() +" ms)");
    	t0 = System.nanoTime();
    }
    
}