package me.cdh.state;

import me.cdh.Animate;

public enum Action implements Animate {
    UP(4, 10),
    DOWN(4, 10),
    LEFT(4, 10),
    RIGHT(4, 10),
    CURLED(2, 40),
    LAYING(4, 20),
    SITTING(4, 20),

    LICKING(4, 40),
    RISING(2, 40),
    SLEEP(1, 10);
    private final int frame;
    private final int delay;
    Action(int frame, int delay) {
        this.frame = frame;
        this.delay = delay;
    }
    @Override
    public int delay() {
        return delay;
    }
    @Override
    public int frame() {
        return frame;
    }
}
