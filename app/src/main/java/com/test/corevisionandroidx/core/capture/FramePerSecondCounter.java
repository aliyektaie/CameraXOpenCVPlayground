package com.test.corevisionandroidx.core.capture;

import static java.lang.Thread.sleep;

import java.util.ArrayList;
import java.util.List;

public class FramePerSecondCounter {
    private IFramePerSecondCounterListener listener = null;
    private List<Long> times = new ArrayList<>();
    private int updateDelay = 200;
    private boolean alive = true;
    private boolean paused = false;

    public void onFrame() {
        synchronized (this) {
            long time = System.currentTimeMillis();
            removeTimesLessThan1SecondAgo(time);
            times.add(time);
        }
    }

    private void removeTimesLessThan1SecondAgo(long time) {
        long prevTime = time - 1000;

        while (times.size() > 0) {
            long t = times.get(0);
            if (t < prevTime) {
                times.remove(0);
            } else {
                break;
            }
        }
    }

    public void setUpdateDelay(int updateDelay) {
        this.updateDelay = updateDelay;
    }

    public void stop() {
        paused = false;
        alive = false;
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    public void setListener(IFramePerSecondCounterListener listener) {
        this.listener = listener;
    }

    public void start() {
        new Thread(this::threadMain).start();
    }

    private void threadMain() {
        while (alive) {
            try {
                sleep(updateDelay);
                int fps = 0;
                synchronized (this) {
                    removeTimesLessThan1SecondAgo(System.currentTimeMillis());
                    fps = times.size();
                }
                if (!paused) listener.onFramePerSecondUpdate(fps);
            } catch (InterruptedException ignored) { }
        }
    }
}
