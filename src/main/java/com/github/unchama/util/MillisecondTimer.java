package com.github.unchama.util;

import com.github.unchama.seichiassist.SeichiAssist;

public class MillisecondTimer {
	private long startTime;

	private MillisecondTimer() {}

	public static MillisecondTimer getInitializedTimerInstance() {
		MillisecondTimer timer = new MillisecondTimer();
		timer.resetTimer();
		return timer;
	}

	public void resetTimer() {
		startTime = System.nanoTime();
	}

	public void sendLapTimeMessage(String message) {
		final long recordedNanoSecondDuration = System.nanoTime() - startTime;
		SeichiAssist.Companion.getInstance().getServer().getConsoleSender()
				.sendMessage(message + "(time: "+ recordedNanoSecondDuration / 1000 +" ms)");

		startTime = System.nanoTime();
	}
}
